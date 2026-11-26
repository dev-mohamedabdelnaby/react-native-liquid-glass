package com.mohamedabdelnaby.liquidglass.catalog.shapes

import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Key
import androidx.compose.material3.ripple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.sliderDetent
import com.mohamedabdelnaby.liquidglass.catalog.glass.sliderLabel
import kotlin.math.roundToInt

/**
 * The slider in a [ControlShape]: Pill a thick track with a white handle, Labeled a ring on a thin
 * track, Glow a glowing track with a ring. The value grows toward
 * the end (RTL mirrors it), with a light haptic each tenth, as the Material slider.
 */
@Composable
internal fun ShapedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    accent: Color,
    contentDescription: String?,
) {
    val shape = LocalControlShape.current
    val scheme = MaterialTheme.colorScheme
    val min = valueRange.start
    val max = valueRange.endInclusive.coerceAtLeast(min + 0.0001f)
    val onChange by rememberUpdatedState(onValueChange)
    val current by rememberUpdatedState(value)
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val trackHeight = when (shape) { ControlShape.Pill -> 16.dp; ControlShape.Glow -> 6.dp; ControlShape.Labeled -> 4.dp }
    val handle = if (shape == ControlShape.Pill) 24.dp else 20.dp
    val handleShape = Capsule
    val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            // One node for TalkBack: its label, value ("Volume, 50%") and adjust action.
            .clearAndSetSemantics {
                contentDescription?.let { this.contentDescription = it }
                stateDescription = sliderLabel(value, valueRange)
                progressBarRangeInfo = ProgressBarRangeInfo(value, min..max)
                setProgress { onChange(it.coerceIn(min, max)); true }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val travel = with(LocalDensity.current) { (maxWidth - handle).toPx() }.coerceAtLeast(1f)
        val half = with(LocalDensity.current) { (handle / 2).toPx() }
        fun set(next: Float) {
            if (sliderDetent(next, valueRange) != sliderDetent(current, valueRange)) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            onChange(next)
        }
        fun moveTo(x: Float) = set(min + sliderFraction(x, constraints.maxWidth.toFloat(), half, rtl) * (max - min))
        val x = remember { mutableFloatStateOf(0f) }
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(rtl, travel) { detectTapGestures { moveTo(it.x) } }
                .draggable(
                    rememberDraggableState { x.floatValue += it; moveTo(x.floatValue) },
                    Orientation.Horizontal,
                    interactionSource = interaction,
                    onDragStarted = { x.floatValue = it.x; moveTo(it.x) },
                )
                // Keyboard and D-pad: focus it, then arrows, page keys, Home and End.
                .focusable(interactionSource = interaction)
                .onKeyEvent { e ->
                    val d = if (e.type == KeyEventType.KeyDown) sliderKeyDelta(e.key, rtl) else null
                    if (d != null) set((current + d * (max - min)).coerceIn(min, max))
                    d != null
                },
        )
        val track = Capsule
        val reach = handle / 2 + (maxWidth - handle) * fraction
        // Inactive track, then the active part from the start to the handle's centre.
        Box(Modifier.fillMaxWidth().height(trackHeight).background(if (shape == ControlShape.Labeled) scheme.outlineVariant else accent.copy(alpha = 0.24f), track))
        Box(Modifier.width(reach).height(trackHeight).glow(accent, track, glowStrength()).background(accent, track))
        val ring = shape == ControlShape.Labeled || shape == ControlShape.Glow
        Box(
            Modifier
                .offset { IntOffset(((maxWidth - handle) * fraction).roundToPx(), 0) }
                .size(handle)
                // Pressed, dragged and focused show as Material's halo around the handle.
                .indication(interaction, ripple(bounded = false, radius = 20.dp))
                .glow(accent, handleShape, glowStrength())
                .background(
                    when {
                        ring -> scheme.surface
                        else -> Color.White
                    },
                    handleShape,
                )
                .then(
                    when {
                        ring -> Modifier.border(3.dp, accent, handleShape)
                        // Pill: a hairline so the white handle reads on a light accent.
                        shape == ControlShape.Pill -> Modifier.border(0.5.dp, Color.Black.copy(alpha = 0.12f), handleShape)
                        else -> Modifier
                    },
                ),
        )
    }
}

/** Where a touch at [x] (from the left edge of a [width] wide slider, handle radius [half]) puts the value, 0..1 from the start edge. */
internal fun sliderFraction(x: Float, width: Float, half: Float, rtl: Boolean): Float =
    (((if (rtl) width - x else x) - half) / (width - half * 2).coerceAtLeast(1f)).coerceIn(0f, 1f)

/** A hardware key's move as a fraction of the range (arrows 1%, page keys 10%, Home / End to the ends), or null. */
internal fun sliderKeyDelta(key: Key, rtl: Boolean): Float? = when (key) {
    Key.DirectionRight -> if (rtl) -0.01f else 0.01f
    Key.DirectionLeft -> if (rtl) 0.01f else -0.01f
    Key.DirectionUp -> 0.01f
    Key.DirectionDown -> -0.01f
    Key.PageUp -> 0.1f
    Key.PageDown -> -0.1f
    Key.MoveHome -> -1f
    Key.MoveEnd -> 1f
    else -> null
}
