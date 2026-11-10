package com.mohamedabdelnaby.liquidglass.engine.glass

import androidx.compose.ui.util.lerp
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import kotlin.math.roundToInt
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.effects.blur
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.drawBackdrop
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mohamedabdelnaby.liquidglass.engine.liquid.LiquidMenuPanel
import com.mohamedabdelnaby.liquidglass.engine.liquid.LiquidMenuRow
import com.mohamedabdelnaby.liquidglass.engine.liquid.LiquidMenuSeparator
import com.mohamedabdelnaby.liquidglass.engine.liquid.LiquidMenuTitle
import com.mohamedabdelnaby.liquidglass.engine.liquid.menuSourceAlpha

/**
 * A glass menu that grows out of its anchor, the parent `Box` this is placed in, like a drop.
 * [offset] is kept for `DropdownMenu` compatibility and ignored: the menu opens over its anchor.
 */
@Composable
fun GlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") offset: DpOffset = DpOffset(0.dp, 8.dp),
    content: @Composable ColumnScope.() -> Unit,
) = LiquidDropdown(expanded, onDismissRequest, modifier, onProgress = {}, onClosed = {}, content = content)

/**
 * SwiftUI's `Menu { } label: { }`: [anchor] gets `open`, [items] get `dismiss`. With a [preview]
 * it is iOS's `.contextMenu`: the screen blurs, the preview lifts in place and the menu grows below it.
 */
@Composable
fun GlassMenu(
    modifier: Modifier = Modifier,
    preview: (@Composable () -> Unit)? = null,
    anchor: @Composable (open: () -> Unit) -> Unit,
    items: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    // The anchor fades as the drop covers it, and back as the menu shrinks into it.
    var anchorAlpha by remember { mutableFloatStateOf(1f) }
    Box(modifier.graphicsLayer { alpha = anchorAlpha }) {
        anchor { expanded = true }
        LiquidDropdown(
            expanded,
            { expanded = false },
            Modifier,
            // A lifted preview covers the anchor, so it hides at once instead of fading.
            // A context menu's preview crossfades in over the tile as it pops out of it.
            onProgress = { anchorAlpha = if (preview != null) (1f - it * 3f).coerceIn(0f, 1f) else menuSourceAlpha(it) },
            onClosed = { anchorAlpha = 1f },
            preview = preview,
        ) { items { expanded = false } }
    }
}

/** The lib's MenuOverlay: a full-screen popup hosting the panel while it opens and closes. */
@Composable
private fun LiquidDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    onProgress: (Float) -> Unit,
    onClosed: () -> Unit,
    preview: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // On screen, since the popup is another window.
    var anchor by remember { mutableStateOf(Rect.Zero) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) { if (expanded) shown = true }
    Box(Modifier.onGloballyPositioned { c -> c.parentLayoutCoordinates?.let { anchor = Rect(it.positionOnScreen(), it.size.toSize()) } })
    if (!expanded && !shown) return

    // Dismiss requests while the menu shrinks back are dropped, like the lib's close().
    val dismiss = { if (expanded) onDismissRequest() }
    GlassOverlay {
        Popup(onDismissRequest = dismiss, properties = PopupProperties(focusable = true)) {
            var origin by remember { mutableStateOf<Offset?>(null) }
            var progress by remember { mutableFloatStateOf(0f) }
            var previewSize by remember { mutableStateOf(IntSize.Zero) }
            Box(Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionOnScreen() }) {
                origin?.let { origin ->
                    val local = anchor.translate(-origin)
                    if (preview != null) ContextPreview(local, progress, { previewSize = it }, preview)
                    LiquidMenuPanel(
                        // A context menu grows from 8 dp below the lifted preview, as on iOS.
                        trigger = if (preview == null) local else with(LocalDensity.current) {
                            val top = local.center.y + maxOf(previewSize.height.toFloat(), local.height) / 2f + 8.dp.toPx()
                            Rect(local.left, top, local.right, top + 44.dp.toPx())
                        },
                        open = expanded,
                        onDismissRequest = dismiss,
                        onProgress = { progress = it; onProgress(it) },
                        onClosed = { shown = false; onClosed() },
                        modifier = modifier,
                        towardCenter = preview == null,
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * iOS's context menu backdrop: the screen dims and blurs lightly, and [preview] springs out of the
 * anchor's size to its own, centered on it, as [progress] (which may overshoot 1) runs.
 */
@Composable
private fun ContextPreview(anchor: Rect, progress: Float, onPreviewSize: (IntSize) -> Unit, preview: @Composable () -> Unit) {
    val shown = progress.coerceIn(0f, 1f)
    val backdrop = LocalGlassBackdrop.current
    val dark = isSystemInDarkTheme()
    Box(
        Modifier
            .fillMaxSize()
            .then(
                if (backdrop == null) Modifier
                else Modifier.drawBackdrop(backdrop, { RectangleShape }, { blur(10.dp.toPx() * shown) }, highlight = null, shadow = null),
            )
            .background(Color.Black.copy(alpha = (if (dark) 0.35f else 0.2f) * shown)),
    )
    val density = LocalDensity.current
    Box(
        Modifier
            .offset { IntOffset(anchor.left.roundToInt(), anchor.top.roundToInt()) }
            .size(with(density) { anchor.width.toDp() }, with(density) { anchor.height.toDp() }),
        contentAlignment = Alignment.Center,
    ) {
        var size by remember { mutableStateOf(IntSize.Zero) }
        Box(
            Modifier
                // Its own size, not the anchor's, so the pop can start from the anchor's.
                .wrapContentSize(unbounded = true)
                .onSizeChanged { size = it; onPreviewSize(it) }
                .graphicsLayer {
                    val start = if (size.width > 0) minOf(1f, anchor.width / size.width) else 1f
                    val scale = lerp(start, 1f, progress)
                    scaleX = scale
                    scaleY = scale
                    alpha = (progress * 3f).coerceIn(0f, 1f)
                },
        ) { preview() }
    }
}

@Composable
fun GlassMenuItem(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    destructive: Boolean = false,
    checked: Boolean = false,
) = LiquidMenuRow(text, onClick, icon, destructive, checked)

@Composable
fun GlassMenuHeader(text: String) = LiquidMenuTitle(text)

@Composable
fun GlassMenuDivider() = LiquidMenuSeparator()
