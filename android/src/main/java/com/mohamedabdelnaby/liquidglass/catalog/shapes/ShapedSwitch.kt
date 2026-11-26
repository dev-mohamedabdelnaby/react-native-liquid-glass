package com.mohamedabdelnaby.liquidglass.catalog.shapes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * The switch in a [ControlShape]: Pill and Labeled show ON / OFF beside the thumb, Glow lights
 * up when on. On is at the end, so RTL mirrors it. A null [onCheckedChange] only draws it (a row that toggles carries the semantics).
 */
@Composable
internal fun ShapedSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    val shape = LocalControlShape.current
    val scheme = MaterialTheme.colorScheme
    val accent = shapeAccent()
    val labeled = shape != ControlShape.Glow
    val width = if (labeled) 64.dp else 52.dp
    val height = 32.dp
    val thumb = 24.dp
    val inset = 4.dp
    val on by animateFloatAsState(if (checked) 1f else 0f, shapeSpring(), label = "switch")
    // Filled when on; off is the outline's gray (Pill fills it, the others outline it).
    val off = scheme.outline
    val track by animateColorAsState(
        when {
            checked -> if (shape == ControlShape.Labeled) Color.Transparent else accent
            shape == ControlShape.Pill -> off
            else -> Color.Transparent
        },
        shapeSpring(), label = "track",
    )
    val edge by animateColorAsState(if (checked) accent else off, shapeSpring(), label = "edge")
    val knob by animateColorAsState(
        if (shape == ControlShape.Labeled || !checked && shape != ControlShape.Pill) edge else Color.White,
        shapeSpring(), label = "knob",
    )
    val text = if (shape == ControlShape.Labeled) edge else onAccent(if (checked) accent else off)
    val corners = Capsule
    Box(
        modifier
            .minimumInteractiveComponentSize()
            .then(
                if (onCheckedChange == null) Modifier
                else Modifier.toggleable(checked, interactionSource = null, indication = ripple(bounded = false, radius = 24.dp), role = Role.Switch, onValueChange = onCheckedChange),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(width, height)
                .glow(accent, corners, glowStrength(on))
                .background(track, corners)
                .then(if (track.alpha < 1f) Modifier.border(2.dp, edge, corners) else Modifier),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (labeled) Text(
                if (checked) "ON" else "OFF",
                Modifier
                    .align(if (checked) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 8.dp)
                    .clearAndSetSemantics {},
                color = text,
                // Fixed, not scaled with the font size: it must fit its 28 dp slot (TalkBack reads the state, not this).
                fontSize = with(LocalDensity.current) { 11.dp.toSp() },
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Box(
                Modifier
                    // offset mirrors in RTL, so on stays at the end.
                    .offset { IntOffset(((inset + (width - thumb - inset * 2) * on).toPx()).roundToInt(), 0) }
                    .size(thumb)
                    .background(knob, corners),
            )
        }
    }
}
