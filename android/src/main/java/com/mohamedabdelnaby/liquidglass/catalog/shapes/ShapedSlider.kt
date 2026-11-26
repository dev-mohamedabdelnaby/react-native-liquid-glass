package com.mohamedabdelnaby.liquidglass.catalog.shapes

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
        // x is measured from the left edge of the box, whatever the direction.
        fun moveTo(x: Float) {
            val f = (((if (rtl) constraints.maxWidth - x else x) - half) / travel).coerceIn(0f, 1f)
            val next = min + f * (max - min)
            if (sliderDetent(next, valueRange) != sliderDetent(current, valueRange)) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            onChange(next)
        }
        val x = remember { mutableFloatStateOf(0f) }
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(rtl, travel) { detectTapGestures { moveTo(it.x) } }
                .draggable(
                    rememberDraggableState { x.floatValue += it; moveTo(x.floatValue) },
                    Orientation.Horizontal,
                    onDragStarted = { x.floatValue = it.x; moveTo(it.x) },
                ),
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
