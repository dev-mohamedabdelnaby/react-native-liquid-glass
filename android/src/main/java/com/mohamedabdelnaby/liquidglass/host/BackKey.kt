package com.mohamedabdelnaby.liquidglass.host

import android.view.KeyEvent
import android.view.Window
import java.util.WeakHashMap

/**
 * The back key as a key event, the way Android before 16 (and apps not on predictive back) deliver
 * it: it goes to `Activity.onBackPressed`, which React Native's activity answers itself, never
 * asking the activity's back dispatcher. So the window's callback is wrapped once, and a back key
 * goes to the latest attached [BackRouter] that wants it. With predictive back the key never comes
 * this way: [BackRouter]'s handler on the activity's dispatcher gets it instead.
 */
internal object BackKey {
  private val windows = WeakHashMap<Window, Callback>()

  fun add(window: Window, router: BackRouter) {
    windows.getOrPut(window) { Callback(window.callback).also { window.callback = it } }.routers += router
  }

  fun remove(window: Window, router: BackRouter) {
    windows[window]?.routers?.remove(router)
  }

  // Stays installed when no router is left: it then only passes events on.
  private class Callback(private val app: Window.Callback) : Window.Callback by app {
    val routers = mutableListOf<BackRouter>()

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
      val router = routers.lastOrNull { it.wantsBack }
      if (event.keyCode != KeyEvent.KEYCODE_BACK || router == null) return app.dispatchKeyEvent(event)
      if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) router.back()
      return true
    }
  }
}
