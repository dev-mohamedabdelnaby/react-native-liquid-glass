package com.mohamedabdelnaby.liquidglass.catalog.shapes

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.moreContrasting

/** JS `androidControlShape`: how switches, sliders, top tabs and bottom tabs look in Material (glass keeps its liquid controls). */
enum class ControlShape { Pill, Labeled, Glow }

/** `'pill' | 'labeled' | 'glow'`, as JS sends it; anything else (old `'default'` / `'squared'` too) is Pill. */
fun controlShapeOf(value: String): ControlShape = ControlShape.entries.firstOrNull { it.name.lowercase() == value } ?: ControlShape.Pill

/** JS `androidGlowIntensity`: 0 (no glow) to 1 (the strongest); NaN is the default. */
fun glowIntensityOf(value: Float): Float = if (value.isNaN()) DEFAULT_GLOW_INTENSITY else value.coerceIn(0f, 1f)

const val DEFAULT_GLOW_INTENSITY = 0.35f

/** The shape every switch, slider and tab row follows. Provided by the host. */
val LocalControlShape = staticCompositionLocalOf { ControlShape.Pill }

/** How strongly Glow glows. Provided by the host next to [LocalControlShape]; not static, so a Home slider tick recomposes only the glows. */
val LocalGlowIntensity = compositionLocalOf { DEFAULT_GLOW_INTENSITY }

/** A control's own colour, else the theme's primary (Home's tint, contrast-adjusted by the Material theme). */
@Composable
internal fun shapeAccent(own: Color? = null): Color = own ?: MaterialTheme.colorScheme.primary

/** Text and marks on [accent]: white or black, whichever reads better. */
internal fun onAccent(accent: Color): Color = moreContrasting(accent, Color.White, Color.Black)

/** Every shape is a capsule: symmetric, so RTL can't leave a stale border (Task 15). */
internal val Capsule: Shape = RoundedCornerShape(percent = 50)

/** The slide every shape moves with (no liquid drop). */
internal fun <T> shapeSpring(): SpringSpec<T> = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)

/** The halo strength for Glow ([on] while it fades in), scaled by Home's intensity; 0 for the other shapes. */
@Composable
internal fun glowStrength(on: Float = 1f): Float =
    if (LocalControlShape.current == ControlShape.Glow) on * LocalGlowIntensity.current else 0f

/** Glow: a neon halo of [color] around [shape]; [strength] 1 is the strongest, 0 hides it. */
internal fun Modifier.glow(color: Color, shape: Shape, strength: Float = 1f): Modifier =
    if (strength <= 0f) this else dropShadow(shape, Shadow(radius = 14.dp, color = color, spread = 1.dp, alpha = 0.8f * strength))
