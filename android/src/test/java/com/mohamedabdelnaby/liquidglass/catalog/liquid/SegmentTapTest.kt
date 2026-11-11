package com.mohamedabdelnaby.liquidglass.catalog.liquid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The segmented control's tap drop against the iOS measurements (ios-drop-measurements.md,
 * section 3): iOS neighbour / two segments in the comments, stepped at 60 Hz like the device.
 */
class SegmentTapTest {
  private class Frame(val ms: Float, val x: Float, val press: Float, val scaleX: Float, val scaleY: Float)

  private fun run(from: Float, to: Float): List<Frame> {
    val tap = SegmentTap(from, to)
    val frames = mutableListOf<Frame>()
    var moving = true
    var i = 0
    while (moving) {
      moving = tap.step(1f / 60f)
      i++
      // Travel toward the target is positive, in segments from the start.
      val x = (tap.x - from) * if (to > from) 1f else -1f
      frames += Frame(i * 1000f / 60f, x, tap.press, tap.scaleX, tap.scaleY)
      assertTrue("settles within 1.2 s", i < 72)
    }
    assertEquals(to, tap.x, 0.001f)
    assertEquals(1f, tap.scaleX, 0.001f)
    assertEquals(1f, tap.scaleY, 0.001f)
    return frames
  }

  private fun checkTap(from: Float, to: Float, overshoot: ClosedFloatingPointRange<Float>, trough: ClosedFloatingPointRange<Float>) {
    val distance = kotlin.math.abs(to - from)
    val frames = run(from, to)

    // iOS: one overshoot (+0.040 / +0.067 segments) at about 440 ms, then a monotone return.
    val peak = frames.maxBy { it.x }
    assertTrue("overshoot ${peak.x - distance}", peak.x - distance in overshoot)
    assertTrue("overshoot at ${peak.ms} ms", peak.ms in 380f..480f)
    val after = frames.filter { it.ms > peak.ms }
    assertTrue("no second swing", after.all { it.x >= distance - 0.005f })
    after.zipWithNext().forEach { (a, b) -> assertTrue("returns monotonically", b.x <= a.x + 0.0025f) }

    // iOS: lens 1.364 / 1.409 wide at about 204 ms, 1.448 / 1.397 tall at about 110 ms.
    val widest = frames.maxBy { it.scaleX }
    assertTrue("peak scaleX ${widest.scaleX}", widest.scaleX in 1.28f..1.42f)
    assertTrue("peak scaleX at ${widest.ms} ms", widest.ms in 150f..250f)
    val tallest = frames.maxBy { it.scaleY }
    assertTrue("peak scaleY ${tallest.scaleY}", tallest.scaleY in 1.40f..1.50f)
    assertTrue("peak scaleY at ${tallest.ms} ms", tallest.ms in 90f..220f)

    // iOS: one squash after the lens (0.922 / 0.859 wide at about 467 ms); scaleY never below 1.
    val narrowest = frames.filter { it.ms > widest.ms }.minBy { it.scaleX }
    assertTrue("squash ${narrowest.scaleX}", narrowest.scaleX in trough)
    assertTrue("squash at ${narrowest.ms} ms", narrowest.ms in 400f..520f)
    assertTrue("scaleY never below 1", frames.all { it.scaleY >= 1f })

    // iOS: the lens shows from about 50 ms and is flat again by about 325 ms.
    assertTrue("lens up by 67 ms", frames.first { it.ms >= 66f }.press >= 0.5f)
    assertTrue("lens still up at 250 ms", frames.first { it.ms >= 250f }.press >= 0.5f)
    assertTrue("lens flat by 450 ms", frames.first { it.ms >= 450f }.press < 0.1f)
  }

  @Test
  fun neighbourTapMatchesIos() = checkTap(1f, 2f, overshoot = 0.025f..0.045f, trough = 0.90f..0.96f)

  @Test
  fun twoSegmentTapMatchesIos() = checkTap(2f, 0f, overshoot = 0.05f..0.08f, trough = 0.84f..0.90f)
}
