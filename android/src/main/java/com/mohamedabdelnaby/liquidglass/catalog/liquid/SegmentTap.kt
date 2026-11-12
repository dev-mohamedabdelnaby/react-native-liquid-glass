package com.mohamedabdelnaby.liquidglass.catalog.liquid

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

/**
 * The drop a tap sends across an iOS 26 segmented control, from touch-up, fitted to the iOS
 * catalog frame by frame (neighbour / two segments): the centre overshoots once, by 3 % / 3.3 % of
 * the travel at about 430 ms, and returns without a second swing; the lens shows from about 50 ms,
 * is tallest (about 1.42 / 1.39) at about 110 ms and widest (about 1.34 / 1.40) at about 190 ms,
 * and turns back into the flat thumb at about 300 ms; the thumb lands squashed (about 0.94 / 0.88
 * wide, as much taller) and settles by about 700 ms. Positions are in segments; the thumb is one
 * segment wide.
 */
internal class SegmentTap(private var from: Float, to: Float, lift: Float = 0f) {
    /** The segment the thumb is going to. */
    var to = to
        private set
    private var direction = sign(to - from)
    private var distance = abs(to - from)

    // iOS's centre settles as one spring (damping 0.73, stiffness 78) that started about 90 ms
    // before the thumb visibly moves; started that far ahead and eased in, so it doesn't jump.
    private val centre = LiquidSpring(0.73f, 78f, 0.002f).apply {
        snap(from)
        target = to
        repeat(3) { step(0.03f) }
    }
    private val lag = LiquidSpring(1f, 870f, 0.0005f).apply { value = from - centre.value }

    // The lens lifts at once (from [lift] if a finger already lifted it) and deflates from 85 % of the way.
    private val lens = LiquidSpring(1f, 1300f, 0.001f).apply {
        value = lift
        target = 1f
    }

    // Half the lens's extra width, in segments: wider the faster it moves.
    private val halfWidth = LiquidSpring(1f, 1100f, 0.0005f)
    private var arrived = false

    /** The thumb's centre, in segments. */
    var x = from
        private set

    /** 0 for the flat thumb, 1 for the lifted lens. */
    var press = lift
        private set
    var scaleX = 1f
        private set
    var scaleY = 1f
        private set

    /**
     * A tap on another segment while this one runs: on to [to] from where the thumb is, keeping
     * its speed, lens and shape, as iOS retargets its drop.
     */
    fun retarget(to: Float) {
        if (to == this.to) return
        from = x
        this.to = to
        direction = sign(to - from)
        distance = abs(to - from)
        centre.target = to
        lens.target = 1f
        lens.stiffness = 1300f
        arrived = false
    }

    /** Advances by [dt] seconds; false once everything has settled on [to]. */
    fun step(dt: Float): Boolean {
        var moving = centre.step(dt) or lag.step(dt)
        val next = centre.value + lag.value
        val speed = abs(next - x) / dt
        x = next
        val travelled = (x - from) * direction
        if (lens.target == 1f && travelled >= 0.85f * distance) {
            lens.target = 0f
            lens.stiffness = 280f
        }
        if (travelled >= distance) arrived = true
        moving = lens.step(dt) or moving
        halfWidth.target = if (arrived) 0f else max(0f, lens.value) * (0.155f + 0.010f * speed)
        moving = halfWidth.step(dt) or moving
        press = lens.value.coerceIn(0f, 1f)
        // Past the target the leading edge stays there and the trailing edge carries the overshoot,
        // so the thumb lands narrower, and as much taller.
        val over = max(0f, (x - to) * direction)
        scaleX = 1f + 2f * (halfWidth.value - over)
        // The lens is tallest as it lifts and flattens as it widens, as on iOS.
        scaleY = max(1f + press * (0.62f - 0.6f * max(0f, scaleX - 1f)), 1f / scaleX)
        return moving
    }
}
