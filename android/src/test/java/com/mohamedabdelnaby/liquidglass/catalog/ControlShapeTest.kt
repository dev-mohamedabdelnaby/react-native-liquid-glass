package com.mohamedabdelnaby.liquidglass.catalog

import com.mohamedabdelnaby.liquidglass.catalog.shapes.ControlShape
import com.mohamedabdelnaby.liquidglass.catalog.shapes.controlShapeOf
import com.mohamedabdelnaby.liquidglass.catalog.shapes.glowIntensityOf
import com.facebook.react.bridge.JavaOnlyMap
import com.mohamedabdelnaby.liquidglass.catalog.glass.AndroidStyle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.shapes.sliderFraction
import com.mohamedabdelnaby.liquidglass.catalog.shapes.sliderKeyDelta
import com.mohamedabdelnaby.liquidglass.catalog.shapes.onWhite
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.putSettings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The JS `androidControlShape` and `androidGlowIntensity` props as the controls see them. */
class ControlShapeTest {
  @Test
  fun eachJsValueIsItsShape() {
    assertEquals(ControlShape.Pill, controlShapeOf("pill"))
    assertEquals(ControlShape.Labeled, controlShapeOf("labeled"))
    assertEquals(ControlShape.Glow, controlShapeOf("glow"))
  }

  @Test
  fun unknownAndRemovedShapesArePill() {
    for (value in listOf("default", "squared", "bogus", "")) assertEquals(value, ControlShape.Pill, controlShapeOf(value))
  }

  @Test
  fun glowIntensityIsClampedToZeroOne() {
    assertEquals(0.35f, glowIntensityOf(0.35f))
    assertEquals(0f, glowIntensityOf(-2f))
    assertEquals(1f, glowIntensityOf(7f))
    assertEquals(0.35f, glowIntensityOf(Float.NaN))
  }

  @Test
  fun homeEchoesShapeAndIntensityToJs() {
    val map = JavaOnlyMap().putSettings(AppearanceMode.System, GlassSettings(), "system", AndroidStyle.Material, ControlShape.Glow, 0.6f)
    assertEquals("glow", map.getString("androidControlShape"))
    assertEquals(0.6, map.getDouble("androidGlowIntensity"), 1e-6)
    assertEquals("material", map.getString("androidStyle"))
    assertFalse(map.hasKey("androidSegmentedCheck"))
  }

  @Test
  fun sliderTouchMirrorsInRtl() {
    // 200 px wide, 10 px handle radius: 180 px of travel.
    assertEquals(0f, sliderFraction(10f, 200f, 10f, rtl = false), 1e-6f)
    assertEquals(0.25f, sliderFraction(55f, 200f, 10f, rtl = false), 1e-6f)
    assertEquals(0.75f, sliderFraction(55f, 200f, 10f, rtl = true), 1e-6f)
    assertEquals(1f, sliderFraction(-50f, 200f, 10f, rtl = true), 1e-6f)
  }

  @Test
  fun sliderKeysFollowTheDirection() {
    assertEquals(0.01f, sliderKeyDelta(Key.DirectionRight, rtl = false))
    assertEquals(-0.01f, sliderKeyDelta(Key.DirectionRight, rtl = true))
    assertEquals(0.1f, sliderKeyDelta(Key.PageUp, rtl = true))
    assertEquals(1f, sliderKeyDelta(Key.MoveEnd, rtl = false))
    assertNull(sliderKeyDelta(Key.A, rtl = false))
  }

  @Test
  fun selectedTabTextOnWhiteIsAa() {
    // Text in the accent only while it reaches 4.5:1 on white.
    for (accent in listOf(Color(0xFF007AFF), Color(0xFF34C759), Color(0xFF5856D6), Color(0xFF8E8E93), Color(0xFF1C1B1F))) {
      val text = onWhite(accent)
      assertTrue("$accent -> $text", 1.05f / (text.luminance() + 0.05f) >= 4.5f)
    }
  }
}
