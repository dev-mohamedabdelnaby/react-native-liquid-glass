package com.mohamedabdelnaby.liquidglass

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** `glassIcon` drawn by `Icon()`: the custom icon, tinted, at the icon's size, mirrored where the native one is. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GlassIconRenderTest {
  @get:Rule val compose = createComposeRule()

  // Fills only the left half: drawn, it is opaque on the left and clear on the right; mirrored, the reverse.
  private val leftHalf = GlassIcon.Svg("""<svg viewBox="0 0 10 10" xmlns="http://www.w3.org/2000/svg"><rect width="5" height="10"/></svg>""")

  private fun icon(icons: Map<String, GlassIcon>, name: String, fallback: ImageVector, rtl: Boolean = false) {
    compose.setContent {
      CompositionLocalProvider(
        LocalGlassIcons provides icons,
        LocalDensity provides Density(1f),
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
      ) {
        Box { Icon(glassIcon(name, fallback), null, Modifier.testTag("icon"), tint = Color.Red) }
      }
    }
  }

  private fun shot(): ImageBitmap = compose.onNodeWithTag("icon").captureToImage()

  /** The pixel at ([x], 12) of the 24 × 24 icon is the tint. */
  private fun ImageBitmap.redAt(x: Int) = toPixelMap()[x, 12].let { it.alpha > 0.9f && it.red > 0.9f && it.green < 0.1f }

  // The SVG parses off the main thread: wait for it to replace the native icon.
  private fun awaitCustom(leftOpaque: Boolean) =
    compose.waitUntil(5_000) { shot().let { it.redAt(if (leftOpaque) 5 else 18) && !it.redAt(if (leftOpaque) 18 else 5) } }

  @Test
  fun aCustomIconIsDrawnTintedAtTheIconSize() {
    icon(mapOf("heart.fill" to leftHalf), "heart.fill", Icons.Filled.Favorite)
    awaitCustom(leftOpaque = true)
    val image = shot()
    assertEquals(24, image.width)
    assertEquals(24, image.height)
  }

  @Test
  fun anUnknownNameIsTheNativeIcon() {
    icon(mapOf("heart.fill" to leftHalf), "trash", Icons.Filled.Favorite)
    compose.waitForIdle()
    // The heart is filled on both sides of its middle.
    val image = shot()
    assertEquals(true, image.redAt(7) && image.redAt(17))
  }

  @Test
  fun aCustomIconMirrorsInRtlWhereTheNativeOneDoes() {
    icon(mapOf("chevron.backward" to leftHalf), "chevron.backward", Icons.AutoMirrored.Filled.ArrowBack, rtl = true)
    awaitCustom(leftOpaque = false)
  }

  @Test
  fun aCustomIconKeepsItsSideInRtlWhereTheNativeOneDoesNot() {
    icon(mapOf("heart.fill" to leftHalf), "heart.fill", Icons.Filled.Favorite, rtl = true)
    awaitCustom(leftOpaque = true)
  }
}
