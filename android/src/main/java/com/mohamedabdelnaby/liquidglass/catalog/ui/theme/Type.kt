package com.mohamedabdelnaby.liquidglass.catalog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun ios(size: Int, line: Int, tracking: Double, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = TextUnit(tracking.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp),
    fontWeight = weight,
    // As SwiftUI: a paragraph reads in its own script's direction, so English in RTL keeps its period last.
    textDirection = TextDirection.Content,
)

/** iOS 26 text styles (SF sizes and tracking) mapped onto the Material roles the screens use. */
val Typography = Typography(
    displaySmall = ios(34, 41, 0.4, FontWeight.Bold), // largeTitle
    headlineLarge = ios(34, 41, 0.4, FontWeight.Bold), // largeTitle
    headlineMedium = ios(28, 34, 0.38), // title
    headlineSmall = ios(22, 28, -0.26), // title2
    titleLarge = ios(20, 25, -0.45), // title3
    titleMedium = ios(17, 22, -0.43, FontWeight.SemiBold), // headline
    titleSmall = ios(15, 20, -0.23, FontWeight.SemiBold),
    bodyLarge = ios(17, 22, -0.43), // body
    bodyMedium = ios(15, 20, -0.23), // subheadline
    bodySmall = ios(13, 18, -0.08), // footnote
    labelLarge = ios(15, 20, -0.23, FontWeight.Medium),
    labelMedium = ios(12, 16, 0.0), // caption
    labelSmall = ios(11, 13, 0.06), // caption2
)

/** Material 3's own type scale (Android's Roboto sizes), each paragraph in its own script's direction. */
val MaterialTypography = Typography().run {
    fun TextStyle.content() = copy(textDirection = TextDirection.Content)
    Typography(
        displayLarge.content(), displayMedium.content(), displaySmall.content(),
        headlineLarge.content(), headlineMedium.content(), headlineSmall.content(),
        titleLarge.content(), titleMedium.content(), titleSmall.content(),
        bodyLarge.content(), bodyMedium.content(), bodySmall.content(),
        labelLarge.content(), labelMedium.content(), labelSmall.content(),
    )
}

/** Every style of the scale in [family]; null keeps the scale as it is. */
fun Typography.withFontFamily(family: FontFamily?): Typography = if (family == null) this else {
    fun TextStyle.f() = copy(fontFamily = family)
    Typography(
        displayLarge.f(), displayMedium.f(), displaySmall.f(),
        headlineLarge.f(), headlineMedium.f(), headlineSmall.f(),
        titleLarge.f(), titleMedium.f(), titleSmall.f(),
        bodyLarge.f(), bodyMedium.f(), bodySmall.f(),
        labelLarge.f(), labelMedium.f(), labelSmall.f(),
    )
}
