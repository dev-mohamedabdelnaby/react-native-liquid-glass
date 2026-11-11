package com.mohamedabdelnaby.liquidglass

import androidx.compose.ui.graphics.Color
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The values the hosted screen trades with JS. */
class LiquidGlassScreenViewTest {
  @Test
  fun coloursAreRgbaHexOrEmpty() {
    assertEquals("#007AFF59", Color(0x59007AFF).toRgba())
    assertEquals("#FF9500FF", Color(0xFFFF9500).toRgba())
    assertEquals("", (null as Color?).toRgba())
  }

  @Test
  fun appearanceNames() {
    assertEquals(AppearanceMode.Dark, appearanceMode("dark"))
    assertEquals(AppearanceMode.Light, appearanceMode("light"))
    assertEquals(AppearanceMode.System, appearanceMode("system"))
    assertEquals(AppearanceMode.System, appearanceMode(null))
    assertEquals("dark", AppearanceMode.Dark.name.lowercase())
  }

  @Test
  fun unknownScreenIsNull() {
    assertNull(screenComponent("home"))
    assertNull(screenComponent("nope"))
  }
}
