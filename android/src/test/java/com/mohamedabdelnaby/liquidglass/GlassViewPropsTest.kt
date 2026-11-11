package com.mohamedabdelnaby.liquidglass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.engine.glass.CapsuleShape
import com.mohamedabdelnaby.liquidglass.engine.glass.Glass
import com.mohamedabdelnaby.liquidglass.engine.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.engine.glass.styled
import androidx.compose.foundation.shape.RoundedCornerShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassViewPropsTest {
  @Test
  fun styleIsTakenAsGiven() {
    val (glass, settings) = GlassViewProps(glassStyle = "regular").toEngine()
    // The settings default to clear; JS already resolved the style, so the glass keeps its own.
    assertEquals(Glass.Variant.Regular, glass.styled(settings).variant)
    val (clear, clearSettings) = GlassViewProps(glassStyle = "clear").toEngine()
    assertEquals(Glass.Variant.Clear, clear.styled(clearSettings).variant)
  }

  @Test
  fun valuesReachTheEngine() {
    val (glass, settings) = GlassViewProps(
      blur = 8f, white = 0.4f, opaque = true, liquid = false, tintColor = 0xFFFF0000.toInt(),
      refractionHeight = 5f, refractionAmount = 7f,
    ).toEngine()
    val s = glass.styled(settings)
    assertEquals(8.dp, s.blur)
    assertEquals(0.4f, s.white)
    assertTrue(s.opaque)
    assertFalse(s.liquid)
    assertEquals(Color.Red, s.tint)
    assertEquals(5.dp, s.refractionHeight)
    assertEquals(7.dp, s.refractionAmount)
  }

  @Test
  fun shapes() {
    assertEquals(CapsuleShape, GlassViewProps(shapeKind = "capsule").shape())
    assertEquals(CapsuleShape, GlassViewProps(shapeKind = "circle").shape())
    assertEquals(RoundedCornerShape(12.dp), GlassViewProps(shapeKind = "rect", cornerRadius = 12f).shape())
  }

  @Test
  fun iconOnlyButtonIsACircle() {
    assertSame(GlassCircle, GlassButtonProps(androidIcon = "add").shape())
    assertSame(GlassCircle, GlassButtonProps(title = "", androidIcon = "add").shape())
    assertEquals(CapsuleShape, GlassButtonProps(title = "Done").shape())
  }
}
