package com.mohamedabdelnaby.liquidglass.host

import android.view.Window
import androidx.core.view.WindowCompat
import java.util.WeakHashMap

/**
 * What mounted screens ask of one window setting: the latest request wins; with none left, the
 * window gets back the value it had before the first one.
 */
internal class SharedRequests<T>(private val original: T) {
  private val requests = mutableListOf<Pair<Any, T>>()

  val isEmpty: Boolean get() = requests.isEmpty()

  /** [host] asks for [value]; returns the value to show. */
  fun claim(host: Any, value: T): T {
    requests.removeAll { it.first === host }
    requests += host to value
    return value
  }

  /** [host] no longer asks; returns the value to show. */
  fun release(host: Any): T {
    requests.removeAll { it.first === host }
    return requests.lastOrNull()?.second ?: original
  }
}

/**
 * Status and navigation bar icons follow the screen's appearance, as in the Android app's
 * `MainActivity`: dark icons in light appearance. When the last screen leaves, the window's own
 * icons (an app's `StatusBar` style) come back. The iOS host does the same with the window style.
 */
internal object SystemBarIcons {
  // Light icons for (status bar, navigation bar) are the controller's `isAppearanceLight…` = false.
  private val windows = WeakHashMap<Window, SharedRequests<Pair<Boolean, Boolean>>>()

  fun claim(host: Any, window: Window, dark: Boolean) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    val requests = windows.getOrPut(window) {
      SharedRequests(controller.isAppearanceLightStatusBars to controller.isAppearanceLightNavigationBars)
    }
    show(window, requests.claim(host, !dark to !dark))
  }

  fun release(host: Any, window: Window) {
    val requests = windows[window] ?: return
    show(window, requests.release(host))
    if (requests.isEmpty) windows.remove(window)
  }

  private fun show(window: Window, lightBars: Pair<Boolean, Boolean>) {
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = lightBars.first
      isAppearanceLightNavigationBars = lightBars.second
    }
  }
}
