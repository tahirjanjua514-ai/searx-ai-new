package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SearxDarkPrimary,
    onPrimary = SearxDarkOnPrimary,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = SearxDarkSecondary,
    onSecondary = Color(0xFF083344),
    tertiary = SearxDarkTertiary,
    onTertiary = Color(0xFF1E1B4B),
    background = SearxDarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = SearxDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SearxDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = Color(0xFFF87171)
)

private val LightColorScheme = lightColorScheme(
    primary = SearxLightPrimary,
    onPrimary = SearxLightOnPrimary,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = SearxLightSecondary,
    onSecondary = Color.White,
    tertiary = SearxLightTertiary,
    onTertiary = Color.White,
    background = SearxLightBg,
    onBackground = Color(0xFF0F172A),
    surface = SearxLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = SearxLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626)
)

@Composable
fun SearxAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve distinct branded tech identity by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
