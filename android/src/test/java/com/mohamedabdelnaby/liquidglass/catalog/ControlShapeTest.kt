package com.mohamedabdelnaby.liquidglass.catalog

import com.mohamedabdelnaby.liquidglass.catalog.shapes.ControlShape
import com.mohamedabdelnaby.liquidglass.catalog.shapes.controlShapeOf
import com.mohamedabdelnaby.liquidglass.catalog.shapes.glowIntensityOf
import org.junit.Assert.assertEquals
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
}
