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
import androidx.compose.foundation.layout.statusBars
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

/** Pops the current screen. Provided by the nav host so screens need no NavController. */
val LocalOnBack = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Mirrors `scrollEdgeEffectStyle`: how content fades where it meets a bar. [StatusBar] fades only
 * the status bar strip, so the clock stays legible while the rest of the bar has no background.
 */
enum class EdgeEffect { Soft, Hard, Hidden, StatusBar }

/** iOS `systemBackground`: a plain `ScrollView` page. */
val SystemBackground: Color @Composable get() = if (isSystemInDarkTheme()) Color.Black else Color.White

/** iOS `.background.secondary`: cards on a [SystemBackground] page. */
val SecondarySystemBackground: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)

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
    Text(text, modifier.padding(bottom = 4.dp), fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold)

/** The bar title of a large-title page: it appears once the large title has scrolled away. */
@Composable
fun CollapsedBarTitle(text: String, scrolled: Boolean) =
    AnimatedVisibility(scrolled, enter = fadeIn(), exit = fadeOut()) { BarTitle(text) }

@Composable
fun BarTitle(text: String) {
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
    Box(modifier.fillMaxWidth()) {
        if (edge == EdgeEffect.StatusBar) {
            ScrollEdge(EdgeEffect.Soft, top = true, Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
        } else {
            // withAnimation on the style: the old edge crossfades into the new one.
            Crossfade(edge, Modifier.matchParentSize(), label = "top edge") { ScrollEdge(it, top = true, Modifier.fillMaxSize()) }
        }
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
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

/** The fade where scrolling content meets a bar: progressive blur, an opaque band, or nothing. */
@Composable
fun ScrollEdge(edge: EdgeEffect, top: Boolean, modifier: Modifier = Modifier) {
    val backdrop = LocalGlassBackdrop.current ?: return
    val background = MaterialTheme.colorScheme.background
    when (edge) {
        EdgeEffect.Hidden, EdgeEffect.StatusBar -> Unit
        EdgeEffect.Hard -> Box(modifier.background(background.copy(alpha = 0.94f)))
        EdgeEffect.Soft -> {
            // As iOS's edge material: solid over the first 40% next to the bar, then fading out.
            fun stops(near: Color, far: Color) =
                if (top) arrayOf(0f to near, 0.4f to near, 1f to far) else arrayOf(0f to far, 0.6f to near, 1f to near)
            // Blurred content masked to fade out away from the bar: a progressive blur.
            val mask = Brush.verticalGradient(*stops(Color.Black, Color.Transparent))
            val fade = Brush.verticalGradient(*stops(background.copy(alpha = 0.7f), background.copy(alpha = 0f)))
            Box(
                modifier
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RectangleShape },
                        effects = { blur(18.dp.toPx()) },
                        highlight = null,
                        shadow = null,
                        onDrawBackdrop = { drawBackdrop ->
                            drawBackdrop()
                            drawRect(mask, blendMode = BlendMode.DstIn)
                        },
                    )
                    .background(fade),
            )
        }
    }
}
