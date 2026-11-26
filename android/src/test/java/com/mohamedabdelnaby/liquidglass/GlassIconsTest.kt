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
    val icons = glassIconsOf("""{"heart":{"svg":"<svg/>"},"bad":{"size":3},"worse":4,"number":{"svg":5},"glyphless":{"glyph":1,"fontFamily":"Ionicons"}}""")
    assertNull(icons["trash"])
    assertNull(icons["bad"])
    assertNull(icons["worse"])
    // Values that are not strings: skipped, as on the device.
    assertNull(icons["number"])
    assertNull(icons["glyphless"])
    assertEquals(setOf("heart"), icons.keys)
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

  @Test
  fun imagesDecodeAFewTimesTheIconSizeAtMost() {
    // 96 dp at 3x is 288 px: a 4000 px photo decodes at 1/8 (500 px), never below 288.
    assertEquals(8, sampleSize(4000, 3000, 288))
    assertEquals(1, sampleSize(96, 96, 288))
    assertEquals(1, sampleSize(500, 500, 288))
    assertEquals(2, sampleSize(600, 100, 288))
  }
}
