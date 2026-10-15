package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.PlatformType

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaId: String,
    val title: String,
    val platformName: String,
    val originalUrl: String,
    val streamUrl: String,
    val quality: String,
    val format: String,
    val localFilePath: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val progressPercent: Int,
    val status: String,
    val errorMessage: String?,
    val timestamp: Long,
    val isAudioOnly: Boolean
) {
    fun toDomain(): DownloadItem {
        val platform = try {
            PlatformType.valueOf(platformName)
        } catch (_: Exception) {
            PlatformType.UNKNOWN
        }
        val downloadStatus = try {
            DownloadStatus.valueOf(status)
        } catch (_: Exception) {
            DownloadStatus.FAILED
        }
        return DownloadItem(
            id = id,
            mediaId = mediaId,
            title = title,
            platform = platform,
            originalUrl = originalUrl,
            streamUrl = streamUrl,
            quality = quality,
            format = format,
            localFilePath = localFilePath,
            totalBytes = totalBytes,
            downloadedBytes = downloadedBytes,
            progressPercent = progressPercent,
            status = downloadStatus,
            errorMessage = errorMessage,
            timestamp = timestamp,
            isAudioOnly = isAudioOnly
        )
    }

    companion object {
        fun fromDomain(item: DownloadItem): DownloadEntity {
            return DownloadEntity(
                id = item.id,
                mediaId = item.mediaId,
                title = item.title,
                platformName = item.platform.name,
                originalUrl = item.originalUrl,
                streamUrl = item.streamUrl,
                quality = item.quality,
                format = item.format,
                localFilePath = item.localFilePath,
                totalBytes = item.totalBytes,
                downloadedBytes = item.downloadedBytes,
                progressPercent = item.progressPercent,
                status = item.status.name,
                errorMessage = item.errorMessage,
                timestamp = item.timestamp,
                isAudioOnly = item.isAudioOnly
            )
        }
    }
}
