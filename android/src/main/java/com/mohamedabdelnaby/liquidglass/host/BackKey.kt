package com.mohamedabdelnaby.liquidglass.host

import android.view.KeyEvent
import android.view.Window
import java.lang.ref.WeakReference
import java.util.WeakHashMap

/**
 * The back key as a key event, the way Android before 16 (and apps not on predictive back) deliver
 * it: it goes to `Activity.onBackPressed`, which React Native's activity answers itself, never
 * asking the activity's back dispatcher. So the window's callback is wrapped, and a back key goes
 * to the latest attached [BackRouter] that wants it. With predictive back the key never comes this
 * way: [BackRouter]'s handler on the activity's dispatcher gets it instead.
 */
internal object BackKey {
  private val hooks = WindowHooks<Window, Callback>({ it.callback }) { window, previous ->
    Callback(window.callback).also { hook ->
      // The window's callback was replaced: its routers move to the new hook.
      previous?.let { hook.routers += it.routers; it.routers.clear() }
      window.callback = hook
    }
  }

  fun add(window: Window, router: BackRouter) {
    hooks.get(window).routers += router
  }

  fun remove(window: Window, router: BackRouter) {
    hooks.peek(window)?.routers?.remove(router)
  }

  // Stays installed when no router is left: it then only passes events on.
  private class Callback(private val app: Window.Callback) : Window.Callback by app {
    val routers = mutableListOf<BackRouter>()
    // The press's DOWN went to a router, so its UP is ours too.
    private var downTaken = false

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
      if (event.keyCode != KeyEvent.KEYCODE_BACK) return app.dispatchKeyEvent(event)
      val router = routers.lastOrNull { it.wantsBack }
      when (event.action) {
        KeyEvent.ACTION_DOWN -> {
          if (event.repeatCount == 0) downTaken = router != null
          if (downTaken) return true
        }
        KeyEvent.ACTION_UP -> if (downTaken) {
          downTaken = false
          if (event.isCanceled) return true
          if (router != null) {
            router.back()
            return true
          }
          // The handler went away since DOWN: the whole press goes to the activity instead.
          app.dispatchKeyEvent(KeyEvent.changeAction(event, KeyEvent.ACTION_DOWN))
        }
      }
      return app.dispatchKeyEvent(event)
    }
  }
}

/**
 * One hook per window, installed again when something replaced it. Held weakly both ways: the
 * window holds its hook (as its callback), and the hook holds the app's callback, which holds the
 * window, so a strong value would keep every recreated activity alive.
 */
internal class WindowHooks<W : Any, H : Any>(
  private val installed: (W) -> Any?,
  private val install: (window: W, previous: H?) -> H,
) {
  private val hooks = WeakHashMap<W, WeakReference<H>>()

  /** The hook in [window], installed again if the window no longer has it. */
  fun get(window: W): H {
    val hook = hooks[window]?.get()
    if (hook != null && installed(window) === hook) return hook
    return install(window, hook).also { hooks[window] = WeakReference(it) }
  }

  /** The hook last installed in [window], while it is alive. */
  fun peek(window: W): H? = hooks[window]?.get()
}
