package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.LayoutDirection
import com.mohamedabdelnaby.liquidglass.catalog.glass.AndroidStyle
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalAndroidStyle
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** QA regressions on whole catalog screens, in both Android styles. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CatalogScreensTest {
  @get:Rule val compose = createComposeRule()

  private fun show(style: AndroidStyle, direction: LayoutDirection = LayoutDirection.Ltr, screen: @Composable () -> Unit) =
    compose.setContent {
      CompositionLocalProvider(LocalAndroidStyle provides style, LocalLayoutDirection provides direction) {
        LiquidGlassTheme(AppearanceMode.Light) { screen() }
      }
    }

  // The lowest match: the top bar's back button is also "Back".
  private fun bounds(description: String) =
    compose.onAllNodesWithContentDescription(description).fetchSemanticsNodes().maxBy { it.boundsInRoot.top }.boundsInRoot
  private fun textBounds(text: String) = compose.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
  private fun leftOf(a: Rect, b: Rect, what: String) = assertTrue("$what: $a vs $b", a.left < b.left)

  // Issue 8: media controls follow time, not reading direction.
  private fun buttonsMediaRowStaysLtrInRtl(style: AndroidStyle) {
    show(style, LayoutDirection.Rtl) { ButtonsScreen() }
    leftOf(bounds("Play"), bounds("Pause"), "Play before Pause")
    leftOf(bounds("Pause"), bounds("Forward"), "Pause before Forward")
  }

  @Test fun glassButtonsMediaRowStaysLtrInRtl() = buttonsMediaRowStaysLtrInRtl(AndroidStyle.Glass)
  @Test fun materialButtonsMediaRowStaysLtrInRtl() = buttonsMediaRowStaysLtrInRtl(AndroidStyle.Material)

  // Ruling B and the media-player addition: the whole Recipes player keeps its LTR layout in RTL.
  private fun recipesPlayerStaysLtrInRtl(style: AndroidStyle) {
    show(style, LayoutDirection.Rtl) { RecipesScreen() }
    compose.onNodeWithContentDescription("Expand player").performScrollTo()
    leftOf(textBounds("Glass Animals"), bounds("Play"), "artwork and title before the play button")
    compose.onNodeWithContentDescription("Expand player").performClick()
    compose.mainClock.advanceTimeBy(2_000)
    leftOf(bounds("Back"), bounds("Shuffle"), "Back before Shuffle")
    leftOf(bounds("Shuffle"), bounds("Forward"), "Shuffle before Forward")
  }

  @Test fun glassRecipesPlayerStaysLtrInRtl() = recipesPlayerStaysLtrInRtl(AndroidStyle.Glass)
  @Test fun materialRecipesPlayerStaysLtrInRtl() = recipesPlayerStaysLtrInRtl(AndroidStyle.Material)

  // Issue 7: the floating action menu's caption stays clear of the + button, in LTR and RTL.
  private fun fabCaptionClearsTheButton(direction: LayoutDirection) {
    show(AndroidStyle.Glass, direction) { RecipesScreen() }
    val caption = compose.onNodeWithText("The + button", substring = true, useUnmergedTree = true)
    caption.performScrollTo()
    val text = caption.fetchSemanticsNode().boundsInRoot
    val fab = bounds("Open menu")
    val overlaps = text.left < fab.right && fab.left < text.right && text.top < fab.bottom && fab.top < text.bottom
    assertFalse("caption $text overlaps the button $fab", overlaps)
  }

  @Test fun fabCaptionClearsTheButtonInLtr() = fabCaptionClearsTheButton(LayoutDirection.Ltr)
  @Test fun fabCaptionClearsTheButtonInRtl() = fabCaptionClearsTheButton(LayoutDirection.Rtl)

  // Issues 3 and 4: Material's bar never minimizes, so it has no such switch; glass keeps it.
  @Test
  fun onlyGlassTabBarOffersMinimizeOnScroll() {
    show(AndroidStyle.Material) { TabBarScreen() }
    compose.onNodeWithText("Bar color", useUnmergedTree = true).assertExists()
    compose.onNodeWithText("Search field", useUnmergedTree = true).assertExists()
    compose.onNodeWithText("Minimize on scroll", useUnmergedTree = true).assertDoesNotExist()
  }

  @Test
  fun glassTabBarOffersMinimizeOnScroll() {
    show(AndroidStyle.Glass) { TabBarScreen() }
    compose.onNodeWithText("Minimize on scroll", useUnmergedTree = true).assertExists()
    compose.onNodeWithText("Bar color", useUnmergedTree = true).assertExists()
  }
}
