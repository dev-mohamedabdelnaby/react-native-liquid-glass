package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A glass switch row is one switch for TalkBack, named by its label, and its label toggles it (as Material rows). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SwitchRowTest {
  @get:Rule val compose = createComposeRule()

  private val isSwitch = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)

  @Test
  fun aGlassSwitchRowIsOneLabeledSwitchThatItsLabelToggles() {
    var on by mutableStateOf(false)
    compose.setContent { SwitchRow("Notifications", on) { on = it } }
    compose.onAllNodes(isSwitch).assertCountEquals(1)
    compose.onNode(isSwitch and hasText("Notifications")).assertIsOff()
    // Android's accessibility tree takes a node's own state and actions, not merged ones: the
    // row itself is the one toggleable node, around its label (the switch adds no node of its own).
    compose.onAllNodes(isToggleable(), useUnmergedTree = true).assertCountEquals(1)
    compose.onNode(isToggleable(), useUnmergedTree = true).assert(hasAnyDescendant(hasText("Notifications")))
    compose.onNodeWithText("Notifications", useUnmergedTree = true).performClick()
    assertTrue(on)
    compose.onNode(isSwitch and hasText("Notifications")).assertIsOn()
  }
}
