package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.RectangleShape
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.Backdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.LayerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.layerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberCombinedBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberLayerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.drawBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.blur
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.vibrancy

/** Pops the current screen. Provided by the nav host so screens need no NavController. */
val LocalOnBack = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Mirrors `scrollEdgeEffectStyle`: how content fades where it meets a bar. [StatusBar] fades only
 * the status bar strip, so the clock stays legible while the rest of the bar has no background.
 */
enum class EdgeEffect { Soft, Hard, Hidden, StatusBar }

/** iOS `systemBackground`: a plain `ScrollView` page. */
val SystemBackground: Color @Composable get() = if (isMaterial) MaterialTheme.colorScheme.surface else if (isSystemInDarkTheme()) Color.Black else Color.White

/** iOS `.background.secondary`: cards on a [SystemBackground] page. */
val SecondarySystemBackground: Color @Composable get() =
    if (isMaterial) MaterialTheme.colorScheme.surfaceContainerHigh else if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)

/** A flat background for screens that bring their own colorful content; grouped-list gray by default. */
@Composable
fun PlainBackdrop(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.background) =
    Box(modifier.fillMaxSize().background(color))

/** The whole screen as glass sees it: the backdrop with the content over it. */
private val LocalGlassScreenBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/** The host's backdrop layer, for [GlassContent] glass that sits in the content it records. */
private val LocalGlassBackdropLayer = staticCompositionLocalOf<LayerBackdrop?> { null }

private val LocalGlassContentLayer = staticCompositionLocalOf<LayerBackdrop?> { null }

/** The screen on show, for windows a screen opens outside its host (alerts, sheets). Provided by the app. */
@Stable
class GlassWindowBackdrop {
    var backdrop: Backdrop? by mutableStateOf(null)
}

val LocalGlassWindowBackdrop = staticCompositionLocalOf<GlassWindowBackdrop?> { null }

/**
 * Records two layers: the backdrop and the content ([GlassContent]). Glass inside the content
 * refracts the backdrop; glass drawn above the content (bars, overlays, menus) refracts both.
 */
@Composable
fun GlassSurfaceHost(
    modifier: Modifier = Modifier,
    backdrop: @Composable (Modifier) -> Unit = { GlassBackdrop(it) },
    content: @Composable BoxScope.() -> Unit,
) {
    val back = rememberLayerBackdrop()
    val front = rememberLayerBackdrop()
    val screen = rememberCombinedBackdrop(back, front)
    val window = LocalGlassWindowBackdrop.current
    DisposableEffect(window, screen) {
        window?.backdrop = screen
        // The screen navigated to registers before the one it replaces leaves.
        onDispose { if (window?.backdrop === screen) window.backdrop = null }
    }
    // Transparent containers resolve their content color to LocalContentColor, so set it here.
    CompositionLocalProvider(
        LocalGlassBackdrop provides screen,
        LocalGlassScreenBackdrop provides screen,
        LocalGlassBackdropLayer provides back,
        LocalGlassContentLayer provides front,
        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
    ) {
        Box(modifier.fillMaxSize()) {
            backdrop(Modifier.layerBackdrop(back))
            content()
        }
    }
}

/** A screen's scrolling content: recorded for the glass above it; its own glass refracts the backdrop. */
@Composable
fun GlassContent(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val front = LocalGlassContentLayer.current
    Box(modifier.then(front?.let { Modifier.layerBackdrop(it) } ?: Modifier)) {
        CompositionLocalProvider(LocalGlassBackdrop provides (LocalGlassBackdropLayer.current ?: LocalGlassBackdrop.current)) { content() }
    }
}

/**
 * Popups, dialogs and sheets are separate windows: their glass refracts the whole screen and never
 * joins a [GlassEffectContainer] of the screen. Wrap the call that opens the window.
 */
@Composable
fun GlassOverlay(content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalGlassBackdrop provides (LocalGlassScreenBackdrop.current ?: LocalGlassWindowBackdrop.current?.backdrop ?: LocalGlassBackdrop.current),
    LocalGlassContainer provides null,
    content = content,
)

/** A catalog screen: backdrop, content and glass bars, like a SwiftUI `NavigationStack` page. */
@Composable
fun GlassScreen(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    titleContent: @Composable () -> Unit = { BarTitle(title) },
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    topEdge: EdgeEffect = EdgeEffect.Soft,
    topAccessory: (@Composable () -> Unit)? = null,
    // A full-screen cover's ✕ in place of the back chevron; it calls LocalOnBack too.
    closeButton: Boolean = false,
    // false hides the top bar, as `.searchable` does while its search is active.
    showTopBar: Boolean = true,
    backdrop: @Composable (Modifier) -> Unit = { GlassBackdrop(it) },
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    if (isMaterial) return MaterialScreen(
        modifier, actions, titleContent, bottomBar, floatingActionButton, topEdge, topAccessory, closeButton, showTopBar, backdrop, overlay, content,
    )
    GlassSurfaceHost(modifier, backdrop) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            topBar = {
                AnimatedVisibility(
                    showTopBar,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) { GlassTopBar(edge = topEdge, actions = actions, titleContent = titleContent, accessory = topAccessory, closeButton = closeButton) }
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
        ) { padding ->
            GlassContent(Modifier.fillMaxSize()) { content(padding) }
        }
        // Above the bars, still inside the host, so overlay glass refracts the whole screen.
        overlay()
    }
}

/** A navigation stack's large title: `.largeTitle.bold()`, leading, as the first item under the bar. */
@Composable
fun LargeTitle(text: String, modifier: Modifier = Modifier) =
    if (isMaterial) MaterialLargeTitle(text, modifier) else Text(text, modifier.padding(bottom = 4.dp), fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold)

/** The bar title of a large-title page: it appears once the large title has scrolled away. */
@Composable
fun CollapsedBarTitle(text: String, scrolled: Boolean) =
    // Material: a top app bar always shows its title (the screen drops its large title).
    if (isMaterial) MaterialBarTitle(text) else AnimatedVisibility(scrolled, enter = fadeIn(), exit = fadeOut()) { BarTitle(text) }

@Composable
fun BarTitle(text: String) {
    if (isMaterial) return MaterialBarTitle(text)
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * The iOS 26 back button: regular glass whatever the general setting, milky white with a soft blur
 * and a visible hairline, over the strong rim the lens gives a 44 dp circle.
 */
private val BackButtonGlass = Glass.Regular.copy(ownVariant = true, blur = 4.dp, white = 0.3f, edgeLight = 0.1f)

/** An iOS 26 style bar: no background, a glass back button, glass actions, a scroll-edge fade. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlassTopBar(
    modifier: Modifier = Modifier,
    edge: EdgeEffect = EdgeEffect.Soft,
    showBack: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    titleContent: @Composable () -> Unit = {},
    accessory: (@Composable () -> Unit)? = null,
    closeButton: Boolean = false,
) {
    // The status bar's height even while another window hides it (the share sheet opening and
    // closing), so the bar never jumps up under it.
    Box(modifier.fillMaxWidth()) {
        if (edge == EdgeEffect.StatusBar) {
            StatusBarEdge(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBarsIgnoringVisibility))
        } else {
            // withAnimation on the style: the old edge crossfades into the new one.
            Crossfade(edge, Modifier.matchParentSize(), label = "top edge") { ScrollEdge(it, top = true, Modifier.fillMaxSize()) }
        }
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
                .padding(vertical = 4.dp),
        ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
        ) {
            if (showBack) {
                GlassIconButton(
                    icon = if (closeButton) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = if (closeButton) "Close" else "Back",
                    onClick = LocalOnBack.current,
                    modifier = Modifier.align(Alignment.CenterStart),
                    glass = BackButtonGlass,
                )
            }
            Box(Modifier.align(Alignment.Center).padding(horizontal = 64.dp)) { titleContent() }
            Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically, content = actions)
        }
        accessory?.invoke()
        }
    }
}

/** A bottom bar slot: glass items over a scroll-edge fade, above the navigation bar. */
@Composable
fun GlassBottomBar(
    modifier: Modifier = Modifier,
    edge: EdgeEffect = EdgeEffect.Soft,
    content: @Composable RowScope.() -> Unit,
) {
    if (isMaterial) return MaterialBottomBar(modifier, content)
    Box(modifier.fillMaxWidth()) {
        Crossfade(edge, Modifier.matchParentSize(), label = "bottom edge") { ScrollEdge(it, top = false, Modifier.fillMaxSize()) }
        Row(
            Modifier
                .padding(top = 4.dp)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * iOS's `StatusBarEdge`: `.ultraThinMaterial` (a strong blur under a thin wash) over the status
 * bar, solid to 40% of its height and fading out at its bottom edge.
 */
@Composable
private fun StatusBarEdge(modifier: Modifier) =
    EdgeMaterial(modifier, Brush.verticalGradient(0f to Color.Black, 0.4f to Color.Black, 1f to Color.Transparent))

/** `.ultraThinMaterial` masked by [mask]: the blurred content under a thin wash of the page colour. */
@Composable
private fun EdgeMaterial(modifier: Modifier, mask: Brush) {
    val backdrop = LocalGlassBackdrop.current ?: return
    val wash = MaterialTheme.colorScheme.background.copy(alpha = 0.25f)
    Box(
        modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { RectangleShape },
            effects = {
                // Blur first: a blur after another effect pads the layer, and the padding above the
                // screen is empty, which left the strip's top see-through.
                blur(20.dp.toPx())
                vibrancy()
            },
            highlight = null,
            shadow = null,
            onDrawSurface = { drawRect(wash) },
            // Masked in the edge's own size, after the blur: the backdrop layer is the edge plus
            // the blur's padding, where the mask would still be opaque at the edge's bottom.
            onDrawFront = { drawRect(mask, blendMode = BlendMode.DstIn) },
        ),
    )
}

/** The fade where scrolling content meets a bar: progressive blur, an opaque band, or nothing. */
@Composable
fun ScrollEdge(edge: EdgeEffect, top: Boolean, modifier: Modifier = Modifier) {
    // Material bars are opaque: no fade.
    if (isMaterial) return
    LocalGlassBackdrop.current ?: return
    val background = MaterialTheme.colorScheme.background
    when (edge) {
        EdgeEffect.Hidden, EdgeEffect.StatusBar -> Unit
        EdgeEffect.Hard -> Box(modifier.background(background.copy(alpha = 0.94f)))
        // As iOS's soft edge: the status bar strip's material over the whole bar (status bar,
        // buttons and accessory), fading out softly only toward its far edge; a blur, not a
        // page-colour gradient.
        EdgeEffect.Soft -> EdgeMaterial(
            modifier,
            Brush.verticalGradient(
                *if (top) arrayOf(0f to Color.Black, 0.7f to Color.Black, 1f to Color.Transparent)
                else arrayOf(0f to Color.Transparent, 0.3f to Color.Black, 1f to Color.Black),
            ),
        )
    }
}
