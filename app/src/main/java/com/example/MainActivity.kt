package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.DownloadStatus
import com.example.ui.SearxAiViewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SearxAiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SearxAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val downloads by viewModel.downloadsList.collectAsState()

            val isDarkTheme = when (uiState.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
            var showOnboarding by remember { mutableStateOf(false) }

            SearxAiTheme(darkTheme = isDarkTheme) {
                if (showOnboarding) {
                    OnboardingScreen(
                        onGetStarted = { showOnboarding = false }
                    )
                } else if (uiState.currentlyPlayingMedia != null) {
                    PlayerScreen(
                        downloadItem = uiState.currentlyPlayingMedia!!,
                        onClose = { viewModel.stopPlayer() }
                    )
                } else {
                    BackHandler(enabled = currentScreen != Screen.Home) {
                        currentScreen = Screen.Home
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val activeCount = downloads.count {
                                it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED
                            }

                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                val items = listOf(Screen.Home, Screen.Downloads, Screen.Settings)
                                items.forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            if (screen == Screen.Downloads && activeCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        ) {
                                                            Text("$activeCount")
                                                        }
                                                    }
                                                ) {
                                                    screen.icon?.let {
                                                        Icon(imageVector = it, contentDescription = screen.title)
                                                    }
                                                }
                                            } else {
                                                screen.icon?.let {
                                                    Icon(imageVector = it, contentDescription = screen.title)
                                                }
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = when (screen) {
                                                    Screen.Home -> stringResource(id = R.string.nav_home)
                                                    Screen.Downloads -> stringResource(id = R.string.nav_downloads)
                                                    Screen.Settings -> stringResource(id = R.string.nav_settings)
                                                    else -> screen.title
                                                },
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_${screen.route}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
                                when (screen) {
                                    Screen.Home -> {
                                        HomeScreen(
                                            uiState = uiState,
                                            onUrlChange = { viewModel.onUrlInputChanged(it) },
                                            onPasteClick = { viewModel.pasteFromClipboard(it) },
                                            onAnalyzeClick = { viewModel.analyzeUrl() },
                                            onPlatformSelect = { viewModel.selectPlatformShortcut(it) },
                                            onStartDownload = {
                                                viewModel.startDownload(it)
                                                currentScreen = Screen.Downloads
                                            },
                                            onDismissPreview = { viewModel.dismissPreviewSheet() }
                                        )
                                    }
                                    Screen.Downloads -> {
                                        DownloadsScreen(
                                            uiState = uiState,
                                            downloads = downloads,
                                            onTabSelected = { viewModel.setDownloadsTab(it) },
                                            onSearchQueryChange = { viewModel.setDownloadsSearchQuery(it) },
                                            onSortChange = { viewModel.setDownloadsSort(it) },
                                            onPauseClick = { viewModel.pauseDownload(it) },
                                            onResumeClick = { viewModel.resumeDownload(it) },
                                            onCancelClick = { viewModel.cancelDownload(it) },
                                            onRetryClick = { viewModel.retryDownload(it) },
                                            onDeleteClick = { viewModel.deleteDownload(it) },
                                            onClearAll = { viewModel.clearHistory() },
                                            onPlayClick = { viewModel.playMedia(it) }
                                        )
                                    }
                                    Screen.Settings -> {
                                        SettingsScreen(
                                            uiState = uiState,
                                            onThemeModeChange = { viewModel.setThemeMode(it) },
                                            onLanguageChange = { viewModel.setLanguage(it) },
                                            onWifiOnlyChange = { viewModel.setWifiOnly(it) },
                                            onClearHistory = { viewModel.clearHistory() }
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
