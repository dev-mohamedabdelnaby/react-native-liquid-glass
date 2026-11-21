package com.mohamedabdelnaby.liquidglass

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The `font*` props as the screens' font family: one face per weight, loaded by name. */
class FontFamilyTest {
  private fun faces(family: FontFamily?) =
    (family as FontListFontFamily).fonts.map { (it as NamedFont).name to it.weight }

  @Test
  fun eachWeightTakesItsFace() {
    val family = fontFamilyOf(FontFaces("R", "M", "S", "B", "H")) { _, _ -> null }
    assertEquals(
      listOf("R" to FontWeight.W400, "M" to FontWeight.W500, "S" to FontWeight.W600, "B" to FontWeight.W700, "H" to FontWeight.W800),
      faces(family),
    )
  }

  @Test
  fun missingWeightsFallBackToRegular() {
    val family = fontFamilyOf(FontFaces(regular = "R", bold = "B")) { _, _ -> null }
    assertEquals(
      listOf("R" to FontWeight.W400, "R" to FontWeight.W500, "R" to FontWeight.W600, "B" to FontWeight.W700, "R" to FontWeight.W800),
      faces(family),
    )
    // Without a regular face, the weights that are set are all there is.
    assertEquals(listOf("B" to FontWeight.W700), faces(fontFamilyOf(FontFaces(bold = "B")) { _, _ -> null }))
  }

  @Test
  fun noFaceIsTheSystemFont() {
    assertNull(fontFamilyOf(FontFaces()) { _, _ -> error("nothing to load") })
  }

  private fun loads(faces: FontFaces): List<Pair<String, FontWeight>> {
    val loaded = mutableListOf<Pair<String, FontWeight>>()
    val family = fontFamilyOf(faces) { name, weight -> loaded += name to weight; null }
    (family as FontListFontFamily).fonts.forEach { (it as NamedFont).load() }
    return loaded
  }

  // As React Native: a map's faces are fonts of their own (no fontWeight), one name is a family (with fontWeight).
  @Test
  fun mapFacesLoadAtNormalWeightAndAFamilyAtEachWeight() {
    assertEquals(listOf("R", "R", "R", "B", "R").map { it to FontWeight.Normal }, loads(FontFaces(regular = "R", bold = "B")))
    assertEquals(
      listOf(FontWeight.W400, FontWeight.W500, FontWeight.W600, FontWeight.W700, FontWeight.W800).map { "Cairo" to it },
      loads(FontFaces("Cairo", "Cairo", "Cairo", "Cairo", "Cairo")),
    )
  }
}
