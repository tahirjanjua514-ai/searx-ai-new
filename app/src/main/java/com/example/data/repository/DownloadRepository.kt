package com.example.data.repository

import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import com.example.data.local.PreferencesManager
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadOption
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaItem
import com.example.download.DownloadEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

class DownloadRepository(
    private val downloadDao: DownloadDao,
    private val downloadEngine: DownloadEngine,
    private val preferencesManager: PreferencesManager
) {
    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloads().map { list ->
        list.map { it.toDomain() }
    }

    val liveProgress = downloadEngine.liveProgress

    suspend fun startDownload(media: MediaItem, option: DownloadOption): Long {
        val initialItem = DownloadItem(
            mediaId = media.id,
            title = media.title,
            platform = media.platform,
            originalUrl = media.cleanedUrl,
            streamUrl = option.downloadUrl,
            quality = option.qualityLabel,
            format = option.format,
            localFilePath = "",
            totalBytes = option.estimatedSizeBytes,
            downloadedBytes = 0,
            progressPercent = 0,
            status = DownloadStatus.QUEUED,
            isAudioOnly = option.isAudioOnly
        )

        val id = downloadDao.insertDownload(DownloadEntity.fromDomain(initialItem))
        val itemWithId = initialItem.copy(id = id)

        val wifiOnly = preferencesManager.wifiOnlyFlow.first()
        downloadEngine.startOrResumeDownload(itemWithId, wifiOnly)
        return id
    }

    suspend fun resumeDownload(item: DownloadItem) {
        val wifiOnly = preferencesManager.wifiOnlyFlow.first()
        downloadEngine.startOrResumeDownload(item, wifiOnly)
    }

    fun pauseDownload(id: Long) {
        downloadEngine.pauseDownload(id)
    }

    fun cancelDownload(id: Long) {
        downloadEngine.cancelDownload(id)
    }

    suspend fun retryDownload(item: DownloadItem) {
        val wifiOnly = preferencesManager.wifiOnlyFlow.first()
        downloadEngine.startOrResumeDownload(item, wifiOnly)
    }

    suspend fun deleteDownload(item: DownloadItem) {
        downloadEngine.cancelDownload(item.id)
        if (item.localFilePath.isNotBlank()) {
            val file = File(item.localFilePath)
            if (file.exists()) file.delete()
        }
        downloadDao.deleteDownload(item.id)
    }

    suspend fun clearAll() {
        allDownloads.first().forEach { item ->
            downloadEngine.cancelDownload(item.id)
            if (item.localFilePath.isNotBlank()) {
                val file = File(item.localFilePath)
                if (file.exists()) file.delete()
            }
        }
        downloadDao.clearAll()
    }
}
