package com.mohamedabdelnaby.liquidglass.catalog.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration

/** The user's preferred appearance for the whole app. */
enum class AppearanceMode(val title: String) { System("System"), Light("Light"), Dark("Dark") }

private val LightColors = lightColorScheme(
    primary = SystemColors.Blue,
    onPrimary = Color.White,
    secondary = SystemColors.Indigo,
    tertiary = SystemColors.Pink,
    background = Color(0xFFF2F2F7),
    surface = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0x993C3C43),
    outlineVariant = Color(0x333C3C43),
    error = SystemColors.Red,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    secondary = Color(0xFF5E5CE6),
    tertiary = Color(0xFFFF375F),
    background = Color.Black,
    surface = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    onSurface = Color.White,
    onSurfaceVariant = Color(0x99EBEBF5),
    outlineVariant = Color(0x33EBEBF5),
    error = Color(0xFFFF453A),
)

@Composable
fun LiquidGlassTheme(
    mode: AppearanceMode = AppearanceMode.System,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (mode) {
        AppearanceMode.System -> systemDark
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }
    // The glass and isSystemInDarkTheme() read uiMode from LocalConfiguration, so a forced
    // appearance rewrites it for everything below.
    val base = LocalConfiguration.current
    val configuration = remember(base, dark) {
        Configuration(base).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
    }
    CompositionLocalProvider(LocalConfiguration provides configuration) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = Typography,
            content = content,
        )
    }
}
