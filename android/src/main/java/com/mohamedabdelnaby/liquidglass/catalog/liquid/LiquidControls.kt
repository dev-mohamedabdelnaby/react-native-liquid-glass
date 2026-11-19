// Ported from react-native-liquid's LiquidSwitchView and LiquidSliderView (the React Native view
// hosts dropped, the Compose bodies kept).
package com.mohamedabdelnaby.liquidglass.catalog.liquid

import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.glass.DropTintAlpha
import com.mohamedabdelnaby.liquidglass.catalog.glass.dropColorOf
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.emptyBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.layerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberCombinedBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberLayerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.drawBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.blur
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.lens
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.highlight.Highlight
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.shadow.InnerShadow
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.shadow.Shadow

private val Capsule = RoundedCornerShape(percent = 50)

/**
 * The iOS 26 switch: a 64×28 track with a white thumb that, pressed, lifts into a clear glass lens
 * refracting the track, stretches with its speed, then settles and frosts back to white.
 */
@Composable
fun LiquidSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onTint: Color? = null,
    // Tints the lifted drop; null follows the general setting.
    dropColor: Color? = null,
    // false keeps the thumb a solid knob (no liquid lift); null follows the general setting.
    liquid: Boolean? = null,
) {
    val dark = isSystemInDarkTheme()
    val drop = dropColorOf(dropColor)
    val settings = LocalGlassSettings.current
    val isLiquid = liquid ?: (settings.liquid && settings.switchLiquid)
    val accentColor = onTint ?: if (dark) Color(0xFF30D158) else Color(0xFF34C759)
    val trackColor = if (dark) Color(0xFF787880).copy(0.36f) else Color(0xFF787878).copy(0.2f)
    val backdrop = LocalGlassBackdrop.current ?: emptyBackdrop()
    val current by rememberUpdatedState(checked)
    val onChange by rememberUpdatedState(onCheckedChange)

    val dragWidth = with(LocalDensity.current) { 20.dp.toPx() }
    // RTL: on is at the start (left), as UISwitch; the thumb and the finger move the other way.
    val dir = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val currentDir by rememberUpdatedState(dir)
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (checked) 1f else 0f) }

    val drag = remember(animationScope) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.35f,
            onDragStarted = {},
            onDragStopped = {
                // A tap flips it; a drag settles on the nearer side.
                fraction = if (didDrag) (if (targetValue >= 0.5f) 1f else 0f) else (if (current) 0f else 1f)
                didDrag = false
                updateValue(fraction)
                val next = fraction == 1f
                if (next != current) onChange(next)
            },
            onDrag = { _, dragAmount ->
                if (!didDrag) didDrag = dragAmount.x != 0f
                fraction = (fraction + currentDir * dragAmount.x / dragWidth).fastCoerceIn(0f, 1f)
                updateValue(fraction)
            },
        )
    }
    LaunchedEffect(checked) {
        val target = if (checked) 1f else 0f
        if (target != fraction) {
            fraction = target
            drag.animateToValue(target)
        }
    }

    val trackBackdrop = rememberLayerBackdrop()
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.45f)
            .semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
                if (enabled) onClick { onChange(!current); true } else disabled()
            }
            // The whole switch takes touches, not only the thumb, at least 48 dp tall.
            .minimumInteractiveComponentSize()
            .then(if (enabled) drag.modifier else Modifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .clip(Capsule)
                .drawBehind { drawRect(lerp(trackColor, accentColor, drag.value)) }
                .size(64.dp, 28.dp),
        )
        Box(
            Modifier
                .graphicsLayer {
                    val padding = 2.dp.toPx()
                    translationX = dir * lerp(padding, padding + dragWidth, drag.value)
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = (if (isLiquid) drag.pressProgress else 0f)
                            // The track under the lens is shown almost at full size, so the lens stays clear.
                            scale(lerp(2f / 3f, 0.92f, progress), lerp(0f, 0.92f, progress)) { drawBackdrop() }
                        },
                    ),
                    shape = { Capsule },
                    effects = {
                        val progress = (if (isLiquid) drag.pressProgress else 0f)
                        blur(8.dp.toPx() * (1f - progress))
                        lens(4.dp.toPx() * progress, 8.dp.toPx() * progress)
                    },
                    highlight = {
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = (if (isLiquid) drag.pressProgress else 0f),
                        )
                    },
                    shadow = { Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.05f)) },
                    innerShadow = {
                        val progress = (if (isLiquid) drag.pressProgress else 0f)
                        InnerShadow(radius = 3.dp * progress, alpha = 0.35f * progress)
                    },
                    layerBlock = {
                        if (!isLiquid) return@drawBackdrop
                        scaleX = drag.scaleX
                        scaleY = drag.scaleY
                        val velocity = drag.velocity / 50f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = (if (isLiquid) drag.pressProgress else 0f)
                        // The solid thumb frosts away into clear glass, and back.
                        drawRect(Color.White.copy(alpha = 1f - progress))
                        drawRect(Color.White.copy(alpha = (if (dark) 0.03f else 0.06f) * progress))
                        drop?.let { drawRect(it.copy(alpha = DropTintAlpha * progress)) }
                    },
                )
                .size(40.dp, 24.dp),
        )
    }
}

/** The iOS 26 slider: a 6 dp track whose 40×24 thumb lifts into a clear glass lens while dragged. */
@Composable
fun LiquidSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    tint: Color? = null,
    contentDescription: String? = null,
    // Tints the lifted drop; null follows the general setting.
    dropColor: Color? = null,
    // false keeps the thumb a solid knob (no liquid lift); null follows the general setting.
    liquid: Boolean? = null,
) {
    val dark = isSystemInDarkTheme()
    val drop = dropColorOf(dropColor)
    val settings = LocalGlassSettings.current
    val isLiquid = liquid ?: (settings.liquid && settings.sliderLiquid)
    val accentColor = tint ?: if (dark) Color(0xFF0091FF) else Color(0xFF0088FF)
    val trackColor = if (dark) Color(0xFF787880).copy(0.36f) else Color(0xFF787878).copy(0.2f)
    val backdrop = LocalGlassBackdrop.current ?: emptyBackdrop()
    val min = valueRange.start
    val max = valueRange.endInclusive.coerceAtLeast(min + 0.0001f)
    val onChange by rememberUpdatedState(onValueChange)
    val trackBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                progressBarRangeInfo = ProgressBarRangeInfo(value, min..max)
                if (enabled) setProgress { onChange(it.coerceIn(min, max)); true } else disabled()
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = constraints.maxWidth
        // RTL: the value grows from the end (right), as UISlider; x is measured from the start.
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val dir = if (rtl) -1f else 1f
        val currentDir by rememberUpdatedState(dir)
        fun fromStart(x: Float) = if (rtl) trackWidth - x else x
        val animationScope = rememberCoroutineScope()
        var dragging by remember { mutableStateOf(false) }

        val drag = remember(animationScope, min, max, trackWidth) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = value.coerceIn(min, max),
                valueRange = min..max,
                visibilityThreshold = (max - min) / 1000f,
                initialScale = 1f,
                pressedScale = 1.5f,
                onDragStarted = { dragging = true },
                onDragStopped = { dragging = false },
                onDrag = { _, dragAmount ->
                    val target = (targetValue + (max - min) * (currentDir * dragAmount.x / trackWidth)).coerceIn(min, max)
                    updateValue(target)
                    onChange(target)
                },
            )
        }
        LaunchedEffect(drag, value) {
            if (!dragging && drag.targetValue != value) drag.updateValue(value)
        }

        // A tap anywhere on the 48 dp row jumps there; presses on the thumb belong to its drag.
        if (enabled) {
            Box(
                Modifier.matchParentSize().pointerInput(drag, trackWidth, rtl) {
                    var onThumb = false
                    detectTapGestures(
                        onPress = { onThumb = abs(fromStart(it.x) - trackWidth * drag.progress) < 20.dp.toPx() },
                        onTap = { position ->
                            if (onThumb) return@detectTapGestures
                            val tapped = min + (max - min) * (fromStart(position.x) / trackWidth).coerceIn(0f, 1f)
                            drag.animateToValue(tapped)
                            onChange(tapped)
                        },
                    )
                },
            )
        }

        Box(Modifier.layerBackdrop(trackBackdrop)) {
            Box(
                Modifier
                    .clip(Capsule)
                    .background(trackColor)
                    // The fill is drawn, not laid out, so it follows the thumb on every frame.
                    .drawWithContent {
                        drawContent()
                        val fill = size.width * drag.progress.coerceIn(0f, 1f)
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(if (rtl) size.width - fill else 0f, 0f),
                            size = Size(fill, size.height),
                            cornerRadius = CornerRadius(size.height / 2f),
                        )
                    }
                    .height(6.dp)
                    .fillMaxWidth(),
            )
        }

        Box(
            Modifier
                .graphicsLayer {
                    // The thumb sits at the start, so the same offset from it is mirrored in RTL.
                    translationX = dir * (-size.width / 2f + trackWidth * drag.progress)
                        .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f)
                }
                .then(if (enabled) drag.modifier else Modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = (if (isLiquid) drag.pressProgress else 0f)
                            scale(lerp(2f / 3f, 1f, progress), lerp(0f, 1f, progress)) { drawBackdrop() }
                        },
                    ),
                    shape = { Capsule },
                    effects = {
                        val progress = (if (isLiquid) drag.pressProgress else 0f)
                        blur(8.dp.toPx() * (1f - progress))
                        lens(6.dp.toPx() * progress, 10.dp.toPx() * progress)
                    },
                    highlight = {
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = (if (isLiquid) drag.pressProgress else 0f),
                        )
                    },
                    shadow = { Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.05f)) },
                    innerShadow = {
                        val progress = (if (isLiquid) drag.pressProgress else 0f)
                        InnerShadow(radius = 3.dp * progress, alpha = 0.3f * progress)
                    },
                    layerBlock = {
                        if (!isLiquid) return@drawBackdrop
                        scaleX = drag.scaleX
                        scaleY = drag.scaleY
                        val velocity = drag.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = (if (isLiquid) drag.pressProgress else 0f)
                        // The solid thumb frosts away into clear glass with a faint white body, and back.
                        drawRect(Color.White.copy(alpha = 1f - progress + 0.06f * progress))
                        drop?.let { drawRect(it.copy(alpha = DropTintAlpha * progress)) }
                    },
                )
                .size(40.dp, 24.dp),
        )
    }
}
