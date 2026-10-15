package com.example.download

import android.content.Context
import android.os.Environment
import com.example.data.local.DownloadDao
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.util.NetworkUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

class DownloadEngine(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val activeJobs = ConcurrentHashMap<Long, Job>()

    // In-memory live progress map for high-frequency Compose updates
    private val _liveProgress = MutableStateFlow<Map<Long, DownloadItem>>(emptyMap())
    val liveProgress: StateFlow<Map<Long, DownloadItem>> = _liveProgress.asStateFlow()

    fun startOrResumeDownload(item: DownloadItem, wifiOnly: Boolean = false) {
        if (activeJobs.containsKey(item.id)) return // Already downloading

        if (wifiOnly && !NetworkUtils.isWifiConnected(context)) {
            scope.launch {
                downloadDao.updateProgress(
                    id = item.id,
                    downloadedBytes = item.downloadedBytes,
                    totalBytes = item.totalBytes,
                    progressPercent = item.progressPercent,
                    status = DownloadStatus.FAILED.name,
                    errorMessage = "Wi-Fi Only is enabled, but Wi-Fi is not connected."
                )
            }
            return
        }

        if (!NetworkUtils.isConnected(context)) {
            scope.launch {
                downloadDao.updateProgress(
                    id = item.id,
                    downloadedBytes = item.downloadedBytes,
                    totalBytes = item.totalBytes,
                    progressPercent = item.progressPercent,
                    status = DownloadStatus.FAILED.name,
                    errorMessage = "No internet connection."
                )
            }
            return
        }

        val job = scope.launch(Dispatchers.IO) {
            runDownload(item)
        }
        activeJobs[item.id] = job
    }

    fun pauseDownload(id: Long) {
        val job = activeJobs.remove(id)
        job?.cancel()
        scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(id)
            if (entity != null) {
                downloadDao.updateProgress(
                    id = id,
                    downloadedBytes = entity.downloadedBytes,
                    totalBytes = entity.totalBytes,
                    progressPercent = entity.progressPercent,
                    status = DownloadStatus.PAUSED.name,
                    errorMessage = null
                )
            }
            updateLiveProgress(id) { it?.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0) }
        }
    }

    fun cancelDownload(id: Long) {
        val job = activeJobs.remove(id)
        job?.cancel()
        scope.launch(Dispatchers.IO) {
            val entity = downloadDao.getDownloadById(id)
            if (entity != null) {
                // Delete partial file
                val file = File(entity.localFilePath)
                if (file.exists()) file.delete()
                downloadDao.updateProgress(
                    id = id,
                    downloadedBytes = 0,
                    totalBytes = entity.totalBytes,
                    progressPercent = 0,
                    status = DownloadStatus.CANCELLED.name,
                    errorMessage = "Cancelled by user"
                )
            }
            _liveProgress.value = _liveProgress.value - id
        }
    }

    private suspend fun runDownload(initialItem: DownloadItem) {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val safeTitle = initialItem.title.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(40)
        val ext = if (initialItem.isAudioOnly) "mp3" else "mp4"
        val targetFile = if (initialItem.localFilePath.isNotBlank() && File(initialItem.localFilePath).exists()) {
            File(initialItem.localFilePath)
        } else {
            File(downloadDir, "SEARX_${initialItem.platform.name}_${safeTitle}_${initialItem.id}.$ext")
        }

        var downloadedBytes = if (targetFile.exists()) targetFile.length() else 0L

        updateLiveProgress(initialItem.id) {
            initialItem.copy(
                status = DownloadStatus.DOWNLOADING,
                localFilePath = targetFile.absolutePath,
                downloadedBytes = downloadedBytes
            )
        }

        downloadDao.updateProgress(
            id = initialItem.id,
            downloadedBytes = downloadedBytes,
            totalBytes = initialItem.totalBytes,
            progressPercent = initialItem.progressPercent,
            status = DownloadStatus.DOWNLOADING.name
        )

        try {
            val requestBuilder = Request.Builder().url(initialItem.streamUrl)
            if (downloadedBytes > 0) {
                requestBuilder.header("Range", "bytes=$downloadedBytes-")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    throw IllegalStateException("Server returned HTTP ${response.code}: ${response.message}")
                }

                val body = response.body ?: throw IllegalStateException("Empty response body from media server")
                val contentLength = body.contentLength()
                val totalBytes = if (response.code == 206) {
                    downloadedBytes + contentLength
                } else if (contentLength > 0) {
                    contentLength
                } else {
                    initialItem.totalBytes
                }

                val append = (response.code == 206)
                val outputStream = FileOutputStream(targetFile, append)
                val inputStream: InputStream = body.byteStream()
                val buffer = ByteArray(32 * 1024)

                var bytesSinceLastSpeedCalc = 0L
                var lastTime = System.currentTimeMillis()
                var currentSpeed = 0L

                outputStream.use { out ->
                    inputStream.use { input ->
                        var read: Int
                        while (coroutineContext.isActive) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead == -1) break
                            read = bytesRead
                            out.write(buffer, 0, read)
                            downloadedBytes += read
                            bytesSinceLastSpeedCalc += read

                            val now = System.currentTimeMillis()
                            val elapsed = now - lastTime
                            if (elapsed >= 500) {
                                currentSpeed = (bytesSinceLastSpeedCalc * 1000) / elapsed
                                bytesSinceLastSpeedCalc = 0L
                                lastTime = now

                                val remainingBytes = if (totalBytes > downloadedBytes) totalBytes - downloadedBytes else 0L
                                val eta = if (currentSpeed > 0) remainingBytes / currentSpeed else 0L
                                val percent = if (totalBytes > 0) {
                                    ((downloadedBytes.toDouble() / totalBytes) * 100).toInt().coerceIn(0, 100)
                                } else 0

                                updateLiveProgress(initialItem.id) { current ->
                                    current?.copy(
                                        downloadedBytes = downloadedBytes,
                                        totalBytes = totalBytes,
                                        progressPercent = percent,
                                        speedBytesPerSec = currentSpeed,
                                        etaSeconds = eta,
                                        status = DownloadStatus.DOWNLOADING
                                    )
                                }

                                downloadDao.updateProgress(
                                    id = initialItem.id,
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes,
                                    progressPercent = percent,
                                    status = DownloadStatus.DOWNLOADING.name
                                )
                            }
                        }
                    }
                }

                // Download Finished
                val finalPercent = 100
                updateLiveProgress(initialItem.id) { current ->
                    current?.copy(
                        downloadedBytes = downloadedBytes,
                        totalBytes = downloadedBytes,
                        progressPercent = finalPercent,
                        speedBytesPerSec = 0,
                        etaSeconds = 0,
                        status = DownloadStatus.COMPLETED
                    )
                }

                downloadDao.updateProgress(
                    id = initialItem.id,
                    downloadedBytes = downloadedBytes,
                    totalBytes = downloadedBytes,
                    progressPercent = finalPercent,
                    status = DownloadStatus.COMPLETED.name,
                    errorMessage = null
                )
            }
        } catch (e: CancellationException) {
            // Paused or Cancelled
            updateLiveProgress(initialItem.id) { it?.copy(status = DownloadStatus.PAUSED, speedBytesPerSec = 0) }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Download error"
            updateLiveProgress(initialItem.id) {
                it?.copy(status = DownloadStatus.FAILED, errorMessage = errorMsg, speedBytesPerSec = 0)
            }
            downloadDao.updateProgress(
                id = initialItem.id,
                downloadedBytes = downloadedBytes,
                totalBytes = initialItem.totalBytes,
                progressPercent = initialItem.progressPercent,
                status = DownloadStatus.FAILED.name,
                errorMessage = errorMsg
            )
        } finally {
            activeJobs.remove(initialItem.id)
        }
    }

    private fun updateLiveProgress(id: Long, transform: (DownloadItem?) -> DownloadItem?) {
        val current = _liveProgress.value.toMutableMap()
        val item = transform(current[id])
        if (item != null) {
            current[id] = item
        } else {
            current.remove(id)
        }
        _liveProgress.value = current
    }
}
