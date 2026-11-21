package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.ui.graphics.Shape
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.liquid.MERGED_GLASS_MAX_SHAPES
import com.mohamedabdelnaby.liquidglass.catalog.liquid.MergedGlassLook
import com.mohamedabdelnaby.liquidglass.catalog.liquid.MergedGlassShape
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.Backdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.BackdropEffectScope
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.LayerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.drawBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.blur
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.iosLens
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.vibrancy
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.highlight.Highlight
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.shadow.Shadow
import com.mohamedabdelnaby.liquidglass.catalog.liquid.mergedGlass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tanh

/** SwiftUI's `.capsule`. */
val CapsuleShape = RoundedCornerShape(50)

/** SwiftUI's `.rect`: square corners. */
val RectShape = RoundedCornerShape(0.dp)

val GlassCircle: RoundedCornerShape = CircleShape

/**
 * Mirrors SwiftUI's `Glass`: `Glass.Regular.tint(SystemColors.Orange).interactive()`.
 *
 * [blur], [refractionHeight], [refractionAmount] and [chromaticAberration] are the lens props of
 * react-native-liquid's `LiquidGlassView`; `null` blur takes the variant's (20 dp regular, 2 dp clear).
 */
@Immutable
data class Glass(
    val variant: Variant = Variant.Regular,
    val tint: Color? = null,
    val interactive: Boolean = false,
    val blur: Dp? = null,
    val refractionHeight: Dp = 12.dp,
    val refractionAmount: Dp = 24.dp,
    val chromaticAberration: Boolean = false,
    /** iOS's `.glassProminent`: pressed, it only lightens; it doesn't swell or stretch. */
    val prominent: Boolean = false,
    /**
     * Per-item looks; null follows Home's general Glass settings. [ownVariant] keeps this glass's
     * Regular/Clear even when the general setting says Clear.
     */
    val white: Float? = null,
    val black: Float? = null,
    val edgeLight: Float? = null,
    val ownVariant: Boolean = false,
    /** A solid fill: nothing behind the glass shows through. */
    val opaque: Boolean = false,
    /** false: a frosted blur without the liquid lens, rim light, press swell or merging. */
    val liquid: Boolean = true,
) {
    enum class Variant { Regular, Clear }

    /** A glass tint, drawn like react-native-liquid's (a hue shift plus the color at 75%). */
    fun tint(color: Color?): Glass = copy(tint = color)

    fun interactive(enabled: Boolean = true): Glass = copy(interactive = enabled)

    internal val clear get() = variant == Variant.Clear
    // react-native-liquid's LiquidGlassView defaults: frosted regular glass, nearly clear clear glass.
    internal val blurDp get() = blur ?: if (clear) 2.dp else 20.dp

    companion object {
        val Regular = Glass()
        val Clear = Glass(Variant.Clear)
    }
}

/**
 * App-wide glass settings (Home's Glass section), like the Glass Lab in react-native-liquid's
 * playground: every untinted glass takes this variant, blur, white/black wash and edge light.
 */
@Immutable
data class GlassSettings(
    val clear: Boolean = true,
    val blur: Dp = 3.dp,
    val white: Float = 0.25f,
    val black: Float = 0.05f,
    val edgeLight: Float = 0.15f,
    val refractionHeight: Dp = 14.dp,
    val refractionAmount: Dp = 24.dp,
    // The liquid drop's tint on tab bars, segmented controls, sliders and switches; null keeps it clear.
    val dropColor: Color? = null,
    // A tint for every glass surface that doesn't bring its own; null keeps the white and black washes.
    val tint: Color? = null,
    // Every glass surface fills solid (its tint, the general tint, or the surface color).
    val opaque: Boolean = false,
    // Android only: false draws every glass as a frosted blur without the liquid lens.
    val liquid: Boolean = true,
    // Android only: each control's liquid lift; off, the slider and switch thumbs stay solid knobs,
    // segmented controls and tab bars slide their pill without the drop.
    val sliderLiquid: Boolean = true,
    val switchLiquid: Boolean = true,
    val segmentedLiquid: Boolean = true,
    // Bottom tab bars: their glass (frosted when off) and, separately, the drop.
    val tabBarLiquid: Boolean = true,
    val tabBarDrop: Boolean = true,
    // Android Material only: the check mark on the selected segment of segmented buttons.
    val segmentedCheck: Boolean = true,
    // Every screen text's family (the theme's type and the texts that build their own style); null is the system font.
    val fontFamily: FontFamily? = null,
)

val LocalGlassSettings = staticCompositionLocalOf { GlassSettings() }

/** A control's own drop color, or the general one. */
@Composable
internal fun dropColorOf(override: Color?): Color? = override ?: LocalGlassSettings.current.dropColor

/** How strongly a picked color tints the drop at full press: enough to read, still see-through. */
internal const val DropTintAlpha = 0.4f

/** react-native-liquid's `whiteBlackColor`: white and black washes (0 to 1, each up to 70%) make a grey tint. */
internal fun whiteBlackWash(white: Float, black: Float): Color? {
    val w = white.coerceIn(0f, 1f) * 0.7f
    val b = black.coerceIn(0f, 1f) * 0.7f
    val alpha = 1f - (1f - w) * (1f - b)
    if (alpha <= 0f) return null
    val gray = (1f - b) * w / alpha
    return Color(gray, gray, gray, alpha)
}

/** The glass as the settings shape it; tinted glass (panels, prominent buttons) keeps its own look. */
internal fun Glass.styled(settings: GlassSettings): Glass {
    // Tinted glass (panels, prominent buttons) keeps its color and variant.
    val tinted = tint != null
    // shortcut: lens values equal to the defaults (12 / 24 dp) follow the settings, so an item can't pin
    // exactly those; make them nullable like blur if that matters.
    val defaults = Glass()
    return copy(
        variant = if (settings.clear && !ownVariant && !tinted) Glass.Variant.Clear else variant,
        tint = if (opaque || settings.opaque) (tint ?: settings.tint)?.copy(alpha = 1f)
        else tint ?: settings.tint ?: whiteBlackWash(white ?: settings.white, black ?: settings.black),
        opaque = opaque || settings.opaque,
        liquid = liquid && settings.liquid,
        edgeLight = edgeLight ?: settings.edgeLight,
        blur = blur ?: settings.blur,
        refractionHeight = if (refractionHeight == defaults.refractionHeight) settings.refractionHeight else refractionHeight,
        refractionAmount = if (refractionAmount == defaults.refractionAmount) settings.refractionAmount else refractionAmount,
    )
}

/** Glass for menus, dialogs and sheets: a surface-colored wash keeps their text legible. */
@Composable
fun panelGlass(): Glass = Glass.Regular.tint(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)).copy(blur = 16.dp)

/** Mirrors `glassEffectTransition(_:)`. */
enum class GlassTransition { MatchedGeometry, Materialize, Identity }

/** What every glass surface on a screen refracts. Provided by [GlassScreen]/[GlassSurfaceHost]. */
val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/** The bouncy spring used across the catalog (SwiftUI's `.bouncy`). */
val BouncySpring = spring<Float>(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)

/** The iOS 26 lens shader needs AGSL (API 33); API 31–32 get blur and vibrancy, older a frost. */
private val lensSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/**
 * Mirrors SwiftUI's `glassEffect(_:in:)` with react-native-liquid's renderer: backdrop blur, the
 * iOS 26 lens (rim-packed refraction, vibrancy, two-lobe rim light), frost, tint and edge light.
 * Interactive glass swells when pressed, stretches along a drag and wobbles back.
 *
 * Inside a [GlassEffectContainer] the container draws this shape merged with its neighbours;
 * [transition] is how it enters and leaves (`glassEffectID` morphing), and members with an equal
 * [unionId] draw as one shape (`glassEffectUnion`).
 */
@Composable
fun Modifier.glassEffect(
    glass: Glass = Glass.Regular,
    shape: RoundedCornerShape = CapsuleShape,
    transition: GlassTransition = GlassTransition.MatchedGeometry,
    unionId: Any? = null,
    // false while the content animates out, so a container's glass leaves alongside the content
    // fading (as on iOS) instead of after it; true again brings it back.
    present: Boolean = true,
    // Records this glass (without its content) for glass drawn on it to refract, outside a container.
    exportTo: LayerBackdrop? = null,
): Modifier {
    if (isMaterial) return materialSurface(glass.tint, shape)
    val backdrop = LocalGlassBackdrop.current ?: return this
    val settings = LocalGlassSettings.current
    val glass = remember(glass, settings) { glass.styled(settings) }
    val dark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    // Not keyed on prominence: glass turning prominent mid-press (a picker's selected button) keeps
    // its press and lets its swell go as usual.
    val elastic = if (glass.interactive) remember(scope) { ElasticPress(scope) }.also { it.swells = !glass.prominent } else null
    val input = elastic?.let { Modifier.elasticInput(it) } ?: Modifier
    // Not liquid: plain frosted material that neither refracts nor merges with its container.
    if (!glass.liquid) return this.frostedGlass(glass, shape, exportTo = exportTo)

    val container = LocalGlassContainer.current
    if (container != null) {
        val member = remember(container) { GlassMember() }
        SideEffect {
            member.glass = glass
            member.shape = shape
            member.dark = dark
            member.unionId = unionId
            member.transition = transition
            member.elastic = elastic
        }
        DisposableEffect(container, member, present) {
            if (present) container.restore(member) else container.leave(member)
            onDispose { if (present) container.leave(member) }
        }
        return this
            .onGloballyPositioned { member.coordinates = it; container.sync() }
            .graphicsLayer(remember(member) { member.followLayer() })
            .then(input)
            .then(elastic?.let { Modifier.graphicsLayer(it.layerBlock) } ?: Modifier)
    }

    return this
        .then(input)
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = { glassEffects(glass, shape, dark, elastic) },
            highlight = { if (lensSupported) null else if (dark) Highlight.Ambient else Highlight.Default },
            shadow = { Shadow(radius = 16.dp, color = Color.Black.copy(alpha = if (dark) 0.3f else 0.12f)) },
            layerBlock = elastic?.layerBlock,
            exportedBackdrop = exportTo,
            onDrawSurface = {
                drawFrost(glass, dark, elastic?.press?.value ?: 0f)
                drawEdgeLight(shape.topStart.toPx(size, this), glass.edgeLight ?: settings.edgeLight)
            },
        )
}

private fun BackdropEffectScope.glassEffects(glass: Glass, shape: RoundedCornerShape, dark: Boolean, elastic: ElasticPress?) {
    if (!lensSupported) vibrancy()
    blur(glass.blurDp.toPx())
    if (!lensSupported) return
    iosLens(
        cornerRadius = shape.topStart.toPx(size, this),
        // react-native-liquid's factors: the iOS profile at the classic 12 / 24 dp props.
        bevel = glass.refractionHeight.toPx() * 1.33f,
        refraction = glass.refractionAmount.toPx() * 2.2f,
        dispersion = if (glass.chromaticAberration) 0.15f else 0f,
        specular = (if (dark) 0.85f else 1f) * (if (glass.clear) 1.2f else 1f),
        press = elastic?.press?.value ?: 0f,
        touchX = elastic?.touch?.x ?: 0f,
        touchY = elastic?.touch?.y ?: 0f,
    )
}

/** react-native-liquid's surface: frost, tint and the press glow over the lensed backdrop. */
/** Frosted material in any [shape]: the glass's blur, frost and tint without the liquid lens. */
@Composable
fun Modifier.frostedGlass(glass: Glass = Glass.Regular, shape: Shape = CapsuleShape, shadow: Boolean = true, exportTo: LayerBackdrop? = null): Modifier {
    if (isMaterial) return materialSurface(glass.tint, shape)
    val backdrop = LocalGlassBackdrop.current ?: return this
    val styled = glass.styled(LocalGlassSettings.current).copy(variant = Glass.Variant.Regular)
    val dark = isSystemInDarkTheme()
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(FrostedBlur.toPx())
        },
        highlight = null,
        shadow = if (shadow) { { Shadow(radius = 12.dp, color = Color.Black.copy(alpha = if (dark) 0.25f else 0.08f)) } } else null,
        exportedBackdrop = exportTo,
        onDrawSurface = { drawFrost(styled, dark, 0f) },
    )
}

/** A material's blur: enough that what's behind reads as color only. */
private val FrostedBlur = 20.dp

/** What opaque glass fills with: its tint, or the plain surface color when it has none. */
internal fun opaqueFill(tint: Color?, dark: Boolean): Color = tint?.copy(alpha = 1f) ?: if (dark) Color(0xFF1C1C1E) else Color.White

private fun DrawScope.drawFrost(glass: Glass, dark: Boolean, press: Float) {
    if (glass.opaque) drawRect(opaqueFill(glass.tint, dark)) else drawRect(frostColor(glass.clear, dark))
    if (!glass.opaque) glass.tint?.let { tint ->
        drawRect(tint, blendMode = BlendMode.Hue)
        drawRect(tint.copy(alpha = tint.alpha * 0.75f))
    }
    // Pressed glass lightens a little, as on iOS (react-native-liquid's flash is twice as strong).
    if (press > 0f) drawRect(Color.White.copy(alpha = (if (dark) 0.12f else 0.25f) * press))
}

/**
 * react-native-liquid's `specularEdge` (iOS 26 light glass): a glow just inside the top edge, a
 * white line along the bottom and a graded hairline, at [amount] (0 to 1) of the lib's strength.
 */
private fun DrawScope.drawEdgeLight(cornerPx: Float, amount: Float) {
    if (amount <= 0f) return
    val radius = min(cornerPx, min(size.width, size.height) / 2f)
    val top = 0.95f * amount
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = top),
            0.16f to Color.White.copy(alpha = 0f),
            0.93f to Color.White.copy(alpha = 0f),
            1f to Color.White.copy(alpha = top),
        ),
        cornerRadius = CornerRadius(radius),
    )
    val hair = 0.5.dp.toPx()
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.Black.copy(alpha = 0.13f * amount),
            0.45f to Color.Black.copy(alpha = 0.3f * amount),
            0.78f to Color.Black.copy(alpha = 0.24f * amount),
            1f to Color.Transparent,
        ),
        topLeft = Offset(hair / 2f, hair / 2f),
        size = Size(size.width - hair, size.height - hair),
        cornerRadius = CornerRadius((radius - hair / 2f).coerceAtLeast(0f)),
        style = Stroke(hair),
    )
}

private fun frostColor(clear: Boolean, dark: Boolean): Color = when {
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> if (dark) Color(0xF21C1C1E) else Color(0xF2F2F2F7)
    // iOS regular glass is a frost, not a dim: it lightens what is behind it in dark mode too.
    dark -> if (clear) Color(0xFF1C1C1E).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.14f)
    else -> Color.White.copy(alpha = if (clear) 0.08f else 0.32f)
}

/**
 * react-native-liquid's interactive glass: swells while pressed (small glass the most) and
 * stretches along a drag without leaving its place, then wobbles back like a drop.
 */
@Stable
internal class ElasticPress(private val scope: CoroutineScope, var swells: Boolean = true) {
    val swell = Animatable(1f)
    /** Lights the glass and bulges the lens under the finger. */
    val press = Animatable(0f)
    private val stretchX = Animatable(1f)
    private val stretchY = Animatable(1f)
    var touch by mutableStateOf(Offset.Zero)

    val scaleX get() = swell.value * stretchX.value
    val scaleY get() = swell.value * stretchY.value
    val layerBlock: GraphicsLayerScope.() -> Unit = {
        scaleX = this@ElasticPress.scaleX
        scaleY = this@ElasticPress.scaleY
    }

    fun down(size: IntSize, density: Float) {
        scope.launch { press.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
        if (!swells) return
        // About 20 dp on the long side, as measured on iOS 26: a 44 dp circle to 1.4, a wide field barely.
        val grow = min(0.4f, 20f * density / max(size.width, size.height).coerceAtLeast(1))
        scope.launch { swell.animateTo(1f + grow, spring(0.82f, 170f)) }
    }

    fun drag(delta: Offset, size: IntSize, density: Float) {
        if (!swells) return
        val reach = min(12f * density, 0.15f * min(size.width, size.height).coerceAtLeast(1))
        fun amount(d: Float) = abs(tanh(d / (8f * reach)))
        val follow = spring<Float>(0.8f, 420f)
        // Like iOS, the glass stays put under a drag; it only stretches along it.
        // Longer along the drag, thinner across it (about the same volume).
        scope.launch { stretchX.animateTo(1f + 0.05f * amount(delta.x) - 0.02f * amount(delta.y), follow) }
        scope.launch { stretchY.animateTo(1f + 0.05f * amount(delta.y) - 0.02f * amount(delta.x), follow) }
    }

    fun up() {
        // Underdamped, so it dips below its size and wobbles back.
        val wobble = spring<Float>(0.45f, 220f)
        scope.launch { swell.animateTo(1f, spring(0.5f, 210f)) }
        scope.launch { press.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
        scope.launch { stretchX.animateTo(1f, wobble) }
        scope.launch { stretchY.animateTo(1f, wobble) }
    }
}

/** Observe only: never consumes, so clicks and scrolling still reach the content. */
private fun Modifier.elasticInput(elastic: ElasticPress): Modifier = pointerInput(elastic) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        elastic.touch = down.position
        elastic.down(size, density)
        while (true) {
            // The final pass sees what parents consumed: a scroll that took over a move ends the
            // press (a clickable consumes the down, which must not).
            val change = awaitPointerEvent(PointerEventPass.Final).changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed || (change.isConsumed && change.positionChanged())) break
            elastic.touch = change.position
            elastic.drag(change.position - down.position, size, density)
        }
        elastic.up()
    }
}

// ---- Containers ----

internal val LocalGlassContainer = staticCompositionLocalOf<GlassContainerState?> { null }

/** One glass shape of a container, with its laid-out rect and the per-edge morph offsets. */
@Stable
internal class GlassMember {
    var glass by mutableStateOf(Glass.Regular)
    var shape by mutableStateOf(CapsuleShape)
    var dark by mutableStateOf(false)
    var unionId: Any? by mutableStateOf(null)
    var elastic: ElasticPress? by mutableStateOf(null)
    var transition = GlassTransition.MatchedGeometry
    var coordinates: LayoutCoordinates? = null
    var placed = false
    var leaving = false

    /** Where layout put the glass, in container px. */
    var rect by mutableStateOf(Rect.Zero)
    val left = Animatable(0f, 0.5f)
    val top = Animatable(0f, 0.5f)
    val right = Animatable(0f, 0.5f)
    val bottom = Animatable(0f, 0.5f)

    /** Where the glass is drawn: the rect plus the morph still in flight. */
    val displayed: Rect
        get() = Rect(rect.left + left.value, rect.top + top.value, rect.right + right.value, rect.bottom + bottom.value)
}

@Stable
internal class GlassContainerState(private val scope: CoroutineScope) {
    val members = mutableStateListOf<GlassMember>()
    var coordinates: LayoutCoordinates? = null
    private var offsetInParent: Offset? = null

    /** Called whenever the container or a member is placed; whichever comes last fills the rects. */
    fun sync() {
        val container = coordinates?.takeIf { it.isAttached } ?: return
        // A container that grows or moves in its parent (a column widening to the left) shifts
        // its own origin; shift the shapes with it, so glass that stayed put on screen doesn't morph.
        // Glass still laid out where it was moved with the container (a column re-centering it, as
        // Union's does when its details show): it follows instead of lagging behind.
        val offset = container.positionInParent()
        offsetInParent?.let { last ->
            if (offset != last) members.forEach {
                val now = it.coordinates?.takeIf { c -> c.isAttached }?.let { c -> container.localBoundingBoxOf(c, clipBounds = false) }
                if (it.placed && now != it.rect) it.rect = it.rect.translate(last - offset)
            }
        }
        offsetInParent = offset
        // New glass grows out of glass already on screen, as on iOS, not out of glass appearing with it.
        val shown = members.filter { it.placed && !it.leaving }
        // The glass a container first lays out is simply there, as on iOS: nothing animates it in.
        // Shapes are placed one by one in a layout pass, so this holds until the next frame.
        val firstLayout = !laidOut
        for (member in members) {
            if (member.leaving) continue
            val coords = member.coordinates?.takeIf { it.isAttached } ?: continue
            val rect = container.localBoundingBoxOf(coords, clipBounds = false)
            if (member.placed && rect == member.rect) continue
            // Identity glass tracks its layout exactly, so a layout animation (expandVertically) drives it.
            if (member.placed && member.transition == GlassTransition.Identity) {
                member.rect = rect
                continue
            }
            val from = morphStart(member, rect, firstLayout) { nearest(member, rect, shown)?.displayed }
            member.rect = rect
            member.placed = true
            member.morph(from, rect)
        }
        if (!laidOut && !settling && members.any { it.placed }) {
            settling = true
            scope.launch { withFrameNanos { }; laidOut = true }
        }
    }

    // Set a frame after glass was first laid out in this container.
    private var laidOut = false
    private var settling = false

    /** Glass leaves into its nearest neighbour (matched geometry) or shrinks away in place. */
    fun leave(member: GlassMember) {
        // Already on its way out: `present` turning false leaves once from the old effect's
        // dispose and once from the new effect.
        if (member.leaving) return
        if (!member.placed || member.transition == GlassTransition.Identity) {
            members -= member
            return
        }
        member.leaving = true
        scope.launch {
            // Picked a frame later, once every shape leaving together is marked, so several leave
            // into what stays (the + button) instead of into each other.
            // The reverse of appearing: flow back into that neighbour's whole shape, as on iOS.
            // Into the neighbour's layout, not where it is drawn now: a neighbour sliding to a new
            // place (a row re-centring) takes the leaving glass with it instead of leaving it behind.
            val to = if (member.transition == GlassTransition.MatchedGeometry) nearest(member, member.rect)?.rect else null
            member.morph(member.displayed, to ?: member.rect.collapsed()).join()
            // Unless it came back meanwhile.
            if (member.leaving) members -= member
        }
    }

    /** Adds a new member, or brings back one that was leaving. */
    fun restore(member: GlassMember) {
        if (member !in members) members += member
        if (member.leaving) {
            member.leaving = false
            member.morph(member.displayed, member.rect)
        }
    }

    private fun nearest(member: GlassMember, rect: Rect, among: List<GlassMember> = members): GlassMember? = among
        .filter { it !== member && it.placed && !it.leaving }
        .minByOrNull { (it.rect.center - rect.center).getDistance() }

    /**
     * Springs each edge from [from] to [to] separately, slightly differently per edge so the glass
     * stretches like liquid (react-native-liquid's ~0.55 s morph).
     */
    // Undispatched, so the edges snap to [from] before this frame draws: new glass never shows a
    // frame at its final place, and glass appearing next to it grows from where it really is.
    private fun GlassMember.morph(from: Rect, to: Rect) = scope.launch(start = CoroutineStart.UNDISPATCHED) {
        // Each edge keeps its speed when a new layout restarts it, so a moving target is chased
        // smoothly instead of the glass stalling every frame.
        suspend fun edge(a: Animatable<Float, *>, from: Float, to: Float, spec: androidx.compose.animation.core.SpringSpec<Float>) {
            val velocity = a.velocity
            a.snapTo(from)
            a.animateTo(to, spec, initialVelocity = velocity)
        }
        listOf(
            // About 0.3 s, as measured on iOS 26 (react-native-liquid's springs, three times as stiff).
            launch(start = CoroutineStart.UNDISPATCHED) { edge(left, from.left - rect.left, to.left - rect.left, spring(0.72f, 400f, 0.5f)) },
            launch(start = CoroutineStart.UNDISPATCHED) { edge(right, from.right - rect.right, to.right - rect.right, spring(0.68f, 350f, 0.5f)) },
            launch(start = CoroutineStart.UNDISPATCHED) { edge(top, from.top - rect.top, to.top - rect.top, spring(0.76f, 480f, 0.5f)) },
            launch(start = CoroutineStart.UNDISPATCHED) { edge(bottom, from.bottom - rect.bottom, to.bottom - rect.bottom, spring(0.76f, 450f, 0.5f)) },
        ).joinAll()
    }

    /** The shader's shapes: members of one union as a single shape, pressed glass swollen. */
    fun shapes(offset: Float, density: Density): List<MergedGlassShape> = members
        .groupBy { it.unionId ?: it }
        .values
        .map { group ->
            val rect = group.map { it.pressedRect() }.reduce { a, b ->
                Rect(min(a.left, b.left), min(a.top, b.top), max(a.right, b.right), max(a.bottom, b.bottom))
            }.translate(offset, offset)
            val first = group.first()
            MergedGlassShape(
                left = rect.left,
                top = rect.top,
                width = rect.width,
                height = rect.height,
                radius = group.minOf { it.shape.topStart.toPx(rect.size, density) },
                tint = group.firstNotNullOfOrNull { it.glass.tint }?.toArgb(),
                opaque = first.glass.opaque,
                press = group.maxOf { it.elastic?.press?.value ?: 0f },
                dark = first.dark,
                clear = first.glass.clear,
            )
        }
        // shortcut: shapes past the shader's 12 are not drawn; raise MERGED_GLASS_MAX_SHAPES if a screen needs more.
        .take(MERGED_GLASS_MAX_SHAPES)
}

private fun Rect.collapsed() = Rect(center, center)

/**
 * Where glass newly laid out at [rect] starts its morph: where it is drawn if it was placed
 * before; in place on the container's first layout or with the identity transition; out of the
 * nearest glass on screen with the matched transition; otherwise grown from its centre.
 */
internal fun morphStart(member: GlassMember, rect: Rect, firstLayout: Boolean, nearest: () -> Rect?): Rect = when {
    member.placed -> member.displayed
    firstLayout || member.transition == GlassTransition.Identity -> rect
    member.transition == GlassTransition.MatchedGeometry -> nearest() ?: rect.collapsed()
    else -> rect.collapsed()
}

/**
 * The member's content rides on its glass while the glass morphs (it is laid out at its final
 * place at once), as SwiftUI moves a view with its matched glass: centred on the drawn glass and
 * shrunk with it, never grown. Only matched glass carries it: materializing glass appears and
 * goes in place, and its content stays (Toolbar's group glass turned off keeps its buttons).
 */
private fun GlassMember.followLayer(): GraphicsLayerScope.() -> Unit = {
    val shown = displayed
    if (transition == GlassTransition.MatchedGeometry && rect.width > 0f && rect.height > 0f && shown != rect) {
        translationX = shown.center.x - rect.center.x
        translationY = shown.center.y - rect.center.y
        val scale = min(shown.width / rect.width, shown.height / rect.height).coerceIn(0f, 1f)
        scaleX = scale
        scaleY = scale
    }
}

/** The member's rect with its press swell, stretch and pull, as its content's layer draws it. */
private fun GlassMember.pressedRect(): Rect {
    val rect = displayed
    val e = elastic ?: return rect
    val c = rect.center
    return Rect(Offset(c.x - rect.width * e.scaleX / 2f, c.y - rect.height * e.scaleY / 2f), Size(rect.width * e.scaleX, rect.height * e.scaleY))
}

/** Room around a container for swollen, morphing glass and its shadow. */
private val ContainerBleed = 32.dp

/**
 * Mirrors `GlassEffectContainer(spacing:)`: all member glass is drawn in one pass of
 * react-native-liquid's merged-glass shader, so shapes closer than [spacing] melt together with a
 * bridge, and members morph edge by edge as they move, appear and leave. Below API 33 members
 * draw as separate glass.
 */
@Composable
fun GlassEffectContainer(
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalGlassBackdrop.current
    if (backdrop == null || !lensSupported || isMaterial) {
        Box(modifier) { content() }
        return
    }
    val scope = rememberCoroutineScope()
    val state = remember(scope) { GlassContainerState(scope) }
    val dark = isSystemInDarkTheme()
    Box(modifier.onGloballyPositioned { state.coordinates = it; state.sync() }) {
        // An empty container draws nothing; otherwise its bleed would show the raw backdrop.
        if (state.members.isNotEmpty()) Box(
            Modifier
                .matchParentSize()
                .layout { measurable, constraints ->
                    val bleed = ContainerBleed.roundToPx()
                    val placeable = measurable.measure(Constraints.fixed(constraints.maxWidth + bleed * 2, constraints.maxHeight + bleed * 2))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(-bleed, -bleed) }
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RectangleShape },
                    effects = {
                        val first = state.members.firstOrNull()?.glass ?: Glass.Regular
                        mergedGlass(
                            state.shapes(ContainerBleed.toPx(), this),
                            MergedGlassLook(
                                blurPx = first.blurDp.toPx(),
                                bevelPx = first.refractionHeight.toPx() * 1.33f,
                                refractPx = first.refractionAmount.toPx() * 2.2f,
                                mergePx = spacing.toPx(),
                                dark = dark,
                            ),
                        )
                    },
                    highlight = null,
                    shadow = null,
                ),
        )
        CompositionLocalProvider(LocalGlassContainer provides state) { content() }
    }
}
