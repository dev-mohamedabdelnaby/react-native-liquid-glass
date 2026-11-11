package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

/** The shapes a [LiquidGlassView] can take. */
enum class LiquidGlassShape(val title: String) { Capsule("Capsule"), Rounded("Rounded"), Circle("Circle"), Rect("Rect") }

fun LiquidGlassShape.toShape(cornerRadius: Dp): RoundedCornerShape = when (this) {
    LiquidGlassShape.Capsule -> CapsuleShape
    LiquidGlassShape.Rounded -> RoundedCornerShape(cornerRadius)
    LiquidGlassShape.Circle -> GlassCircle
    LiquidGlassShape.Rect -> RectShape
}

/**
 * A reusable container that places any content on a glass surface, with react-native-liquid's
 * `LiquidGlassView` lens props: [blurRadius] (null: the variant's), [refractionHeight] (the rim
 * bevel), [refractionAmount] (how far the rim bends the page) and [chromaticAberration].
 */
@Composable
fun LiquidGlassView(
    modifier: Modifier = Modifier,
    // null follows Home's general Glass settings; a value overrides them for this view alone.
    isClear: Boolean? = null,
    shape: LiquidGlassShape = LiquidGlassShape.Rounded,
    cornerRadius: Dp = 24.dp,
    tint: Color? = null,
    interactive: Boolean = false,
    padding: Dp = 20.dp,
    blurRadius: Dp? = null,
    refractionHeight: Dp = 12.dp,
    refractionAmount: Dp = 24.dp,
    chromaticAberration: Boolean = false,
    white: Float? = null,
    black: Float? = null,
    edgeLight: Float? = null,
    content: @Composable () -> Unit,
) {
    val glass = (if (isClear == true) Glass.Clear else Glass.Regular).tint(tint).interactive(interactive).copy(
        blur = blurRadius, refractionHeight = refractionHeight, refractionAmount = refractionAmount,
        chromaticAberration = chromaticAberration, white = white, black = black, edgeLight = edgeLight,
        ownVariant = isClear != null,
    )
    Box(
        modifier.glassEffect(glass, shape.toShape(cornerRadius)).padding(padding),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun LiquidGlassViewSamples() {
    GlassSurfaceHost {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LiquidGlassView { Text("Regular glass view") }
            LiquidGlassView(shape = LiquidGlassShape.Capsule, tint = SystemColors.Orange, interactive = true) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.TouchApp, null)
                    Text("Tinted & interactive")
                }
            }
            LiquidGlassView(shape = LiquidGlassShape.Circle) { Icon(Icons.Filled.Star, null) }
            LiquidGlassView(isClear = true) { Text("Clear glass view") }
        }
    }
}

@Preview(name = "Light")
@Composable
private fun LiquidGlassViewLightPreview() = LiquidGlassTheme(AppearanceMode.Light) { LiquidGlassViewSamples() }

@Preview(name = "Dark")
@Composable
private fun LiquidGlassViewDarkPreview() = LiquidGlassTheme(AppearanceMode.Dark) { LiquidGlassViewSamples() }
