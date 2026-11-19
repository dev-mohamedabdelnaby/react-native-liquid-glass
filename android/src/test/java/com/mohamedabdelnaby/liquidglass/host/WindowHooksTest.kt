package com.mohamedabdelnaby.liquidglass.host

import java.lang.ref.WeakReference
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/** The back key's window hooks: they let old windows go, and come back when replaced. */
class WindowHooksTest {
  /** A window and its activity hold each other, as on Android. */
  private class FakeWindow { var callback: Any? = null }
  private class FakeActivity(val window: FakeWindow) { init { window.callback = this } }
  private class Hook(val app: Any?)

  private val installs = mutableListOf<Hook?>()
  private val hooks = WindowHooks<FakeWindow, Hook>({ it.callback }) { window, previous ->
    installs += previous
    Hook(window.callback).also { window.callback = it }
  }

  @Test
  fun aHookedWindowCanBeCollected() {
    val window = hookedWindow()
    repeat(20) {
      if (window.get() == null) return@repeat
      System.gc()
      Thread.sleep(20)
    }
    assertNull("the hook map kept the window (and its activity) alive", window.get())
  }

  // Nothing but the map refers to the window once this returns.
  private fun hookedWindow(): WeakReference<FakeWindow> {
    val activity = FakeActivity(FakeWindow())
    hooks.get(activity.window)
    return WeakReference(activity.window)
  }

  @Test
  fun aReplacedHookIsInstalledAgain() {
    val window = FakeActivity(FakeWindow()).window
    val first = hooks.get(window)
    assertSame(first, hooks.get(window))

    // Another library sets its own callback without passing events on.
    window.callback = Any()
    val second = hooks.get(window)
    assertNotSame(first, second)
    assertSame(second, window.callback)
    assertSame(first, installs.last())
    assertSame(second, hooks.peek(window))
  }
}
