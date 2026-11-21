package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/** The Material slider's haptic steps and value label, and the contrast pick for tinted controls. */
class MaterialControlsTest {
  @Test
  fun detentsAreTenthsOfTheRange() {
    assertEquals(0, sliderDetent(0f, 0f..1f))
    assertEquals(0, sliderDetent(0.09f, 0f..1f))
    assertEquals(1, sliderDetent(0.1f, 0f..1f))
    assertEquals(3, sliderDetent(12f, 0f..40f))
    assertEquals(10, sliderDetent(40f, 0f..40f))
    // Outside the range or an empty range: no new detent.
    assertEquals(10, sliderDetent(50f, 0f..40f))
    assertEquals(0, sliderDetent(3f, 3f..3f))
  }

  @Test
  fun labelIsPercentForAFractionElseTheNumber() {
    assertEquals("25%", sliderLabel(0.25f, 0f..1f))
    assertEquals("14", sliderLabel(14.2f, 0f..40f))
  }

  @Test
  fun contentTakesTheMoreContrastingColor() {
    val dark = Color(0xFF1C1B1F)
    val light = Color(0xFFFFFBFE)
    assertEquals(dark, moreContrasting(Color(0xFFFFCC00), light, dark))
    assertEquals(light, moreContrasting(Color(0xFF3A0CA3), light, dark))
  }
}
