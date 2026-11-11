package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.Popup
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

private val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

/** `.sheet` with `.medium` and `.large` detents, floating on glass. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassOverlay { ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            // Filling the sheet gives it the medium and large detents.
            Modifier.fillMaxSize().glassEffect(panelGlass(), SheetShape).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), CapsuleShape),
            )
            content()
        }
    } }
}

/** `.alert`: a title, a message and actions on rounded glass. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassAlertDialog(
    title: String,
    message: String,
    onDismissRequest: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    // iOS alerts are modal, so only back dismisses, not an outside tap.
    GlassOverlay { BasicAlertDialog(onDismissRequest = onDismissRequest, properties = DialogProperties(dismissOnClickOutside = false)) {
        Column(
            Modifier.glassEffect(panelGlass(), RoundedCornerShape(32.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Column(
                Modifier.padding(top = 12.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = actions,
            )
        }
    } }
}

/** Full-width dialog action, prominent or plain (frosted) glass. */
@Composable
fun DialogAction(text: String, onClick: () -> Unit, prominent: Boolean = false, destructive: Boolean = false) {
    val modifier = Modifier.fillMaxWidth()
    when {
        prominent -> GlassProminentButton(onClick, modifier, tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) { Text(text) }
        // Frosted, not liquid, like the sheet's Done: a liquid lens on the dialog's own glass reads as a lens on a lens.
        else -> GlassButton(
            onClick, modifier,
            glass = Glass.Regular.copy(liquid = false),
            contentColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        ) { Text(text) }
    }
}

/**
 * `.popover` (and iOS 27's confirmation dialog): a frosted bubble above its anchor, the parent of
 * this call, with its arrow on it, springing out of the arrow's tip. Tapping outside dismisses it.
 */
@Composable
fun GlassPopover(expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val pop = remember { Animatable(0f) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) {
        if (expanded) {
            shown = true
            pop.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 520f))
        } else {
            pop.animateTo(0f, spring(dampingRatio = 1f, stiffness = 700f))
            shown = false
        }
    }
    if (!shown) return
    val gap = with(LocalDensity.current) { 4.dp.roundToPx() }
    GlassOverlay {
        Popup(
            popupPositionProvider = remember(gap) { AboveAnchor(gap) },
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
        ) {
            Column(
                Modifier
                    .graphicsLayer {
                        val p = pop.value
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        scaleX = lerp(0.2f, 1f, p)
                        scaleY = lerp(0.2f, 1f, p)
                        alpha = (p * 2f).coerceIn(0f, 1f)
                    }
                    // Frosted: the liquid lens only draws rounded rectangles, and this shape has an arrow.
                    // No shadow: it would outline the bubble and its arrow like a border.
                    .frostedGlass(Glass.Regular, PopoverShape, shadow = false)
                    .padding(bottom = PopoverArrow),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

private val PopoverArrow = 10.dp

/** The popover's bubble with its arrow pointing down at the anchor, as one shape. */
private object PopoverShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val arrow = with(density) { PopoverArrow.toPx() }
        val half = with(density) { 11.dp.toPx() }
        val radius = with(density) { 16.dp.toPx() }
        val bottom = size.height - arrow
        return Outline.Generic(Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, bottom, CornerRadius(radius)))
            // A sharp triangle pointing at the anchor, overlapping the bubble by a pixel so no seam shows.
            moveTo(size.width / 2f - half, bottom - 1f)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width / 2f + half, bottom - 1f)
            close()
        })
    }
}

/** Places the popup centered above its anchor, [gap] px over it. */
private class AboveAnchor(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize) =
        IntOffset(
            (anchorBounds.center.x - popupContentSize.width / 2).coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            (anchorBounds.top - popupContentSize.height - gap).coerceAtLeast(0),
        )
}
