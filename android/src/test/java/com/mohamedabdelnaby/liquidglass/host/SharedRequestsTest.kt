package com.mohamedabdelnaby.liquidglass.host

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The system bar icons screens ask for: latest wins, the window's own value comes back last. */
class SharedRequestsTest {
  private val home = Any()
  private val screen = Any()

  /** A window whose value only [SharedRequests] and, in some cases, the app change. */
  private class FakeWindow(var shown: String) {
    val requests = SharedRequests(shown)

    fun claim(host: Any, value: String) {
      requests.claim(host, value, shown)?.let { shown = it }
    }

    fun release(host: Any) {
      requests.release(host, shown)?.let { shown = it }
    }
  }

  @Test
  fun latestRequestWinsAndTheOriginalComesBackWhenAllLeave() {
    val window = FakeWindow("app")

    window.claim(home, "light")
    window.claim(screen, "dark")
    assertEquals("dark", window.shown)
    // Home asks again with a new value: it is now the latest.
    window.claim(home, "dark")
    window.claim(home, "light")
    assertEquals("light", window.shown)
    window.release(screen)
    assertEquals("light", window.shown)
    window.release(home)
    assertEquals("app", window.shown)
    assertTrue(window.requests.isEmpty)
  }

  @Test
  fun leavingScreenHandsBackToTheOneBelow() {
    val window = FakeWindow("app")
    window.claim(home, "light")
    window.claim(screen, "dark")

    window.release(screen)

    assertEquals("light", window.shown)
  }

  @Test
  fun theSameValueAgainKeepsTheHostsPlace() {
    val window = FakeWindow("app")
    window.claim(home, "light")
    window.claim(screen, "dark")

    // Every props update asks again; Home's repeat must not jump over the screen.
    assertNull(window.requests.claim(home, "light", window.shown))
    window.release(screen)

    assertEquals("light", window.shown)
  }

  @Test
  fun theAppsOwnValueWrittenWhileAScreenIsUpStays() {
    val window = FakeWindow("app")
    window.claim(screen, "dark")

    // An app's <StatusBar> (or an RN screen pushed over this one) writes its own value.
    window.shown = "app-light"
    window.release(screen)

    assertEquals("app-light", window.shown)
    assertTrue(window.requests.isEmpty)
  }

  @Test
  fun aClaimAfterTheAppWroteRestoresTheAppsValue() {
    val window = FakeWindow("app")
    window.claim(home, "light")
    window.shown = "app-dark"

    window.claim(screen, "dark")
    window.release(screen)
    window.release(home)

    assertEquals("app-dark", window.shown)
  }
}

/** Status and navigation icons are each the app's or the library's on their own. */
class WindowBarIconsTest {
  private class Flags(override var lightStatus: Boolean, override var lightNavigation: Boolean) : BarFlags

  @Test
  fun anAppStatusBarWriteKeepsTheStatusFlagAndStillRestoresTheNavigationFlag() {
    val flags = Flags(lightStatus = false, lightNavigation = false)
    val icons = WindowBarIcons(flags)
    val screen = Any()

    // A light screen: dark icons on both bars.
    icons.claim(screen, dark = false, flags)
    assertTrue(flags.lightStatus && flags.lightNavigation)
    // React Native's StatusBar (barStyle="light-content") writes only the status flag.
    flags.lightStatus = false
    icons.release(screen, flags)

    assertEquals(false, flags.lightStatus)
    assertEquals("the app's navigation flag comes back", false, flags.lightNavigation)
    assertTrue(icons.isEmpty)
  }
}

/** A screen's request lasts while it is in the React tree, not only while it is in the window. */
class BarsRequestTest {
  private val calls = mutableListOf<String>()
  private val request = BarsRequest<String>(
    claim = { window, dark -> calls += "claim $window ${if (dark) "dark" else "light"}" },
    release = { window -> calls += "release $window" },
  )

  @Test
  fun claimsOnceComposedInAWindow() {
    request.entered("main")
    request.appearance(dark = false)
    request.appearance(dark = false)
    request.appearance(dark = true)

    assertEquals(listOf("claim main light", "claim main dark"), calls)
  }

  @Test
  fun aDetachWithoutRemovalKeepsTheRequest() {
    request.appearance(dark = false)
    request.entered("main")
    // A native-stack push or a cover takes the view out of the window (no call), then back in.
    request.entered("main")

    assertEquals(listOf("claim main light"), calls)
  }

  @Test
  fun leavingTheReactTreeReleases() {
    request.appearance(dark = false)
    request.entered("main")
    request.left()
    request.left()

    assertEquals(listOf("claim main light", "release main"), calls)
  }

  @Test
  fun anotherWindowMovesTheRequest() {
    request.appearance(dark = true)
    request.entered("main")
    request.entered("dialog")

    assertEquals(listOf("claim main dark", "release main", "claim dialog dark"), calls)
  }
}
