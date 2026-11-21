package com.mohamedabdelnaby.liquidglass.catalog.ui.theme

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.lerp
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.glass.moreContrasting
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.mohamedabdelnaby.liquidglass.catalog.glass.AndroidStyle
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalAndroidStyle

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

/**
 * Home's tint as the Material scheme's accent: primary, and the containers it tints (tonal buttons,
 * the selected segment, the navigation bar's indicator), with on-colors picked for contrast.
 * shortcut: a blend, not a full tonal palette from the tint as seed; use material-color-utilities
 * if the other roles should follow the tint too.
 */
private fun ColorScheme.tinted(tint: Color): ColorScheme {
    val primaryContainer = lerp(surface, tint, 0.3f)
    val secondaryContainer = lerp(surface, tint, 0.22f)
    return copy(
        primary = tint,
        onPrimary = moreContrasting(tint, surface, onSurface),
        primaryContainer = primaryContainer,
        onPrimaryContainer = moreContrasting(primaryContainer, surface, onSurface),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = moreContrasting(secondaryContainer, surface, onSurface),
    )
}

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
    val material = LocalAndroidStyle.current == AndroidStyle.Material
    val context = LocalContext.current
    CompositionLocalProvider(LocalConfiguration provides configuration) {
        if (material) {
            // Android's own colors (the wallpaper's on Android 12+, else Material's baseline) and type.
            // shortcut: MaterialExpressiveTheme and MotionScheme.expressive() are internal in material3
            // 1.4.0 (BOM 2026.09.00); use them once a BOM ships them public.
            val scheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                else -> if (dark) darkColorScheme() else lightColorScheme()
            }
            MaterialTheme(
                colorScheme = LocalGlassSettings.current.tint?.let { scheme.tinted(it.copy(alpha = 1f)) } ?: scheme,
                typography = MaterialTypography,
                content = content,
            )
        } else {
            MaterialTheme(
                colorScheme = if (dark) DarkColors else LightColors,
                typography = Typography,
                content = content,
            )
        }
    }
}
