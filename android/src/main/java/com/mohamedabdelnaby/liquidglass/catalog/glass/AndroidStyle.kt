package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.staticCompositionLocalOf

/** How the screens draw on Android: Liquid Glass, or Android's own Material 3 components. */
enum class AndroidStyle { Glass, Material }

/** The style every building block follows. Provided by the host. */
val LocalAndroidStyle = staticCompositionLocalOf { AndroidStyle.Glass }

/** True when the building blocks draw their Material 3 counterparts. */
internal val isMaterial: Boolean @Composable get() = LocalAndroidStyle.current == AndroidStyle.Material

/**
 * A screen text's style: its iOS size (and line height) in glass, exactly as `fontSize =` gave it,
 * and a role of Material's type scale in Material.
 */
@Composable
fun platformTextStyle(iosSize: TextUnit, material: TextStyle, iosLineHeight: TextUnit = TextUnit.Unspecified): TextStyle =
    if (isMaterial) material else LocalTextStyle.current.merge(TextStyle(fontSize = iosSize, lineHeight = iosLineHeight))

/** Copy that names glass or iOS, and its Android wording in Material. */
@Composable
fun platformCopy(glass: String, material: String): String = if (isMaterial) material else glass
