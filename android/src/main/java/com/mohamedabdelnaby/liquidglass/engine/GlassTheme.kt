package com.mohamedabdelnaby.liquidglass.engine

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp

// The catalog app's ui/theme: Apple system colours and iOS text styles on the Material roles.

internal val LightGlassColors = lightColorScheme(
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

internal val DarkGlassColors = darkColorScheme(
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

/** The scheme for the system appearance; the hosts have no app theme of their own. */
internal fun glassColors(dark: Boolean): ColorScheme = if (dark) DarkGlassColors else LightGlassColors

private fun ios(size: Int, line: Int, tracking: Double, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = TextUnit(tracking.toFloat(), TextUnitType.Sp),
    fontWeight = weight,
)

internal val GlassTypography = Typography(
    displaySmall = ios(34, 41, 0.4, FontWeight.Bold),
    headlineLarge = ios(34, 41, 0.4, FontWeight.Bold),
    headlineMedium = ios(28, 34, 0.38),
    headlineSmall = ios(22, 28, -0.26),
    titleLarge = ios(20, 25, -0.45),
    titleMedium = ios(17, 22, -0.43, FontWeight.SemiBold),
    titleSmall = ios(15, 20, -0.23, FontWeight.SemiBold),
    bodyLarge = ios(17, 22, -0.43),
    bodyMedium = ios(15, 20, -0.23),
    bodySmall = ios(13, 18, -0.08),
    labelLarge = ios(15, 20, -0.23, FontWeight.Medium),
    labelMedium = ios(12, 16, 0.0),
    labelSmall = ios(11, 13, 0.06),
)

/** The catalog's look for glass hosted outside it, following the system light/dark setting. */
@Composable
fun GlassTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = glassColors(isSystemInDarkTheme()), typography = GlassTypography, content = content)
}
