package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Downloads : Screen("downloads", "Downloads", Icons.Default.VideoLibrary)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Player : Screen("player", "Player")
}
