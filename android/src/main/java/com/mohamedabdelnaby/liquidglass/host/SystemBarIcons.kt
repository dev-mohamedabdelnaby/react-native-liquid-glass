package com.mohamedabdelnaby.liquidglass.host

import android.view.Window
import androidx.core.view.WindowCompat
import java.util.WeakHashMap

/**
 * What mounted screens ask of one window setting: the latest request wins; with none left, the
 * window gets back its own value. A value the app wrote since this last wrote one (an app's
 * `StatusBar` style) is the app's: a claim keeps it as the one to restore, a release leaves it.
 */
internal class SharedRequests<T>(private var original: T) {
  private val requests = mutableListOf<Pair<Any, T>>()
  private var written: T? = null

  val isEmpty: Boolean get() = requests.isEmpty()

  /** [host] asks for [value] while the window shows [current]; returns the value to write, if any. */
  fun claim(host: Any, value: T, current: T): T? {
    if (written != null && current != written) original = current
    // The same value again keeps this host's place.
    if (requests.any { it.first === host && it.second == value }) return null
    requests.removeAll { it.first === host }
    requests += host to value
    written = value
    return value
  }

  /** [host] no longer asks while the window shows [current]; returns the value to write, if any. */
  fun release(host: Any, current: T): T? {
    if (requests.none { it.first === host }) return null
    requests.removeAll { it.first === host }
    if (current != written) {
      // The app wrote its own value since: it stays, and the requests made before it end.
      requests.clear()
      return null
    }
    return (requests.lastOrNull()?.second ?: original).also { written = it }
  }
}

/**
 * One screen's request for its window's bar icons, kept as the iOS host keeps its window style:
 * made when the screen first enters a window, moved only to another window, and ended when the
 * screen leaves the React tree. Leaving the window alone (a native-stack push, a cover) keeps it.
 */
internal class BarsRequest<W : Any>(
  private val claim: (window: W, dark: Boolean) -> Unit,
  private val release: (window: W) -> Unit,
) {
  private var window: W? = null
  private var dark: Boolean? = null

  /** The screen's appearance: once composed, and again whenever it changes. */
  fun appearance(dark: Boolean) {
    if (dark == this.dark) return
    this.dark = dark
    window?.let { claim(it, dark) }
  }

  /** The screen entered [window]. */
  fun entered(window: W) {
    if (window === this.window) return
    left()
    this.window = window
    dark?.let { claim(window, it) }
  }

  /** The screen left the React tree. */
  fun left() {
    window?.let(release)
    window = null
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
    val current = shown(window)
    val requests = windows.getOrPut(window) { SharedRequests(current) }
    requests.claim(host, !dark to !dark, current)?.let { show(window, it) }
  }

  fun release(host: Any, window: Window) {
    val requests = windows[window] ?: return
    requests.release(host, shown(window))?.let { show(window, it) }
    if (requests.isEmpty) windows.remove(window)
  }

  private fun shown(window: Window): Pair<Boolean, Boolean> =
    WindowCompat.getInsetsController(window, window.decorView).run { isAppearanceLightStatusBars to isAppearanceLightNavigationBars }

  private fun show(window: Window, lightBars: Pair<Boolean, Boolean>) {
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = lightBars.first
      isAppearanceLightNavigationBars = lightBars.second
    }
  }
}
