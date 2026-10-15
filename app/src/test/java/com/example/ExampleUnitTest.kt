package com.example

import com.example.data.model.DownloadItem
import com.example.data.model.DownloadOption
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaItem
import com.example.data.model.PlatformType
import com.example.data.platform.FacebookProvider
import com.example.data.platform.InstagramProvider
import com.example.data.platform.PlatformManager
import com.example.data.platform.TikTokProvider
import com.example.data.platform.YouTubeProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val platformManager = PlatformManager()
    private val youtubeProvider = YouTubeProvider()
    private val tikTokProvider = TikTokProvider()
    private val facebookProvider = FacebookProvider()
    private val instagramProvider = InstagramProvider()

    @Test
    fun testPlatformDetection() {
        assertEquals(PlatformType.YOUTUBE, platformManager.detectPlatform("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals(PlatformType.YOUTUBE, platformManager.detectPlatform("https://youtu.be/dQw4w9WgXcQ?si=tracking123"))
        assertEquals(PlatformType.YOUTUBE, platformManager.detectPlatform("https://www.youtube.com/shorts/abcd1234"))

        assertEquals(PlatformType.TIKTOK, platformManager.detectPlatform("https://www.tiktok.com/@user/video/1234567890123456789"))
        assertEquals(PlatformType.TIKTOK, platformManager.detectPlatform("https://vm.tiktok.com/ZM123456/"))

        assertEquals(PlatformType.FACEBOOK, platformManager.detectPlatform("https://www.facebook.com/reel/987654321"))
        assertEquals(PlatformType.FACEBOOK, platformManager.detectPlatform("https://fb.watch/abcd1234/"))

        assertEquals(PlatformType.INSTAGRAM, platformManager.detectPlatform("https://www.instagram.com/reel/C8kL8M_O1yZ/"))
        assertEquals(PlatformType.INSTAGRAM, platformManager.detectPlatform("https://www.instagram.com/p/C8kL8M_O1yZ/"))

        assertEquals(PlatformType.UNKNOWN, platformManager.detectPlatform("https://commondatastorage.googleapis.com/sample.mp4"))
    }

    @Test
    fun testTrackingSanitization() {
        // YouTube tracking removal
        val ytClean = youtubeProvider.sanitizeUrl("https://youtu.be/dQw4w9WgXcQ?si=BeaconTracker99&feature=shared")
        assertFalse(ytClean.contains("si="))
        assertFalse(ytClean.contains("feature="))
        assertTrue(ytClean.contains("dQw4w9WgXcQ"))

        // TikTok tracking removal
        val ttClean = tikTokProvider.sanitizeUrl("https://www.tiktok.com/@creator/video/12345?is_from_webapp=1&sender_device=pc")
        assertFalse(ttClean.contains("is_from_webapp"))
        assertFalse(ttClean.contains("sender_device"))

        // Facebook tracking removal
        val fbClean = facebookProvider.sanitizeUrl("https://www.facebook.com/reel/12345?mibextid=rS40aB7S9Ucbxw6v")
        assertFalse(fbClean.contains("mibextid"))

        // Instagram tracking removal
        val igClean = instagramProvider.sanitizeUrl("https://www.instagram.com/reel/C8kL8M_O1yZ/?igsh=MWQ1ZGUxMzBkMA==")
        assertFalse(igClean.contains("igsh"))
    }

    @Test
    fun testMediaFormatting() {
        val media = MediaItem(
            id = "test_1",
            originalUrl = "https://youtube.com/watch?v=123",
            cleanedUrl = "https://youtube.com/watch?v=123",
            title = "Test Video",
            author = "Test Creator",
            durationSeconds = 125, // 2 minutes 5 seconds
            thumbnailUrl = "",
            platform = PlatformType.YOUTUBE,
            availableOptions = emptyList(),
            isDownloadable = true
        )
        assertEquals("02:05", media.formattedDuration)

        val option = DownloadOption(
            qualityLabel = "1080p",
            format = "MP4",
            downloadUrl = "https://example.com/video.mp4",
            estimatedSizeBytes = 52428800L // 50 MB
        )
        assertEquals("50.0 MB", option.formattedSize)

        val downloadItem = DownloadItem(
            id = 1L,
            mediaId = "test_1",
            title = "Test Video",
            platform = PlatformType.YOUTUBE,
            originalUrl = "https://youtube.com",
            streamUrl = "https://example.com/v.mp4",
            quality = "1080p",
            format = "MP4",
            localFilePath = "/path/test.mp4",
            totalBytes = 100 * 1024 * 1024L,
            downloadedBytes = 50 * 1024 * 1024L,
            progressPercent = 50,
            speedBytesPerSec = 2 * 1024 * 1024L,
            etaSeconds = 25,
            status = DownloadStatus.DOWNLOADING
        )

        assertEquals("2.00 MB/s", downloadItem.formattedSpeed)
        assertEquals("25s", downloadItem.formattedEta)
        assertTrue(downloadItem.formattedProgress.contains("50%"))
    }

    @Test
    fun testAnalyzeUrlAsync() = runBlocking {
        val result = platformManager.parseAndAnalyze("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        assertTrue(result.isSuccess)
        val media = result.getOrNull()
        assertNotNull(media)
        assertEquals(PlatformType.YOUTUBE, media?.platform)
        assertTrue(media!!.availableOptions.isNotEmpty())
    }
}
