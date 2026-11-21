package com.mohamedabdelnaby.liquidglass.catalog.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Home's tint as the Material scheme's primary roles, per mode. */
class TintSchemeTest {
  private val tints = listOf(Color(0xFF007AFF), Color(0xFFAF52DE), Color(0xFFFFCC00), Color(0xFF34C759), Color(0xFF1C1C1E))

  private fun contrast(a: Color, b: Color): Float {
    val hi = maxOf(a.luminance(), b.luminance())
    val lo = minOf(a.luminance(), b.luminance())
    return (hi + 0.05f) / (lo + 0.05f)
  }

  @Test
  fun darkPrimaryIsTheTintLifted() {
    for (tint in tints) {
      val primary = tintedScheme(darkColorScheme(), tint, dark = true).primary
      assertTrue("$tint", primary.luminance() >= tint.luminance() - 0.001f)
    }
    // Blue and purple are dark tints: lifted to a light tone.
    for (tint in tints.take(2)) assertTrue(tintedScheme(darkColorScheme(), tint, dark = true).primary.luminance() > tint.luminance() + 0.1f)
  }

  @Test
  fun pairsReadInBothModes() {
    for (tint in tints) for (dark in listOf(false, true)) {
      val s = tintedScheme(if (dark) darkColorScheme() else lightColorScheme(), tint, dark)
      assertTrue("$tint dark=$dark primary", contrast(s.primary, s.onPrimary) >= 4.5f)
      assertTrue("$tint dark=$dark container", contrast(s.primaryContainer, s.onPrimaryContainer) >= 4.5f)
    }
  }

  @Test
  fun secondaryAndTertiaryStayTheSystems() {
    val base = darkColorScheme()
    val s = tintedScheme(base, Color(0xFFAF52DE), dark = true)
    assertEquals(base.secondary, s.secondary)
    assertEquals(base.secondaryContainer, s.secondaryContainer)
    assertEquals(base.tertiary, s.tertiary)
    assertEquals(s.primary, s.surfaceTint)
  }

  @Test
  fun toLuminanceReachesTheTarget() {
    assertEquals(0.5f, toLuminance(Color(0xFF007AFF), 0.5f).luminance(), 0.01f)
    assertEquals(0.05f, toLuminance(Color(0xFFFFCC00), 0.05f).luminance(), 0.01f)
  }
}
