package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Glass captions sit on a photo of any colour: they keep 4.5:1 (WCAG AA) over its darkest, lightest and mid pixels. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CaptionContrastTest {
  @get:Rule val compose = createComposeRule()

  private fun contrast(a: Color, b: Color): Float {
    val (lo, hi) = listOf(a.luminance(), b.luminance()).sorted()
    return (hi + 0.05f) / (lo + 0.05f)
  }

  @Test
  fun glassCaptionsReadOverAnyPhotoInLightAndDark() {
    val colors = mutableMapOf<AppearanceMode, Pair<Color, Color>>()
    compose.setContent {
      CompositionLocalProvider(LocalAndroidStyle provides AndroidStyle.Glass) {
        listOf(AppearanceMode.Light, AppearanceMode.Dark).forEach { mode ->
          LiquidGlassTheme(mode) { colors[mode] = captionColors() }
        }
      }
    }
    compose.waitForIdle()
    for ((mode, pair) in colors) {
      val (text, plate) = pair
      for (photo in listOf(Color.Black, Color.White, Color(0xFF808080))) {
        val under = plate.compositeOver(photo)
        val ratio = contrast(text.compositeOver(under), under)
        assertTrue("$mode over $photo: $ratio", ratio >= 4.5f)
      }
    }
  }
}
