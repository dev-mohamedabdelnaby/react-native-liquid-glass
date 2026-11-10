// Ported from react-native-liquid's LiquidTabBarView (the React Native view host, touch dispatch,
// view-tree backdrops and events replaced by Compose), with the sizes and colours its
// LiquidBottomTabBar.tsx wrapper passes.
package com.mohamedabdelnaby.liquidglass.engine.liquid

import com.mohamedabdelnaby.liquidglass.engine.glass.opaqueFill
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.engine.glass.DropTintAlpha
import com.mohamedabdelnaby.liquidglass.engine.glass.dropColorOf
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.backdrops.emptyBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.backdrops.layerBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.backdrops.rememberBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.backdrops.rememberCombinedBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.backdrops.rememberLayerBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.drawBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.effects.blur
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.effects.lens
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.effects.vibrancy
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.highlight.Highlight
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.shadow.InnerShadow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** LiquidBottomTabBar.tsx: the capsule's height. */
private const val BAR_HEIGHT_DP = 62f
/** LiquidBottomTabBar.tsx: `blur` (0.85) for the default `glassStyle` 'clear'. */
private const val BACKDROP_BLUR = 0.85f

private const val INSET_DP = 4f
/** Scale of the lens while lifted, relative to the resting pill. */
// The lifted lens is an elliptical drop, wider than the pill.
private const val PRESSED_SCALE_X = 1.38f
private const val PRESSED_SCALE_Y = 1.2f
/** Spring stiffness of the drop melting back into the pill. */
private const val SETTLE_STIFFNESS = 1100f

/**
 * The drop returns to its normal size faster than it melts, so it is still liquid, at tab size,
 * for a moment before it becomes the pill.
 */
private const val SETTLE_SCALE_STIFFNESS = 1300f

/** Spring stiffness of the lens travelling to a tapped tab, and following a finger. */
private const val TAP_TRAVEL_STIFFNESS = 330f
private const val DRAG_STIFFNESS = 1000f
/** Extra scale of every tab while the lens is lifted. */
private const val TAB_LIFT_SCALE = 0f
/** Extra scale of the tab under the lens. */
private const val TAB_MAGNIFICATION = 0.12f
/**
 * How many rendered frames the lens stays lifted over its tab before settling. Counted in frames,
 * so a main-thread stall (mounting the new screen) doesn't use up the hold.
 */
private const val HOLD_FRAMES = 1

/**
 * How close (in tabs) the lens must be to its tab to count as arrived. Loose, so the drop starts
 * melting as it lands instead of crawling the last bit lifted.
 */
private const val ARRIVE_DISTANCE = 0.08f
/** How close (in tabs) the lens gets before the tap is reported and the new screen mounts. */
private const val SELECT_DISTANCE = 0.25f
/** Reports the tap after this long even if the lens hasn't come close yet. */
private const val SELECT_FALLBACK_MS = 250L
/** How far (in tabs) the lens can be pulled past the first and last tab. */
private const val MAX_OVERSHOOT = 0.35f
/** How much of the overshoot stretches the bar, in tab widths. */
private const val BAR_STRETCH = 0.45f

/** A tab of [LiquidTabBar]. */
/** [badge] above 0 draws a red count on the icon's trailing side, as `.badge(_:)`. */
data class LiquidTabItem(val label: String, val icon: ImageVector? = null, val badge: Int = 0)

/** Animated values, written once per frame by the physics loop. */
@Stable
private class TabBarState {
    /** Position of the lens, in tabs. */
    var value by mutableFloatStateOf(0f)
    /** 0 when resting as the pill, 1 when lifted. */
    var press by mutableFloatStateOf(0f)
    var scaleX by mutableFloatStateOf(1f)
    var scaleY by mutableFloatStateOf(1f)
    /** Speed of the lens in tabs per second. */
    var velocity by mutableFloatStateOf(0f)
    /**
     * The tab tapped last, shown in the active colour until it is selected, so the active tab reads
     * as soon as the drop lands instead of after the round trip.
     */
    var landingIndex by mutableIntStateOf(-1)
}

/** LiquidTabBarView's springs, touches and frame loop. */
private class TabBarPhysics(private val scope: CoroutineScope) {

    val state = TabBarState()
    var tabCount = 1
    var rtl = false
    var onSelect: (Int) -> Unit = {}
    // The bar's width and inset in px, set by the touch handler.
    var width = 0f
    var inset = 0f

    private val value = LiquidSpring(1f, 1000f, 0.001f)
    private val press = LiquidSpring(1f, 1000f, 0.001f)
    // Near-critical damping: the lens grows and shrinks without wobbling.
    private val scaleX = LiquidSpring(0.85f, 420f, 0.001f).apply { snap(1f) }
    private val scaleY = LiquidSpring(0.9f, 420f, 0.001f).apply { snap(1f) }
    // A slow, well-damped velocity spring makes the drop heavy: it deforms gradually.
    private val velocity = LiquidSpring(0.95f, 45f, 0.01f)

    private var selectedIndex = 0
    // Tab chosen on release, reported as the lens lands (see flushPendingSelect).
    private var pendingSelect = -1
    private var selectFallback: Job? = null
    private var hasSelection = false
    private var touching = false
    // Set once the finger moves: the lens then follows it instead of snapping to tabs.
    private var dragging = false
    private var releasePending = false
    private var animating = false
    private var lastFrameNanos = 0L
    private var previousValue = 0f
    // Frames rendered since the lens arrived over its tab after release, -1 while travelling.
    private var arrivedFrames = -1
    private var downX = 0f

    private val tabWidth: Float
        get() = (width - inset * 2) / tabCount

    /** Lens position under [x], rubber-banded past the first and last tab. */
    private fun positionFor(x: Float): Float {
        val last = (tabCount - 1).toFloat()
        val mirrored = if (rtl) width - x else x
        val raw = (mirrored - inset) / tabWidth - 0.5f
        return when {
            raw < 0f -> -rubberBand(-raw)
            raw > last -> last + rubberBand(raw - last)
            else -> raw
        }
    }

    private fun rubberBand(excess: Float): Float = MAX_OVERSHOOT * excess / (excess + 1f)

    private fun indexFor(x: Float): Int = positionFor(x).roundToInt().coerceIn(0, tabCount - 1)

    fun setSelectedIndex(index: Int) {
        if (hasSelection && index == selectedIndex) {
            return
        }
        val animated = hasSelection && abs(value.value - index) > 0.05f
        hasSelection = true
        selectedIndex = index
        // The caller now colours the tabs itself.
        state.landingIndex = -1
        // Not in the lib: the selection a release reports comes back a frame later, when the lens
        // is already on its way there; lifting it again would make the landing drop bounce.
        if (touching || value.target == index.toFloat()) {
            return
        }
        if (animated) {
            // Lift the lens while it travels to the new tab.
            value.stiffness = TAP_TRAVEL_STIFFNESS
            lift()
            value.target = index.toFloat()
            releasePending = true
            startAnimating()
        } else {
            value.snap(index.toFloat())
            render()
        }
    }

    // Touches

    fun onDown(x: Float) {
        touching = true
        releasePending = false
        downX = x
        dragging = false
        lift()
        // A tap glides straight to the tab under the finger.
        value.stiffness = TAP_TRAVEL_STIFFNESS
        value.target = indexFor(x).toFloat()
        startAnimating()
    }

    fun onMove(x: Float, touchSlop: Float) {
        if (abs(x - downX) > touchSlop) {
            if (!dragging) {
                value.stiffness = DRAG_STIFFNESS
            }
            dragging = true
        }
        value.target = if (dragging) positionFor(x) else indexFor(x).toFloat()
        startAnimating()
    }

    fun onUp(x: Float) {
        touching = false
        val index = indexFor(x)
        state.landingIndex = index
        value.target = index.toFloat()
        releasePending = true
        startAnimating()
        pendingSelect = index
        // Select as the lens lands (onFrame), not on release, so the new screen composes after the
        // travel instead of during it. The timer is a fallback in case the lens never arrives.
        selectFallback?.cancel()
        selectFallback = scope.launch {
            delay(SELECT_FALLBACK_MS)
            flushPendingSelect()
        }
    }

    fun onCancel() {
        touching = false
        state.landingIndex = -1
        pendingSelect = -1
        value.target = selectedIndex.toFloat()
        releasePending = true
        startAnimating()
    }

    /** Reports the tab chosen on release once the lens reaches it (or after [SELECT_FALLBACK_MS]). */
    private fun flushPendingSelect() {
        selectFallback?.cancel()
        val index = pendingSelect
        if (index < 0) {
            return
        }
        pendingSelect = -1
        onSelect(index)
    }

    // Physics

    private fun lift() {
        // Lift quickly, settle a little softer, like the iOS bar.
        press.stiffness = 1400f
        // A little bounce on the way up, like a drop of liquid.
        scaleX.dampingRatio = 0.85f
        scaleY.dampingRatio = 0.9f
        scaleX.stiffness = 600f
        scaleY.stiffness = 600f
        press.target = 1f
        scaleX.target = PRESSED_SCALE_X
        scaleY.target = PRESSED_SCALE_Y
    }

    private fun settle() {
        // The drop first loses its reflections and shrinks back to tab size while still liquid, then
        // melts into the pill.
        press.stiffness = SETTLE_STIFFNESS
        // Critically damped on the way back, so the size eases in without a wobble.
        scaleX.dampingRatio = 1f
        scaleY.dampingRatio = 1f
        scaleX.stiffness = SETTLE_SCALE_STIFFNESS
        scaleY.stiffness = SETTLE_SCALE_STIFFNESS
        press.target = 0f
        scaleX.target = 1f
        scaleY.target = 1f
    }

    private fun startAnimating() {
        if (!animating) {
            animating = true
            lastFrameNanos = 0L
            previousValue = value.value
            scope.launch { while (withFrameNanos(::onFrame)) Unit }
        }
    }

    /** Steps the springs one frame; returns whether to keep animating. */
    private fun onFrame(frameTimeNanos: Long): Boolean {
        val dt = if (lastFrameNanos == 0L) 1f / 60f
        else ((frameTimeNanos - lastFrameNanos) / 1e9f).coerceIn(1f / 240f, 1f / 30f)
        lastFrameNanos = frameTimeNanos

        var moving = value.step(dt)
        velocity.target = (value.value - previousValue) / dt
        previousValue = value.value
        moving = velocity.step(dt) || moving

        if (pendingSelect >= 0 && abs(value.value - value.target) < SELECT_DISTANCE) {
            flushPendingSelect()
        }

        // The lens arrives, stays lifted for a moment, then settles into the pill.
        if (releasePending && abs(value.value - value.target) < ARRIVE_DISTANCE) {
            if (arrivedFrames < 0) {
                arrivedFrames = 0
                flushPendingSelect()
            } else if (++arrivedFrames >= HOLD_FRAMES) {
                releasePending = false
                arrivedFrames = -1
                settle()
            }
        } else {
            arrivedFrames = -1
        }

        moving = press.step(dt) || moving
        moving = scaleX.step(dt) || moving
        moving = scaleY.step(dt) || moving

        render()

        val keepGoing = moving || touching || releasePending
        if (!keepGoing) {
            animating = false
        }
        return keepGoing
    }

    private fun render() {
        state.value = value.value
        state.press = press.value.coerceIn(0f, 1f)
        state.scaleX = scaleX.value
        state.scaleY = scaleY.value
        state.velocity = velocity.value
    }
}

/**
 * Liquid Glass tab bar: a floating glass capsule with equal-width tabs and a selection pill.
 * Pressing lifts a clear glass drop out of the pill; it follows the finger with spring physics,
 * stretches with its speed, magnifies the tab under it and settles into the pill on release, when
 * [onSelect] reports the tab. [selectedIndex] glides the pill there.
 */
@Composable
fun LiquidTabBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    tabs: List<LiquidTabItem>,
    modifier: Modifier = Modifier,
    // The lib's top tab bar is the same bar, text only and lower.
    height: Dp = BAR_HEIGHT_DP.dp,
    // iOS's segmented picker: a flat gray track, a white thumb and label-coloured text; it still lifts the drop.
    segmented: Boolean = false,
    // A solid fill in place of the bar glass; labels turn white on a dark fill.
    color: Color? = null,
    // Tints the lifted drop; null follows the general setting.
    dropColor: Color? = null,
    // false: frosted bar glass without the lens (segmented controls lose their drop); null follows the setting.
    liquid: Boolean? = null,
    // false: the pill only slides, no drop lifts; null follows the setting.
    showDrop: Boolean? = null,
) {
    val dark = isSystemInDarkTheme()
    val drop = dropColorOf(dropColor)
    val glassSettings = LocalGlassSettings.current
    val isLiquid = liquid ?: (glassSettings.liquid && if (segmented) glassSettings.segmentedLiquid else glassSettings.tabBarLiquid)
    val dropOn = isLiquid && (showDrop ?: (segmented || glassSettings.tabBarDrop))
    val opaque = LocalGlassSettings.current.opaque
    val barTint = LocalGlassSettings.current.tint
    val insetDp = if (segmented) 2.dp else INSET_DP.dp
    // theme.ts colours LiquidBottomTabBar passes: tint, label, tabSelection and tabBarGlass.
    val onDarkFill = color != null && color.luminance() < 0.5f
    val inactiveColor = if (onDarkFill) Color.White.copy(alpha = 0.75f) else if (dark) Color.White else Color.Black
    val activeColor = when {
        onDarkFill -> Color.White
        segmented -> inactiveColor
        dark -> Color(0xFF0A84FF)
        else -> Color(0xFF007AFF)
    }
    val pillColor = if (segmented) (if (dark) Color(0xFF636366) else Color.White) else Color.Black.copy(alpha = if (dark) 0.42f else 0.06f)
    // tertiarySystemFill, the track UISegmentedControl draws.
    val trackColor = Color(0xFF767680).copy(alpha = if (dark) 0.24f else 0.12f)
    val tint = LocalGlassSettings.current.tint ?: if (dark) Color(0xFF2C2C2E).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.18f)
    // Dark glass stays a visible gray even over black content, like iOS, so the darker pill shows.
    // The 'clear' style draws a tenth of it.
    val containerColor = (if (dark) Color(0xFF2C2C2E) else Color(0xFFFAFAFA)).copy(alpha = 0.1f)
    val backdrop = LocalGlassBackdrop.current ?: emptyBackdrop()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val count = tabs.size.coerceAtLeast(1)
    val capsule = RoundedCornerShape(percent = 50)

    val scope = rememberCoroutineScope()
    val physics = remember(scope) { TabBarPhysics(scope) }
    val state = physics.state
    // What the visuals see of the press: nothing when the drop is off, so the pill just slides.
    val press = { if (dropOn) state.press else 0f }
    SideEffect {
        physics.tabCount = count
        physics.rtl = rtl
        physics.onSelect = onSelect
        physics.setSelectedIndex(selectedIndex.coerceIn(0, count - 1))
    }

    // What the drop shows: the page, the bar glass and the tabs in the active colour, magnified.
    val barLayer = rememberLayerBackdrop()
    val tabsLayer = rememberLayerBackdrop()
    val lensBackdrop = rememberBackdrop(rememberCombinedBackdrop(backdrop, barLayer, tabsLayer)) { drawBackdrop ->
        scale(1f + 0.05f * press()) { drawBackdrop() }
    }

    Box(
        modifier
            .height(height)
            .pointerInput(physics) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    physics.width = size.width.toFloat()
                    physics.inset = insetDp.toPx()
                    physics.onDown(down.position.x)
                    down.consume()
                    var released = false
                    try {
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if (change.changedToUpIgnoreConsumed()) {
                                physics.onUp(change.position.x)
                                released = true
                                break
                            }
                            physics.onMove(change.position.x, viewConfiguration.touchSlop)
                            change.consume()
                        }
                    } finally {
                        if (!released) physics.onCancel()
                    }
                }
            },
    ) {
        // TabBarBackground: the bar glass and the resting selection pill, below the tabs.
        Box(
            Modifier
                .matchParentSize()
                .layerBackdrop(barLayer)
                .then(if (segmented) Modifier.background(trackColor, capsule) else if (color != null) Modifier.background(color, capsule) else Modifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { capsule },
                    effects = {
                        vibrancy()
                        // Not liquid: a frosted material bar.
                        blur((if (isLiquid) 2f + 6f * BACKDROP_BLUR else 20f).dp.toPx())
                        if (isLiquid) lens(24f.dp.toPx(), 24f.dp.toPx())
                    },
                    layerBlock = {
                        // The bar grows slightly while pressed and stretches toward a finger pulled past its ends.
                        val width = size.width.coerceAtLeast(1f)
                        val tabWidth = (size.width - insetDp.toPx() * 2) / count
                        val overshoot = state.value - state.value.coerceIn(0f, (count - 1).toFloat())
                        val stretch = overshoot * tabWidth * BAR_STRETCH * (if (rtl) -1f else 1f)
                        val scale = 1f + 6f.dp.toPx() / width * press()
                        scaleX = scale + abs(stretch) / width
                        scaleY = scale
                        translationX = stretch / 2f
                    },
                    onDrawSurface = {
                        drawRect(containerColor)
                        // A neutral tint (white or gray frost) gets no hue blend, only its 60% wash.
                        drawRect(tint.copy(alpha = tint.alpha * 0.6f))
                        if (opaque) drawRect(opaqueFill(barTint, dark))
                    },
                ))
                .drawBehind {
                    // The pill fades out as the lens lifts off it.
                    val inset = insetDp.toPx()
                    val tabWidth = (size.width - inset * 2) / count
                    val height = size.height - inset * 2
                    val position = state.value.coerceIn(0f, (count - 1).toFloat())
                    if (segmented) {
                        // The thumb's soft drop shadow.
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.08f * (1f - press())),
                            topLeft = Offset(pillLeft(rtl, size.width, inset, tabWidth, position), inset + 1.dp.toPx()),
                            size = Size(tabWidth, height),
                            cornerRadius = CornerRadius(height / 2f),
                        )
                    }
                    drawRoundRect(
                        color = pillColor.copy(alpha = pillColor.alpha * (1f - press())),
                        topLeft = Offset(pillLeft(rtl, size.width, inset, tabWidth, position), inset),
                        size = Size(tabWidth, height),
                        cornerRadius = CornerRadius(height / 2f),
                    )
                },
        )

        val isSelected = { i: Int -> i == selectedIndex || i == state.landingIndex }
        TabItems(
            tabs, state, press, insetDp, segmented, isSelected,
            Modifier.matchParentSize().selectableGroup(),
            itemModifier = { i ->
                Modifier.clearAndSetSemantics {
                    role = Role.Tab
                    selected = i == selectedIndex
                    contentDescription = tabs[i].label
                    onClick { onSelect(i); true }
                }
            },
            color = { i -> if (isSelected(i)) activeColor else inactiveColor },
        )

        // A copy of the tabs in the active colour, seen only through the drop.
        TabItems(
            tabs, state, press, insetDp, segmented, isSelected,
            Modifier.matchParentSize().clearAndSetSemantics {}.alpha(0f).layerBackdrop(tabsLayer),
            itemModifier = { Modifier },
            color = { activeColor },
        )

        // TabBarLens: the clear glass drop, above the tabs so it refracts and magnifies them.
        Box(
            Modifier
                .align(AbsoluteAlignment.TopLeft)
                .padding(insetDp)
                .fillMaxHeight()
                .fillMaxWidth(1f / count)
                .graphicsLayer {
                    // Moved in the draw phase so following the finger never waits for a layout pass.
                    val inset = insetDp.toPx()
                    translationX = pillLeft(rtl, size.width * count + inset * 2, inset, size.width, state.value) - inset
                    // Fade with the press, eased so the lens melts into the pill instead of popping.
                    // The drop stays fully glassy while it shrinks and only fades in the last part.
                    alpha = (press() * 1.25f).coerceAtMost(1f).let { p -> p * p * p * (p * (p * 6f - 15f) + 10f) }
                }
                .drawBackdrop(
                    backdrop = lensBackdrop,
                    shape = { capsule },
                    effects = {
                        // Refraction stays strong until the very end so the drop never looks blurry.
                        val progress = sqrt(press())
                        // Light refraction, no color fringes: the rim bends the content without mirroring it.
                        lens(8f.dp.toPx() * progress, 12f.dp.toPx() * progress)
                    },
                    // Reflections fade out during the first part of the settle, before the drop melts.
                    highlight = {
                        // Spread over most of the settle and eased with smootherstep, so it fades gently.
                        val reflect = ((press() - 0.2f) / 0.8f).coerceIn(0f, 1f).let { it * it * it * (it * (it * 6f - 15f) + 10f) }
                        // Like the iOS drop: a faint glint on a hairline, not a bold white outline.
                        Highlight.Default.copy(width = 0.75.dp, alpha = 0.45f * reflect)
                    },
                    shadow = null,
                    innerShadow = { InnerShadow(radius = 8f.dp * press(), alpha = 0.6f * press()) },
                    // Scaled here, not in the layer above, so the backdrop undoes it and the drop magnifies
                    // only by its zoom. Like liquid, it stretches along its motion and thins out while it travels.
                    layerBlock = {
                        val stretch = (abs(state.velocity) / 10f * 0.24f).coerceAtMost(0.13f)
                        scaleX = state.scaleX / (1f - stretch)
                        scaleY = state.scaleY * (1f - stretch / 3f)
                    },
                    // A light fill gives the drop density while it stays see-through.
                    onDrawSurface = {
                        // Drawn in the drop's own shape so the fill never shows square corners.
                        drawRoundRect(
                            // Dark glass in dark mode, so the drop matches the dark bar.
                            color = when {
                                drop != null -> drop.copy(alpha = (DropTintAlpha * press()).coerceIn(0f, 1f))
                                dark -> Color(0xFF1C1C1E).copy(alpha = (0.42f * press()).coerceIn(0f, 1f))
                                else -> Color.White.copy(alpha = (0.22f * press()).coerceIn(0f, 1f))
                            },
                            cornerRadius = CornerRadius(size.height / 2f),
                        )
                        // The drop's edge, like iOS: a fine hairline in the glass's own shade.
                        val hair = 0.5.dp.toPx()
                        drawRoundRect(
                            color = (if (dark) Color.White else Color.Black).copy(alpha = 0.2f * press()),
                            topLeft = Offset(hair / 2f, hair / 2f),
                            size = Size(size.width - hair, size.height - hair),
                            cornerRadius = CornerRadius((size.height - hair) / 2f),
                            style = Stroke(hair),
                        )
                    },
                ),
        )
    }
}

/** Equal-width tabs (icon over a 10 sp label); every tab lifts and the one under the drop grows. */
@Composable
private fun TabItems(
    tabs: List<LiquidTabItem>,
    state: TabBarState,
    press: () -> Float,
    inset: Dp,
    segmented: Boolean,
    isSelected: (index: Int) -> Boolean,
    modifier: Modifier,
    itemModifier: (index: Int) -> Modifier,
    color: (index: Int) -> Color,
) {
    // Matches the inset of the selection pill.
    Row(modifier.padding(horizontal = inset)) {
        tabs.forEachIndexed { i, tab ->
            Column(
                itemModifier(i)
                    .weight(1f)
                    .fillMaxHeight()
                    .graphicsLayer {
                        val clamped = state.value.coerceIn(0f, (tabs.size - 1).toFloat())
                        val proximity = (1f - abs(clamped - i)).coerceAtLeast(0f)
                        val scale = 1f + (TAB_LIFT_SCALE + TAB_MAGNIFICATION * proximity) * press()
                        scaleX = scale
                        scaleY = scale
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val tabColor = color(i)
                tab.icon?.let {
                    Box {
                        Icon(it, contentDescription = null, Modifier.size(24.dp), tint = tabColor)
                        if (tab.badge > 0) {
                            Text(
                                "${tab.badge}",
                                Modifier
                                    .align(AbsoluteAlignment.TopRight)
                                    .offset(x = 12.dp, y = (-4).dp)
                                    .defaultMinSize(18.dp, 18.dp)
                                    .background(Color(0xFFFF3B30), RoundedCornerShape(percent = 50))
                                    .wrapContentSize(),
                                color = Color.White,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Text(
                    tab.label,
                    Modifier.padding(top = if (tab.icon != null) 2.dp else 0.dp),
                    color = tabColor,
                    // UISegmentedControl: 13 pt, semibold only on the selected segment.
                    fontSize = if (tab.icon != null) 10.sp else if (segmented) 13.sp else 14.sp,
                    lineHeight = if (tab.icon != null) 12.sp else 18.sp,
                    fontWeight = if (segmented && !isSelected(i)) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Left edge of the pill at [position] in tabs, mirrored in right-to-left layout. */
private fun pillLeft(rtl: Boolean, width: Float, inset: Float, tabWidth: Float, position: Float): Float =
    if (rtl) width - inset - (position + 1f) * tabWidth else inset + position * tabWidth
