package com.mohamedabdelnaby.liquidglass

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The `icons` prop as icons, and where an icon's drawing goes in the place it takes. */
class GlassIconsTest {
  @Test
  fun jsonGivesEachKind() {
    val icons = glassIconsOf(
      """{"heart":{"glyph":"","fontFamily":"Ionicons"},"magnifyingglass":{"svg":"<svg/>"},"plus":{"uri":"file:///plus.png"}}""",
    )
    assertEquals(
      mapOf(
        "heart" to GlassIcon.Glyph("", "Ionicons"),
        "magnifyingglass" to GlassIcon.Svg("<svg/>"),
        "plus" to GlassIcon.Image("file:///plus.png"),
      ),
      icons,
    )
  }

  @Test
  fun unknownNamesAndBadEntriesHaveNoIcon() {
    val icons = glassIconsOf("""{"heart":{"svg":"<svg/>"},"bad":{"size":3},"worse":4}""")
    assertNull(icons["trash"])
    assertNull(icons["bad"])
    assertNull(icons["worse"])
    assertEquals(emptyMap<String, GlassIcon>(), glassIconsOf(""))
    assertEquals(emptyMap<String, GlassIcon>(), glassIconsOf("not json"))
  }

  @Test
  fun aGlyphTakesTheRequestedSizeAsAMaterialIconDoes() {
    // A square glyph in a 24 dp icon: 20 dp, 2 dp in from each edge, as Material draws its own.
    assertEquals(Rect(2f, 2f, 22f, 22f), iconRect(70f, 70f, 24f, 24f))
    // At 48 px: twice that.
    assertEquals(Rect(4f, 4f, 44f, 44f), iconRect(70f, 70f, 48f, 48f))
    // A wide glyph keeps its shape, centred.
    assertEquals(Rect(2f, 7f, 22f, 17f), iconRect(100f, 50f, 24f, 24f))
  }
}
