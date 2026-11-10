package com.mohamedabdelnaby.liquidglass.engine.glass

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

/**
 * SwiftUI's `withAnimation(.bouncy)` around an `if`: one spring (in place in about 0.27 s, as in the
 * iOS recording, and back out the same way) drives the view's layout, content and glass.
 */
@Stable
class Reveal(visible: Boolean) {
    // Done within 3%: the spring's slow last percent would leave collapsed glass inside its source.
    val progress = Animatable(if (visible) 1f else 0f, visibilityThreshold = 0.03f)
    var shown by mutableStateOf(visible)
}

@Composable
fun rememberReveal(visible: Boolean): Reveal {
    val reveal = remember { Reveal(visible) }
    LaunchedEffect(visible) {
        if (visible) {
            reveal.shown = true
            reveal.progress.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = 380f))
        } else {
            // Closing is opening played back: the same stiffness, damped so it settles without a bounce.
            reveal.progress.animateTo(0f, spring(dampingRatio = 1f, stiffness = 380f))
            reveal.shown = false
        }
    }
    return reveal
}

/**
 * Lays its glass out between [from] (where it grows out of, in this view's coordinates) and its own
 * bounds as the reveal goes 0 → 1, and back on the way out: the glass rides the same spring as the
 * content, as SwiftUI's matched geometry does inside `withAnimation`.
 */
fun Modifier.growFrom(reveal: Reveal, from: MeasureScope.(width: Int, height: Int) -> IntRect) = layout { measurable, constraints ->
    val width = constraints.maxWidth
    val height = constraints.maxHeight
    val p = reveal.progress.value
    val a = from(width, height)
    val left = lerp(a.left.toFloat(), 0f, p).roundToInt()
    val top = lerp(a.top.toFloat(), 0f, p).roundToInt()
    val right = lerp(a.right.toFloat(), width.toFloat(), p).roundToInt()
    val bottom = lerp(a.bottom.toFloat(), height.toFloat(), p).roundToInt()
    val placeable = measurable.measure(Constraints.fixed((right - left).coerceAtLeast(1), (bottom - top).coerceAtLeast(1)))
    layout(width, height) { placeable.place(left, top) }
}

/** Content fading in and sharpening from a blur with the reveal, as SwiftUI inserts a view. */
fun Modifier.revealContent(reveal: Reveal) = graphicsLayer {
    val p = reveal.progress.value.coerceIn(0f, 1f)
    alpha = p
    val radius = (1f - p) * 8.dp.toPx()
    renderEffect = if (radius > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) BlurEffect(radius, radius) else null
}

/**
 * One action of a floating button's menu, stacked above the button with 12 dp gaps; [below] is how
 * many actions sit between it and the button. With [morph] (`glassEffectID` + matched geometry) its
 * glass grows out of the button's circle and its label rides along; without it (`.materialize`) it
 * grows in place.
 */
@Composable
fun RevealPill(
    title: String,
    icon: ImageVector,
    glass: Glass,
    reveal: Reveal,
    below: Int,
    morph: Boolean = true,
    buttonSize: Dp = 60.dp,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    // Where the button's centre is, from this pill's top-left corner.
    fun buttonCenterY(height: Int) = with(density) { height + below * (height + 12.dp.roundToPx()) + 12.dp.roundToPx() + buttonSize.roundToPx() / 2 }
    Box(Modifier.glassClickable(label = title, onClick = onClick)) {
        Box(
            Modifier
                .matchParentSize()
                .growFrom(reveal) { width, height ->
                    if (morph) {
                        // The button's centre: the collapsing glass ends inside the button, so the button's
                        // own tint stays on top instead of being washed over in the last frames.
                        val cx = width - buttonSize.roundToPx() / 2
                        val cy = buttonCenterY(height)
                        IntRect(cx, cy, cx, cy)
                    } else {
                        IntRect(width / 5, height / 5, width * 4 / 5, height * 4 / 5)
                    }
                }
                .glassEffect(glass, CapsuleShape, GlassTransition.Identity),
        )
        Row(
            Modifier
                .graphicsLayer {
                    val p = reveal.progress.value
                    // With morphing, the label travels with its glass from inside the button.
                    if (morph) translationY = (1f - p) * (buttonCenterY(size.height.roundToInt()) - size.height / 2f)
                    val scale = lerp(0.5f, 1f, p.coerceIn(0f, 1f))
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = if (morph) TransformOrigin(1f, 0.5f) else TransformOrigin.Center
                }
                .revealContent(reveal)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null)
            Text(title, fontWeight = FontWeight.SemiBold)
        }
    }
}
