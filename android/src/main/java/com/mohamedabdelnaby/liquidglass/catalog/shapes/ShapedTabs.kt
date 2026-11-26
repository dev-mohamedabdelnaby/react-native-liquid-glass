package com.mohamedabdelnaby.liquidglass.catalog.shapes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/** The segmented control (top tabs) in a [ControlShape], 48 dp tall. */
@Composable
internal fun ShapedTabs(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier) =
    ShapedTabRow(labels, selectedIndex, onSelect, modifier.fillMaxWidth().height(48.dp), shapeAccent(), inset = 4.dp) { i, color ->
        Text(labels[i], color = color, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
    }

/**
 * Equal tabs on a container with a selection marker that springs to [selectedIndex]: Pill a white
 * marker in an accent container, Labeled an outlined container with a filled marker, Glow a
 * glowing marker. TalkBack reads each tab as the liquid tab rows did: role Tab, selected, label.
 */
@Composable
internal fun ShapedTabRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    accent: Color,
    inset: Dp,
    item: @Composable (index: Int, color: Color) -> Unit,
) {
    val shape = LocalControlShape.current
    val scheme = MaterialTheme.colorScheme
    val corners = Capsule
    val count = labels.size.coerceAtLeast(1)
    val container = when (shape) {
        ControlShape.Pill -> accent
        ControlShape.Labeled -> scheme.surface
        ControlShape.Glow -> scheme.surfaceContainerHighest
    }
    val marker = if (shape == ControlShape.Pill) Color.White else accent
    val selectedColor = if (shape == ControlShape.Pill) onWhite(accent) else onAccent(accent)
    val otherColor = if (shape == ControlShape.Pill) onAccent(accent) else scheme.onSurface
    val select by rememberUpdatedState(onSelect)
    val position by animateFloatAsState(selectedIndex.coerceIn(0, count - 1).toFloat(), shapeSpring(), label = "marker")
    BoxWithConstraints(
        modifier
            .background(container, corners)
            .then(if (shape == ControlShape.Labeled) Modifier.border(1.5.dp, accent, corners) else Modifier),
    ) {
        val width = (maxWidth - inset * 2) / count
        // offset mirrors in RTL, so the marker follows the tabs.
        Box(
            Modifier
                .padding(inset)
                .offset { IntOffset((width * position).roundToPx(), 0) }
                .width(width)
                .fillMaxHeight()
                .glow(accent, corners, glowStrength())
                .background(marker, corners),
        )
        Row(Modifier.matchParentSize().padding(inset).selectableGroup()) {
            labels.forEachIndexed { i, label ->
                val color by animateColorAsState(if (i == selectedIndex) selectedColor else otherColor, shapeSpring(), label = "tab")
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(i) { detectTapGestures { select(i) } }
                        .clearAndSetSemantics {
                            role = Role.Tab
                            selected = i == selectedIndex
                            contentDescription = label
                            onClick { onSelect(i); true }
                        },
                    contentAlignment = Alignment.Center,
                ) { item(i, color) }
            }
        }
    }
}

/** The accent as text on the white marker when it reads (3:1), else black. */
private fun onWhite(accent: Color): Color = if (accent.luminance() <= 0.3f) accent else Color.Black
