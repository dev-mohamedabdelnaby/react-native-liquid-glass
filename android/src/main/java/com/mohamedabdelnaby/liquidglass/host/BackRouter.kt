package com.mohamedabdelnaby.liquidglass.host

import androidx.activity.OnBackPressedDispatcher
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventInput

/**
 * The back key for one host's Compose `BackHandler`s (a cover, a search field, a detail panel).
 *
 * React Native's activity takes every back press to pop the native-stack, and its callback can sit
 * above the handlers a glass screen adds. So the handlers register here, on private dispatchers, and
 * one overlay-priority handler on the activity's dispatcher forwards the back key to them while any
 * of them is enabled. With none enabled, the back key goes on to React Native.
 */
internal class BackRouter {
  /** Where `BackHandler` registers: it prefers this one. */
  val navigation = NavigationEventDispatcher()

  private val relay = Relay()

  private val proxy = object : NavigationEventHandler<NavigationEventInfo.None>(NavigationEventInfo.None, false) {
    override fun onBackStarted(event: NavigationEvent) { if (relay.hasEnabled) relay.started(event) }
    override fun onBackProgressed(event: NavigationEvent) { if (relay.hasEnabled) relay.progressed(event) }
    override fun onBackCancelled() { if (relay.hasEnabled) relay.cancelled() }
    override fun onBackCompleted() = back()
  }

  /** A handler here takes the back key now. */
  val wantsBack: Boolean get() = proxy.isBackEnabled

  /** The back key, to the handler that takes it. */
  fun back() { if (relay.hasEnabled) relay.completed() else onBackPressed.onBackPressed() }

  /** For code that asks for an `OnBackPressedDispatcher` instead. */
  val onBackPressed = OnBackPressedDispatcher(null) { update() }

  private var attached: NavigationEventDispatcher? = null

  // Last: adding the input reports its state at once, to the fields above.
  init { navigation.addInput(relay) }

  /** The host entered a window whose back key [activity] dispatches. */
  fun attach(activity: NavigationEventDispatcher) {
    if (activity === attached) return
    detach()
    activity.addHandler(proxy, NavigationEventDispatcher.PRIORITY_OVERLAY)
    attached = activity
  }

  /** The host left its window: the screen under a push, or a dropped one. */
  fun detach() {
    if (attached == null) return
    proxy.remove()
    attached = null
  }

  private fun update() {
    proxy.isBackEnabled = relay.hasEnabled || onBackPressed.hasEnabledCallbacks()
  }

  private inner class Relay : NavigationEventInput() {
    var hasEnabled = false

    override fun onHasEnabledHandlersChanged(hasEnabledHandlers: Boolean) {
      hasEnabled = hasEnabledHandlers
      update()
    }

    fun started(event: NavigationEvent) = dispatchOnBackStarted(event)
    fun progressed(event: NavigationEvent) = dispatchOnBackProgressed(event)
    fun cancelled() = dispatchOnBackCancelled()
    fun completed() = dispatchOnBackCompleted()
  }
}
