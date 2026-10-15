package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DownloadEntity
import com.example.data.local.SearxDatabase
import com.example.data.model.DownloadStatus
import com.example.data.model.PlatformType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readAppNameStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SEARX AI", appName)
    }

    @Test
    fun testRoomDatabasePersistence() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = SearxDatabase.getDatabase(context)
        val dao = db.downloadDao()

        val entity = DownloadEntity(
            mediaId = "test_media_123",
            title = "Trending Reel",
            platformName = PlatformType.INSTAGRAM.name,
            originalUrl = "https://instagram.com/reel/123",
            streamUrl = "https://example.com/video.mp4",
            quality = "1080p",
            format = "MP4",
            localFilePath = "/storage/test.mp4",
            totalBytes = 15000000L,
            downloadedBytes = 15000000L,
            progressPercent = 100,
            status = DownloadStatus.COMPLETED.name,
            errorMessage = null,
            timestamp = System.currentTimeMillis(),
            isAudioOnly = false
        )

        val insertedId = dao.insertDownload(entity)
        assertNotNull(insertedId)

        val retrieved = dao.getDownloadById(insertedId)
        assertNotNull(retrieved)
        assertEquals("Trending Reel", retrieved?.title)
        assertEquals("INSTAGRAM", retrieved?.platformName)
        assertEquals(DownloadStatus.COMPLETED.name, retrieved?.status)

        val allList = dao.getAllDownloads().first()
        assertEquals(1, allList.size)

        dao.clearAll()
        val emptyList = dao.getAllDownloads().first()
        assertEquals(0, emptyList.size)
    }
}
