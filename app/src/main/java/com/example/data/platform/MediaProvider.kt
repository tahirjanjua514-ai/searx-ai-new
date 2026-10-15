package com.example.data.platform

import com.example.data.model.MediaItem
import com.example.data.model.PlatformType

interface MediaProvider {
    val platformType: PlatformType

    /**
     * Checks if the provided URL belongs to this platform.
     */
    fun matchesUrl(url: String): Boolean

    /**
     * Cleans tracking parameters (e.g. utm_*, si, igsh, etc.) safely from URL.
     */
    fun sanitizeUrl(url: String): String

    /**
     * Analyzes URL, parses metadata, checks authorization, and builds available download options.
     */
    suspend fun analyzeUrl(url: String): Result<MediaItem>
}
