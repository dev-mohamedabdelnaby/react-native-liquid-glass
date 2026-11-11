package com.mohamedabdelnaby.liquidglass.host

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The system bar icons screens ask for: latest wins, the window's own value comes back last. */
class SharedRequestsTest {
  private val home = Any()
  private val screen = Any()

  @Test
  fun latestRequestWinsAndTheOriginalComesBackWhenAllLeave() {
    val requests = SharedRequests(original = "app")

    assertEquals("light", requests.claim(home, "light"))
    assertEquals("dark", requests.claim(screen, "dark"))
    // Home asks again (its appearance changed): it is now the latest.
    assertEquals("light", requests.claim(home, "light"))
    assertEquals("light", requests.release(screen))
    assertFalse(requests.isEmpty)
    assertEquals("app", requests.release(home))
    assertTrue(requests.isEmpty)
  }

  @Test
  fun leavingScreenHandsBackToTheOneBelow() {
    val requests = SharedRequests(original = "app")
    requests.claim(home, "light")
    requests.claim(screen, "dark")

    assertEquals("light", requests.release(screen))
  }
}
