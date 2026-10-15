package com.example.ui.screens

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.UiState
import com.example.ui.components.CyberPill
import java.io.File

@Composable
fun SettingsScreen(
    uiState: UiState,
    onThemeModeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var storageBytes by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        storageBytes = calculateDirSize(downloadDir)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.settings_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Preferences, Security & App Controls",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section: General Settings
        item {
            SettingsGroupCard(title = "App Preferences") {
                // Language
                SettingsRow(
                    icon = Icons.Default.Language,
                    title = stringResource(id = R.string.setting_language),
                    subtitle = if (uiState.language == "ur") "اردو (Urdu)" else "English (US)",
                    onClick = { showLanguageDialog = true },
                    modifier = Modifier.testTag("setting_language_row")
                )

                // Theme Mode
                SettingsRow(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(id = R.string.setting_theme),
                    subtitle = when (uiState.themeMode) {
                        "DARK" -> "Dark Mode (Cyber Blue)"
                        "LIGHT" -> "Light Mode"
                        else -> "System Default"
                    },
                    onClick = { showThemeDialog = true },
                    modifier = Modifier.testTag("setting_theme_row")
                )

                // Wi-Fi Only Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.setting_wifi_only),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Prevent mobile cellular data usage for large video files",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = uiState.wifiOnly,
                        onCheckedChange = onWifiOnlyChange,
                        modifier = Modifier.testTag("wifi_only_switch"),
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        // Section: Storage
        item {
            SettingsGroupCard(title = "Storage & Cache") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.setting_storage_info),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "App media folder usage: ${formatBytes(storageBytes)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    CyberPill(text = formatBytes(storageBytes), accentColor = MaterialTheme.colorScheme.secondary)
                }

                SettingsRow(
                    icon = Icons.Default.Delete,
                    title = stringResource(id = R.string.clear_history),
                    subtitle = "Delete all saved media files & database history",
                    onClick = onClearHistory,
                    iconTint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Section: Legal & About
        item {
            SettingsGroupCard(title = "Information & Support") {
                SettingsRow(
                    icon = Icons.Default.PrivacyTip,
                    title = stringResource(id = R.string.setting_privacy_policy),
                    subtitle = "Data processing, zero tracking, safe storage",
                    onClick = { showPrivacyDialog = true }
                )

                SettingsRow(
                    icon = Icons.Default.Info,
                    title = stringResource(id = R.string.setting_about),
                    subtitle = "Version 2.0 • Architecture & Platform Rules",
                    onClick = { showAboutDialog = true }
                )

                SettingsRow(
                    icon = Icons.Default.Email,
                    title = stringResource(id = R.string.setting_feedback),
                    subtitle = "Send developer suggestions or report an issue",
                    onClick = { showFeedbackDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language / زبان منتخب کریں") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = uiState.language == "en", onClick = {
                            onLanguageChange("en")
                            showLanguageDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English (Default)")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("ur")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = uiState.language == "ur", onClick = {
                            onLanguageChange("ur")
                            showLanguageDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اردو (Urdu)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme Mode") },
            text = {
                Column {
                    listOf("DARK" to "Dark Cyber Neon", "LIGHT" to "Clean Light", "SYSTEM" to "System Default").forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeModeChange(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = uiState.themeMode == mode, onClick = {
                                onThemeModeChange(mode)
                                showThemeDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy — SEARX AI") },
            text = {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "1. Zero Personal Data Tracking: SEARX AI does not collect or log personal identifiers, browsing habits, or account credentials.\n\n" +
                                "2. Clipboard Protection: The clipboard is accessed strictly and solely upon user action (pressing the Paste button) and is never read covertly in the background.\n\n" +
                                "3. Local Media Storage: All downloaded media is saved strictly to your device's private storage or standard media directory. No media is uploaded to third-party cloud servers without consent.\n\n" +
                                "4. Legal & DRM Compliance: SEARX AI functions within standard platform terms and copyright laws. DRM-protected or unauthorized paid content is strictly not extracted.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("I Understand") }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About SEARX AI") },
            text = {
                Column {
                    Text(
                        text = "SEARX AI v2.0\n" +
                                "Next-Generation Social Media Video Downloader & Media Hub.\n\n" +
                                "Built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and Android Media3 ExoPlayer.\n\n" +
                                "Features:\n" +
                                "• Smart link detection with tracking removal\n" +
                                "• Multi-quality streaming & audio extraction\n" +
                                "• Multi-threaded chunked downloads with pause/resume\n" +
                                "• Built-in full-featured ExoPlayer\n" +
                                "• English & Urdu language support",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }

    // Feedback Dialog
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Support & Feedback") },
            text = {
                Text(
                    text = "Have questions, feature requests, or encountered an issue with a video link?\n\nContact the SEARX AI development team at support@searxai.app or via GitHub issue tracker.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(onClick = { showFeedbackDialog = false }) { Text("Got It") }
            }
        )
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun calculateDirSize(dir: File): Long {
    var size = 0L
    if (dir.exists()) {
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) calculateDirSize(file) else file.length()
        }
    }
    return size
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes.toDouble() / (1024 * 1024 * 1024))
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
        bytes >= 1024 -> String.format("%.1f KB", bytes.toDouble() / 1024)
        else -> "$bytes B"
    }
}
