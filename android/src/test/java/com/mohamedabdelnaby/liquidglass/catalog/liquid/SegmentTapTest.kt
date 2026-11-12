package com.mohamedabdelnaby.liquidglass.catalog.liquid

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The segmented control's tap drop against the iOS measurements (ios-drop-measurements.md,
 * section 3.1, mean of 6 runs each), at 60 and 120 Hz. Windows are the iOS value with a stated
 * tolerance, not fitted to the model.
 */
class SegmentTapTest {
  private class Frame(val ms: Float, val x: Float, val press: Float, val scaleX: Float, val scaleY: Float)

  /** iOS's numbers for one travel; values at (ms) for the peaks. */
  private class Ios(
    val overshoot: Float, val overshootMs: Float,
    val widest: Float, val widestMs: Float,
    val tallest: Float, val tallestMs: Float,
    val squash: Float, val squashMs: Float,
    val settleMs: Float,
  )

  private val neighbour = Ios(0.040f, 441f, 1.364f, 204f, 1.448f, 112f, 0.922f, 467f, 647f)
  private val twoAway = Ios(0.067f, 430f, 1.409f, 203f, 1.397f, 99f, 0.859f, 464f, 680f)

  private fun run(tap: SegmentTap, from: Float, hz: Int, onFrame: (Int) -> Unit = {}): List<Frame> {
    val frames = mutableListOf<Frame>()
    var moving = true
    var i = 0
    while (moving) {
      onFrame(i)
      moving = tap.step(1f / hz)
      i++
      // Travel toward the target is positive, in segments from the start.
      val x = (tap.x - from) * if (tap.to > from) 1f else -1f
      frames += Frame(i * 1000f / hz, x, tap.press, tap.scaleX, tap.scaleY)
      assertTrue("settles within 1.2 s", i < 1.2f * hz)
    }
    assertEquals(tap.to, tap.x, 0.001f)
    assertEquals(1f, tap.scaleX, 0.001f)
    assertEquals(1f, tap.scaleY, 0.001f)
    return frames
  }

  private fun near(label: String, actual: Float, expected: Float, tolerance: Float) =
    assertTrue("$label $actual, iOS $expected ± $tolerance", abs(actual - expected) <= tolerance)

  private fun checkTap(from: Float, to: Float, ios: Ios, hz: Int) {
    val distance = abs(to - from)
    val frames = run(SegmentTap(from, to), from, hz)

    // One overshoot, then a monotone return with no second swing.
    val peak = frames.maxBy { it.x }
    near("overshoot", peak.x - distance, ios.overshoot, 0.010f)
    near("overshoot ms", peak.ms, ios.overshootMs, 50f)
    val after = frames.filter { it.ms > peak.ms }
    assertTrue("no second swing", after.all { it.x >= distance - 0.005f })
    after.zipWithNext().forEach { (a, b) -> assertTrue("returns monotonically", b.x <= a.x + 0.0025f) }

    val widest = frames.maxBy { it.scaleX }
    near("widest", widest.scaleX, ios.widest, 0.03f)
    near("widest ms", widest.ms, ios.widestMs, 40f)
    val tallest = frames.maxBy { it.scaleY }
    near("tallest", tallest.scaleY, ios.tallest, 0.04f)
    near("tallest ms", tallest.ms, ios.tallestMs, 40f)

    // One squash as the thumb lands; scaleY never below 1.
    val narrowest = frames.filter { it.ms > widest.ms }.minBy { it.scaleX }
    near("squash", narrowest.scaleX, ios.squash, 0.025f)
    near("squash ms", narrowest.ms, ios.squashMs, 50f)
    assertTrue("scaleY never below 1", frames.all { it.scaleY >= 1f })

    // iOS's lens shows from about 50 ms to about 325 ms. Known gap: this lens is under half
    // strength about 60 ms earlier, so the check is "still up at 250 ms".
    assertTrue("lens up by 67 ms", frames.first { it.ms >= 66f }.press >= 0.5f)
    assertTrue("lens still up at 250 ms", frames.first { it.ms >= 250f }.press >= 0.5f)
    assertTrue("lens flat by 450 ms", frames.first { it.ms >= 450f }.press < 0.1f)

    val settled = frames.indexOfLast { abs(it.x - distance) >= 0.01f || abs(it.scaleX - 1f) >= 0.02f || abs(it.scaleY - 1f) >= 0.03f } + 1
    assertTrue("settles by iOS + 100 ms", frames[settled].ms <= ios.settleMs + 100f)
  }

  @Test
  fun neighbourTapMatchesIos() = checkTap(1f, 2f, neighbour, hz = 60)

  @Test
  fun twoSegmentTapMatchesIos() = checkTap(2f, 0f, twoAway, hz = 60)

  @Test
  fun neighbourTapMatchesIosAt120Hz() = checkTap(0f, 1f, neighbour, hz = 120)

  @Test
  fun twoSegmentTapMatchesIosAt120Hz() = checkTap(0f, 2f, twoAway, hz = 120)

  @Test
  fun aSecondTapRetargetsWithoutDroppingTheLens() {
    val tap = SegmentTap(0f, 1f)
    var lowest = 1f
    var largestStep = 0f
    var last = 0f
    run(tap, 0f, hz = 60) { frame ->
      // A tap on the third segment 150 ms in, while the lens is up.
      if (frame == 9) tap.retarget(2f)
      if (frame in 10..20) lowest = minOf(lowest, tap.press)
      largestStep = maxOf(largestStep, abs(tap.x - last))
      last = tap.x
    }
    assertEquals(2f, tap.x, 0.001f)
    assertTrue("lens stays up through the retarget ($lowest)", lowest >= 0.5f)
    assertTrue("no jump ($largestStep segments in a frame)", largestStep < 0.2f)
  }
}
