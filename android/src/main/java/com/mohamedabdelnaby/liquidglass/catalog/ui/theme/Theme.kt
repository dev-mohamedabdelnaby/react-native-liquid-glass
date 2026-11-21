package com.mohamedabdelnaby.liquidglass.catalog.ui.theme

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.lerp
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassSettings
import androidx.compose.ui.graphics.luminance
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

/** [color] blended toward white or black until its relative luminance is [target]. */
internal fun toLuminance(color: Color, target: Float): Color {
    val from = color.copy(alpha = 1f)
    val toward = if (from.luminance() < target) Color.White else Color.Black
    var lo = 0f
    var hi = 1f
    repeat(24) {
        val mid = (lo + hi) / 2f
        val reached = lerp(from, toward, mid).luminance().let { if (toward == Color.White) it >= target else it <= target }
        if (reached) hi = mid else lo = mid
    }
    return lerp(from, toward, hi)
}

/**
 * Home's tint as the scheme's primary roles, in tones like Material's (as relative luminance):
 * light mode darkens it until white reads on it (4.5:1); dark mode lifts it to a light tone with a
 * dark on-color. Containers, on-colors and the inverse take the tint's other tones. Secondary,
 * tertiary and the surfaces stay the system's.
 * shortcut: tones are blends toward white or black, not a full tonal palette (no
 * material-color-utilities); hue and chroma drift a little at the extremes.
 */
internal fun tintedScheme(base: ColorScheme, tint: Color, dark: Boolean): ColorScheme {
    val t = tint.copy(alpha = 1f)
    val primary = if (dark) {
        if (t.luminance() >= 0.5f) t else toLuminance(t, 0.5f)
    } else {
        if (t.luminance() <= 0.17f) t else toLuminance(t, 0.17f)
    }
    return base.copy(
        primary = primary,
        onPrimary = toLuminance(t, if (dark) 0.03f else 1f),
        primaryContainer = toLuminance(t, if (dark) 0.06f else 0.75f),
        onPrimaryContainer = toLuminance(t, if (dark) 0.75f else 0.012f),
        inversePrimary = toLuminance(t, if (dark) 0.11f else 0.5f),
        surfaceTint = primary,
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
            // Android's own colors (the wallpaper's on Android 12+, else Material's baseline), Home's
            // tint as the primary roles, and Material's type.
            val system = remember(dark, context) {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                    else -> if (dark) darkColorScheme() else lightColorScheme()
                }
            }
            val tint = LocalGlassSettings.current.tint
            val scheme = remember(system, tint, dark) { tint?.let { tintedScheme(system, it, dark) } ?: system }
            MaterialTheme(
                colorScheme = scheme,
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
