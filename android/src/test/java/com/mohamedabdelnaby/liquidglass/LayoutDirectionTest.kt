package com.mohamedabdelnaby.liquidglass

import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Test

/** The JS `layoutDirection` prop as the hosted screen's Compose layout direction. */
class LayoutDirectionTest {
  @Test
  fun forcedDirectionsIgnoreTheSystem() {
    assertEquals(LayoutDirection.Rtl, layoutDirectionOf("rtl", false))
    assertEquals(LayoutDirection.Ltr, layoutDirectionOf("ltr", true))
  }

  @Test
  fun systemFollowsTheDevice() {
    assertEquals(LayoutDirection.Rtl, layoutDirectionOf("system", true))
    assertEquals(LayoutDirection.Ltr, layoutDirectionOf("system", false))
  }

  @Test
  fun unknownIsLtr() {
    assertEquals(LayoutDirection.Ltr, layoutDirectionOf("bogus", false))
  }
}
