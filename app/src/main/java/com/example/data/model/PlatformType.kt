package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.InstagramGradientStart
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.YoutubeRed

enum class PlatformType(
    val displayName: String,
    val brandColor: Color,
    val domains: List<String>,
    val description: String
) {
    YOUTUBE(
        displayName = "YouTube",
        brandColor = YoutubeRed,
        domains = listOf("youtube.com", "youtu.be", "m.youtube.com"),
        description = "Public Shorts, videos & authorized content"
    ),
    TIKTOK(
        displayName = "TikTok",
        brandColor = TikTokCyan,
        domains = listOf("tiktok.com", "vm.tiktok.com", "vt.tiktok.com"),
        description = "Public creator clips & sounds"
    ),
    FACEBOOK(
        displayName = "Facebook",
        brandColor = FacebookBlue,
        domains = listOf("facebook.com", "fb.watch", "m.facebook.com"),
        description = "Public reels, watch clips & stories"
    ),
    INSTAGRAM(
        displayName = "Instagram",
        brandColor = InstagramGradientStart,
        domains = listOf("instagram.com", "instagr.am"),
        description = "Public reels, IGTV & video posts"
    ),
    UNKNOWN(
        displayName = "Direct / Other",
        brandColor = Color(0xFF00F5D4),
        domains = emptyList(),
        description = "Direct MP4/WebM video stream"
    );

    companion object {
        fun detectFromUrl(url: String): PlatformType {
            val lower = url.lowercase().trim()
            return when {
                lower.contains("youtube.com") || lower.contains("youtu.be") -> YOUTUBE
                lower.contains("tiktok.com") -> TIKTOK
                lower.contains("facebook.com") || lower.contains("fb.watch") -> FACEBOOK
                lower.contains("instagram.com") || lower.contains("instagr.am") -> INSTAGRAM
                else -> UNKNOWN
            }
        }
    }
}
