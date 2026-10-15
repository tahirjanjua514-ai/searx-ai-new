package com.example.data.platform

import android.net.Uri
import com.example.data.model.DownloadOption
import com.example.data.model.MediaItem
import com.example.data.model.PlatformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DirectStreamProvider : MediaProvider {
    override val platformType: PlatformType = PlatformType.UNKNOWN

    override fun matchesUrl(url: String): Boolean {
        val lower = url.lowercase().trim()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }

    override fun sanitizeUrl(url: String): String {
        return url.trim()
    }

    override suspend fun analyzeUrl(url: String): Result<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val clean = sanitizeUrl(url)
            val uri = Uri.parse(clean)
            val fileName = uri.lastPathSegment?.substringBefore("?") ?: "Direct Media Stream"
            val isAudio = clean.endsWith(".mp3", true) || clean.endsWith(".m4a", true) || clean.endsWith(".aac", true)

            val options = listOf(
                DownloadOption(
                    qualityLabel = if (isAudio) "Direct Audio Stream" else "Original Direct Stream",
                    format = if (isAudio) "MP3" else "MP4",
                    downloadUrl = clean,
                    estimatedSizeBytes = 25 * 1024 * 1024L,
                    isAudioOnly = isAudio,
                    isAvailable = true
                )
            )

            Result.success(
                MediaItem(
                    id = "direct_${System.currentTimeMillis() % 10000}",
                    originalUrl = url,
                    cleanedUrl = clean,
                    title = fileName.replace("-", " ").replace("_", " "),
                    author = uri.host ?: "Direct Web Server",
                    durationSeconds = 120,
                    thumbnailUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80",
                    platform = PlatformType.UNKNOWN,
                    availableOptions = options,
                    isDownloadable = true,
                    restrictionReason = null,
                    officialUrl = clean
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
