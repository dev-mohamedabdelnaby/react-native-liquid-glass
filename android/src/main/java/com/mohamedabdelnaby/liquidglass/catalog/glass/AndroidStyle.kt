package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** How the screens draw on Android: Liquid Glass, or Android's own Material 3 components. */
enum class AndroidStyle { Glass, Material }

/** The style every building block follows. Provided by the host. */
val LocalAndroidStyle = staticCompositionLocalOf { AndroidStyle.Glass }

/** True when the building blocks draw their Material 3 counterparts. */
internal val isMaterial: Boolean @Composable get() = LocalAndroidStyle.current == AndroidStyle.Material
