package com.mohamedabdelnaby.liquidglass.host

import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import org.junit.Assert.assertEquals
import org.junit.Test

/** The back key reaches a glass screen's handlers before React Native's, whatever the order. */
class BackRouterTest {
  private class Counter(enabled: Boolean) : NavigationEventHandler<NavigationEventInfo.None>(NavigationEventInfo.None, enabled) {
    var backs = 0
    override fun onBackCompleted() { backs++ }
  }

  private val activity = NavigationEventDispatcher()
  private val key = DirectNavigationEventInput().also { activity.addInput(it) }
  private val router = BackRouter()
  private val reactNative = Counter(true)
  private val cover = Counter(true)

  @Test
  fun anEnabledHandlerTakesTheKeyEvenWhenReactNativeRegisteredLater() {
    router.attach(activity)
    router.navigation.addHandler(cover)
    // React Native's callback is re-added on top at every activity start.
    activity.addHandler(reactNative)

    key.backCompleted()
    assertEquals(1, cover.backs)
    assertEquals(0, reactNative.backs)

    // The cover closed: its handler is off, so back pops the screen.
    cover.isBackEnabled = false
    key.backCompleted()
    assertEquals(1, cover.backs)
    assertEquals(1, reactNative.backs)
  }

  @Test
  fun aDetachedHostLetsTheKeyThrough() {
    activity.addHandler(reactNative)
    router.attach(activity)
    router.navigation.addHandler(cover)
    router.detach()

    key.backCompleted()
    assertEquals(0, cover.backs)
    assertEquals(1, reactNative.backs)
  }

  @Test
  fun theKeyEventPathAsksTheRouterDirectly() {
    // Before predictive back the key never reaches the activity's dispatcher; BackKey asks the router.
    assertEquals(false, router.wantsBack)
    router.navigation.addHandler(cover)
    assertEquals(true, router.wantsBack)
    router.back()
    assertEquals(1, cover.backs)
    cover.isBackEnabled = false
    assertEquals(false, router.wantsBack)
  }
}
