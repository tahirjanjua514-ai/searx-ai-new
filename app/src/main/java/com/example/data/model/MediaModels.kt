package com.example.data.model

data class DownloadOption(
    val qualityLabel: String,      // e.g. "1080p FHD", "720p HD", "480p", "Audio (MP3)"
    val format: String,            // "MP4" or "MP3"
    val downloadUrl: String,       // Direct downloadable media stream URL
    val estimatedSizeBytes: Long,  // bytes or 0 if unknown
    val isAudioOnly: Boolean = false,
    val isAvailable: Boolean = true
) {
    val formattedSize: String
        get() = if (estimatedSizeBytes > 0) {
            val mb = estimatedSizeBytes.toDouble() / (1024 * 1024)
            String.format("%.1f MB", mb)
        } else {
            "Variable / Stream"
        }
}

data class MediaItem(
    val id: String,
    val originalUrl: String,
    val cleanedUrl: String,
    val title: String,
    val author: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val platform: PlatformType,
    val availableOptions: List<DownloadOption>,
    val isDownloadable: Boolean,
    val restrictionReason: String? = null,
    val officialUrl: String = originalUrl
) {
    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return "Live / Short"
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DownloadItem(
    val id: Long = 0,
    val mediaId: String,
    val title: String,
    val platform: PlatformType,
    val originalUrl: String,
    val streamUrl: String,
    val quality: String,
    val format: String,
    val localFilePath: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val progressPercent: Int,
    val speedBytesPerSec: Long = 0,
    val etaSeconds: Long = 0,
    val status: DownloadStatus,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isAudioOnly: Boolean = false
) {
    val formattedSpeed: String
        get() = when {
            speedBytesPerSec >= 1024 * 1024 -> String.format("%.2f MB/s", speedBytesPerSec.toDouble() / (1024 * 1024))
            speedBytesPerSec >= 1024 -> String.format("%.1f KB/s", speedBytesPerSec.toDouble() / 1024)
            speedBytesPerSec > 0 -> "$speedBytesPerSec B/s"
            else -> "--"
        }

    val formattedEta: String
        get() = when {
            etaSeconds > 3600 -> String.format("%dh %dm", etaSeconds / 3600, (etaSeconds % 3600) / 60)
            etaSeconds > 60 -> String.format("%dm %ds", etaSeconds / 60, etaSeconds % 60)
            etaSeconds > 0 -> "${etaSeconds}s"
            else -> "--"
        }

    val formattedProgress: String
        get() {
            val curMb = downloadedBytes.toDouble() / (1024 * 1024)
            return if (totalBytes > 0) {
                val totMb = totalBytes.toDouble() / (1024 * 1024)
                String.format("%.1f MB / %.1f MB (%d%%)", curMb, totMb, progressPercent)
            } else {
                String.format("%.1f MB downloaded", curMb)
            }
        }
}
