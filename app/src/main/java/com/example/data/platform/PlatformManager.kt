package com.example.data.platform

import com.example.data.model.MediaItem
import com.example.data.model.PlatformType

class PlatformManager {
    private val youtubeProvider = YouTubeProvider()
    private val tikTokProvider = TikTokProvider()
    private val facebookProvider = FacebookProvider()
    private val instagramProvider = InstagramProvider()
    private val directStreamProvider = DirectStreamProvider()

    private val providers = listOf(
        youtubeProvider,
        tikTokProvider,
        facebookProvider,
        instagramProvider
    )

    fun detectPlatform(url: String): PlatformType {
        for (provider in providers) {
            if (provider.matchesUrl(url)) {
                return provider.platformType
            }
        }
        return if (directStreamProvider.matchesUrl(url)) PlatformType.UNKNOWN else PlatformType.UNKNOWN
    }

    fun cleanUrl(url: String): String {
        for (provider in providers) {
            if (provider.matchesUrl(url)) {
                return provider.sanitizeUrl(url)
            }
        }
        return directStreamProvider.sanitizeUrl(url)
    }

    suspend fun parseAndAnalyze(url: String): Result<MediaItem> {
        val trimmed = url.trim()
        if (trimmed.isBlank() || (!trimmed.startsWith("http://") && !trimmed.startsWith("https://"))) {
            return Result.failure(IllegalArgumentException("Please enter a valid URL starting with http:// or https://"))
        }

        for (provider in providers) {
            if (provider.matchesUrl(trimmed)) {
                return provider.analyzeUrl(trimmed)
            }
        }

        // Fallback to direct stream provider if it looks like a valid web URL
        return directStreamProvider.analyzeUrl(trimmed)
    }
}
