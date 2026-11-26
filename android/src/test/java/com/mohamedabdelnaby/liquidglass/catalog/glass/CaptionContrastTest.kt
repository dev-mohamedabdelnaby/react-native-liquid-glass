package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Glass captions on a photo of any colour keep 4.5:1 (WCAG AA) over its darkest, lightest and mid pixels;
 * on a plain page they stay the theme's onSurfaceVariant with no plate.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CaptionContrastTest {
  @get:Rule val compose = createComposeRule()

  private fun contrast(a: Color, b: Color): Float {
    val (lo, hi) = listOf(a.luminance(), b.luminance()).sorted()
    return (hi + 0.05f) / (lo + 0.05f)
  }

  /** Each mode's caption colours, and the theme's onSurfaceVariant. */
  private fun captions(onPhoto: Boolean): Map<AppearanceMode, Triple<Color, Color, Color>> {
    val colors = mutableMapOf<AppearanceMode, Triple<Color, Color, Color>>()
    compose.setContent {
      CompositionLocalProvider(LocalAndroidStyle provides AndroidStyle.Glass, LocalOnPhoto provides onPhoto) {
        listOf(AppearanceMode.Light, AppearanceMode.Dark).forEach { mode ->
          LiquidGlassTheme(mode) {
            val (text, plate) = captionColors()
            colors[mode] = Triple(text, plate, MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
    compose.waitForIdle()
    assertEquals(2, colors.size)
    return colors
  }

  @Test
  fun glassCaptionsOnAPlainPageKeepTheThemeGreyWithNoPlate() {
    for ((mode, colors) in captions(onPhoto = false)) {
      assertEquals("$mode text", colors.third, colors.first)
      assertEquals("$mode plate", Color.Transparent, colors.second)
    }
  }

  @Test
  fun glassCaptionsReadOverAnyPhotoInLightAndDark() {
    for ((mode, colors) in captions(onPhoto = true)) {
      val (text, plate) = colors.first to colors.second
      for (photo in listOf(Color.Black, Color.White, Color(0xFF808080))) {
        val under = plate.compositeOver(photo)
        val ratio = contrast(text.compositeOver(under), under)
        assertTrue("$mode over $photo: $ratio", ratio >= 4.5f)
      }
    }
  }
}
