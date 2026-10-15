package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.local.SearxDatabase
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadOption
import com.example.data.model.DownloadStatus
import com.example.data.model.MediaItem
import com.example.data.model.PlatformType
import com.example.data.platform.PlatformManager
import com.example.data.repository.DownloadRepository
import com.example.download.DownloadEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    DATE_DESC,
    SIZE_DESC,
    NAME_ASC
}

data class UiState(
    val urlInput: String = "",
    val detectedPlatform: PlatformType = PlatformType.UNKNOWN,
    val isAnalyzing: Boolean = false,
    val analyzedMedia: MediaItem? = null,
    val analysisError: String? = null,
    val showPreviewSheet: Boolean = false,
    val downloadsTab: Int = 0, // 0: Active, 1: Completed, 2: Failed
    val downloadsSearchQuery: String = "",
    val downloadsSort: SortOption = SortOption.DATE_DESC,
    val currentlyPlayingMedia: DownloadItem? = null,
    val themeMode: String = "DARK",
    val language: String = "en",
    val wifiOnly: Boolean = false,
    val autoClipboard: Boolean = true
)

class SearxAiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SearxDatabase.getDatabase(application)
    private val downloadDao = database.downloadDao()
    private val downloadEngine = DownloadEngine(application, downloadDao, viewModelScope)
    private val preferencesManager = PreferencesManager(application)
    private val platformManager = PlatformManager()

    val downloadRepository = DownloadRepository(downloadDao, downloadEngine, preferencesManager)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Combined downloads observing DB and live download engine updates
    val downloadsList: StateFlow<List<DownloadItem>> = combine(
        downloadRepository.allDownloads,
        downloadRepository.liveProgress
    ) { dbList, liveMap ->
        dbList.map { item ->
            liveMap[item.id] ?: item
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect preferences
        viewModelScope.launch {
            preferencesManager.themeModeFlow.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
        viewModelScope.launch {
            preferencesManager.languageFlow.collect { lang ->
                _uiState.value = _uiState.value.copy(language = lang)
            }
        }
        viewModelScope.launch {
            preferencesManager.wifiOnlyFlow.collect { wifiOnly ->
                _uiState.value = _uiState.value.copy(wifiOnly = wifiOnly)
            }
        }
        viewModelScope.launch {
            preferencesManager.autoClipboardFlow.collect { autoClip ->
                _uiState.value = _uiState.value.copy(autoClipboard = autoClip)
            }
        }
    }

    fun onUrlInputChanged(newUrl: String) {
        val detected = platformManager.detectPlatform(newUrl)
        _uiState.value = _uiState.value.copy(
            urlInput = newUrl,
            detectedPlatform = detected,
            analysisError = null
        )
    }

    fun pasteFromClipboard(clipText: String) {
        val cleaned = clipText.trim()
        if (cleaned.isNotBlank()) {
            onUrlInputChanged(cleaned)
            analyzeUrl()
        }
    }

    fun selectPlatformShortcut(platform: PlatformType) {
        val sampleUrl = when (platform) {
            PlatformType.YOUTUBE -> "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            PlatformType.TIKTOK -> "https://www.tiktok.com/@creator/video/720192837482"
            PlatformType.FACEBOOK -> "https://www.facebook.com/reel/1029384756"
            PlatformType.INSTAGRAM -> "https://www.instagram.com/reel/C8kL8M_O1yZ/"
            PlatformType.UNKNOWN -> ""
        }
        if (sampleUrl.isNotBlank()) {
            onUrlInputChanged(sampleUrl)
        }
    }

    fun analyzeUrl() {
        val rawUrl = _uiState.value.urlInput.trim()
        if (rawUrl.isBlank()) {
            _uiState.value = _uiState.value.copy(analysisError = "Please enter or paste a valid video URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                analysisError = null,
                analyzedMedia = null
            )

            val cleaned = platformManager.cleanUrl(rawUrl)
            val result = platformManager.parseAndAnalyze(cleaned)

            result.onSuccess { media ->
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analyzedMedia = media,
                    showPreviewSheet = true,
                    analysisError = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisError = error.localizedMessage ?: "Unable to analyze this link. Please check the URL."
                )
            }
        }
    }

    fun dismissPreviewSheet() {
        _uiState.value = _uiState.value.copy(showPreviewSheet = false)
    }

    fun startDownload(option: DownloadOption) {
        val media = _uiState.value.analyzedMedia ?: return
        viewModelScope.launch {
            downloadRepository.startDownload(media, option)
            _uiState.value = _uiState.value.copy(
                showPreviewSheet = false,
                downloadsTab = 0 // Switch to Active tab
            )
        }
    }

    fun pauseDownload(id: Long) {
        downloadRepository.pauseDownload(id)
    }

    fun resumeDownload(item: DownloadItem) {
        viewModelScope.launch {
            downloadRepository.resumeDownload(item)
        }
    }

    fun cancelDownload(id: Long) {
        downloadRepository.cancelDownload(id)
    }

    fun retryDownload(item: DownloadItem) {
        viewModelScope.launch {
            downloadRepository.retryDownload(item)
        }
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(item)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            downloadRepository.clearAll()
        }
    }

    fun setDownloadsTab(index: Int) {
        _uiState.value = _uiState.value.copy(downloadsTab = index)
    }

    fun setDownloadsSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(downloadsSearchQuery = query)
    }

    fun setDownloadsSort(sort: SortOption) {
        _uiState.value = _uiState.value.copy(downloadsSort = sort)
    }

    fun playMedia(item: DownloadItem) {
        _uiState.value = _uiState.value.copy(currentlyPlayingMedia = item)
    }

    fun stopPlayer() {
        _uiState.value = _uiState.value.copy(currentlyPlayingMedia = null)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setLanguage(lang)
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWifiOnly(enabled)
        }
    }

    fun setAutoClipboard(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoClipboard(enabled)
        }
    }
}
