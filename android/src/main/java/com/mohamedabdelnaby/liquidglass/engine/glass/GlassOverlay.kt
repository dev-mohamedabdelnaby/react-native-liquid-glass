package com.mohamedabdelnaby.liquidglass.engine.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.Backdrop

// Split out of the app's GlassScreen.kt (screen scaffolding, not copied): only what GlassOverlay needs.

/** The whole screen as glass sees it: the backdrop with the content over it. Provided by the host. */
val LocalGlassScreenBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/** The screen on show, for windows a screen opens outside its host (alerts, sheets). Provided by the host. */
@Stable
class GlassWindowBackdrop {
    var backdrop: Backdrop? by mutableStateOf(null)
}

val LocalGlassWindowBackdrop = staticCompositionLocalOf<GlassWindowBackdrop?> { null }

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
