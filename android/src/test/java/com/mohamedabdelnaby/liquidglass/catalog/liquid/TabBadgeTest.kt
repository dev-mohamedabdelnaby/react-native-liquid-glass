package com.mohamedabdelnaby.liquidglass.catalog.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The tab badge stays an 18 dp circle for one digit, at any font scale, as UITabBar's badge does. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabBadgeTest {
  @get:Rule val compose = createComposeRule()

  @Test
  fun aOneDigitBadgeIsAnEighteenDpCircleWhoseCountFitsAtTheLargestFontScale() {
    compose.setContent {
      CompositionLocalProvider(LocalDensity provides Density(3f, fontScale = 2f)) {
        Box { TabBadge(3, Color.Red, Modifier.testTag("badge")) }
      }
    }
    val badge = compose.onNodeWithTag("badge").getUnclippedBoundsInRoot()
    assertEquals(18.dp, badge.width)
    assertEquals(18.dp, badge.height)
    // The count does not grow with the font scale, so its text is not cut off.
    val layout = mutableListOf<TextLayoutResult>()
    compose.onNodeWithText("3").fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(layout)
    assertFalse("count taller than the badge: ${layout[0].multiParagraph.height} px", layout[0].didOverflowHeight)
    // 11 dp, as at font scale 1.
    assertEquals(11f, layout[0].layoutInput.style.fontSize.value * layout[0].layoutInput.density.fontScale, 0.1f)
  }
}
