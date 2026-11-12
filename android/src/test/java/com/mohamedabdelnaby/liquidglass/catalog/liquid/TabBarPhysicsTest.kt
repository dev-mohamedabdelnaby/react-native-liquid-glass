package com.mohamedabdelnaby.liquidglass.catalog.liquid

import androidx.compose.runtime.MonotonicFrameClock
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A segmented control's touches, as iOS keys them (ios-drop-measurements.md 3.5): a press on
 * another segment moves nothing until release, a press on the thumb lifts it critically damped,
 * and a second tap in flight retargets the drop. Three 100 px segments, frames at 60 Hz.
 */
class TabBarPhysicsTest {
  /** Frames on demand. */
  private class Clock : MonotonicFrameClock {
    private val waiting = mutableListOf<CancellableContinuation<Long>>()
    private var now = 0L

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R =
      onFrame(suspendCancellableCoroutine { waiting += it })

    fun frames(count: Int, each: () -> Unit = {}) = repeat(count) {
      now += 16_666_667L
      val due = waiting.toList()
      waiting.clear()
      due.forEach { it.resume(now) }
      each()
    }
  }

  private val clock = Clock()
  private val job = Job()
  private val selected = mutableListOf<Int>()
  private val physics = TabBarPhysics(CoroutineScope(Dispatchers.Unconfined + clock + job)).apply {
    tabCount = 3
    segmented = true
    width = 300f
    onSelect = { selected += it }
    setSelectedIndex(0)
  }
  private val state get() = physics.state

  @After
  fun stop() = job.cancel()

  @Test
  fun aPressOnAnotherSegmentMovesNothingUntilRelease() {
    physics.onDown(150f)
    clock.frames(10)
    assertEquals(0f, state.value, 0f)
    assertEquals(0f, state.press, 0f)

    physics.onUp(150f)
    clock.frames(6)
    assertTrue("travelling after release", state.value > 0.2f && state.press > 0.5f)
    clock.frames(60)
    assertEquals(1f, state.value, 0.001f)
    assertEquals(0f, state.press, 0.001f)
    assertEquals(listOf(1), selected)
  }

  @Test
  fun aPressOnTheThumbLiftsItAndLetsGoWithoutAWobble() {
    physics.onDown(50f)
    var widest = 0f
    clock.frames(30) { widest = maxOf(widest, state.scaleX) }
    assertEquals(1f, state.press, 0.01f)
    // iOS's held lens, 1.24 x 1.62, reached without overshoot.
    assertEquals(1.24f, state.scaleX, 0.01f)
    assertEquals(1.62f, state.scaleY, 0.01f)
    assertTrue("no overshoot ($widest)", widest <= 1.241f)

    physics.onUp(50f)
    var narrowest = 2f
    clock.frames(40) { narrowest = minOf(narrowest, state.scaleX) }
    assertEquals(0f, state.press, 0.001f)
    assertEquals(1f, state.scaleX, 0.001f)
    assertTrue("no undershoot ($narrowest)", narrowest >= 0.999f)
    assertEquals(0f, state.value, 0f)
  }

  @Test
  fun aSecondTapInFlightKeepsTheLensUp() {
    physics.onDown(150f)
    physics.onUp(150f)
    clock.frames(9)

    physics.onDown(250f)
    var lowest = 1f
    clock.frames(2) { lowest = minOf(lowest, state.press) }
    physics.onUp(250f)
    clock.frames(10) { lowest = minOf(lowest, state.press) }
    assertTrue("lens stays up ($lowest)", lowest >= 0.5f)

    clock.frames(80)
    assertEquals(2f, state.value, 0.001f)
  }
}
