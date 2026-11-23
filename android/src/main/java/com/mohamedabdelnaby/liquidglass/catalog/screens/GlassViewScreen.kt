package com.mohamedabdelnaby.liquidglass.catalog.screens

import android.annotation.SuppressLint
import android.graphics.Outline
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import com.mohamedabdelnaby.liquidglass.catalog.glass.MaterialSwitch
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mohamedabdelnaby.liquidglass.R
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberLayerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.DemoColumn
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSlider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSwitch
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSegmented
import com.mohamedabdelnaby.liquidglass.catalog.glass.LiquidGlassShape
import com.mohamedabdelnaby.liquidglass.catalog.glass.LiquidGlassView
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors
import androidx.compose.ui.platform.LocalFontFamilyResolver
import android.graphics.Typeface

private val TintChoices = listOf(
    SystemColors.Blue, SystemColors.Orange, SystemColors.Green, SystemColors.Pink, SystemColors.Purple, SystemColors.Teal,
)

@Composable
fun GlassViewScreen() {
    // This view alone: its controls start from Home's general settings and then override them.
    val general = LocalGlassSettings.current
    var isClear by remember { mutableStateOf(general.clear) }
    var white by remember { mutableFloatStateOf(general.white) }
    var black by remember { mutableFloatStateOf(general.black) }
    var edge by remember { mutableFloatStateOf(general.edgeLight) }
    var shape by remember { mutableStateOf(LiquidGlassShape.Rounded) }
    var cornerRadius by remember { mutableFloatStateOf(24f) }
    var tinted by remember { mutableStateOf(false) }
    var tint by remember { mutableStateOf(SystemColors.Blue) }
    var interactive by remember { mutableStateOf(true) }
    var blur by remember { mutableFloatStateOf(general.blur.value) }
    var refractionHeight by remember { mutableFloatStateOf(general.refractionHeight.value) }
    var refractionAmount by remember { mutableFloatStateOf(general.refractionAmount.value) }
    var chromatic by remember { mutableStateOf(false) }
    var contentPadding by remember { mutableFloatStateOf(24f) }
    // Every shape is a rounded rect here (Compose clamps corners to half the short side), so
    // switching shapes is one animated radius.
    val radius by animateDpAsState(
        when (shape) {
            LiquidGlassShape.Rounded -> cornerRadius.dp
            LiquidGlassShape.Rect -> 0.dp
            LiquidGlassShape.Capsule, LiquidGlassShape.Circle -> 150.dp
        },
        // .smooth: no bounce, about 0.5 s.
        spring(dampingRatio = 1f, stiffness = 158f),
        label = "radius",
    )

    GlassScreen("Glass View", backdrop = { GlassBackdrop(it, R.drawable.backdrop_jellyfish) }) { padding ->
        DemoColumn(padding) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                LiquidGlassView(
                    modifier = if (shape == LiquidGlassShape.Circle) Modifier else Modifier.fillMaxWidth(),
                    isClear = isClear,
                    shape = LiquidGlassShape.Rounded,
                    cornerRadius = radius,
                    tint = if (tinted) tint.copy(alpha = 0.6f) else null,
                    interactive = interactive,
                    padding = if (shape == LiquidGlassShape.Circle) 36.dp else contentPadding.dp,
                    blurRadius = blur.dp,
                    white = white,
                    black = black,
                    edgeLight = edge,
                    refractionHeight = refractionHeight.dp,
                    refractionAmount = refractionAmount.dp,
                    chromaticAberration = chromatic,
                ) {
                    if (shape == LiquidGlassShape.Circle) {
                        Icon(Icons.Filled.WaterDrop, "Liquid Glass View", Modifier.size(48.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.WaterDrop, null)
                                Text("Liquid Glass View", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "Any content can sit on a glass surface. Adjust the options below.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            val panel = rememberLayerBackdrop()
            Column(
                Modifier.fillMaxWidth().glassEffect(Glass.Regular, RoundedCornerShape(28.dp), exportTo = panel).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // The tabs' drop refracts the panel under it, as the top tabs' drop does on a plain
                // page, not the photo behind the panel.
                CompositionLocalProvider(LocalGlassBackdrop provides panel) {
                    GlassSegmented(listOf("Regular", "Clear"), if (isClear) 1 else 0, { isClear = it == 1 })
                    GlassSegmented(LiquidGlassShape.entries.map { it.title }, shape.ordinal, { shape = LiquidGlassShape.entries[it] })
                }
                AnimatedVisibility(shape == LiquidGlassShape.Rounded) {
                    Column {
                        Text("Corner radius: ${cornerRadius.toInt()}")
                        GlassSlider(cornerRadius, { cornerRadius = it }, valueRange = 0f..60f, contentDescription = "Corner radius")
                    }
                }
                LensSlider("Padding", contentPadding, 8f..56f) { contentPadding = it }
                LensSlider("Blur", blur, 0f..40f) { blur = it }
                PercentSlider("White", white) { white = it }
                PercentSlider("Black", black) { black = it }
                PercentSlider("Edge light", edge) { edge = it }
                LensSlider("Refraction height", refractionHeight, 0f..40f) { refractionHeight = it }
                LensSlider("Refraction amount", refractionAmount, 0f..60f) { refractionAmount = it }
                SwitchRow("Chromatic aberration", chromatic) { chromatic = it }
                SwitchRow("Interactive", interactive) { interactive = it }
                SwitchRow("Tint", tinted) { tinted = it }
                AnimatedVisibility(tinted) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Tint color")
                        Fill()
                        TintChoices.forEach { color ->
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .background(color, CircleShape)
                                    .then(if (color == tint) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                    .selectable(color == tint, role = Role.RadioButton) { tint = color },
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Platform view · RenderEffect", style = MaterialTheme.typography.titleMedium)
                PlatformGlassView(tinted, tint, interactive, Modifier.fillMaxWidth().height(100.dp))
            }
        }
    }
}

@Composable
private fun LensSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text("$label: ${value.toInt()} dp")
        GlassSlider(value, onChange, valueRange = range, contentDescription = label)
    }
}

@Composable
private fun PercentSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Column {
        Text("$label: ${(value * 100).toInt()}%")
        GlassSlider(value, onChange, contentDescription = label)
    }
}

@Composable
internal fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    // Material: the whole row toggles, so TalkBack reads the label with the switch's state.
    if (isMaterial) return Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Fill()
        MaterialSwitch(checked, null, Modifier)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Fill()
        GlassSwitch(checked, onChange)
    }
}

/**
 * The classic View-system counterpart (the iOS screen compares with UIKit's UIGlassEffect): a
 * FrameLayout whose colorful layer is blurred with RenderEffect on API 31+, under a tint and a label.
 */
// The press scale is decoration; performClick keeps the view's click semantics.
@SuppressLint("ClickableViewAccessibility")
@Composable
private fun PlatformGlassView(tinted: Boolean, tint: Color, interactive: Boolean, modifier: Modifier) {
    val label = MaterialTheme.colorScheme.onSurface.toArgb()
    // The custom font's bold face for the View label (in place of the system font's fake bold).
    val fontFamily = LocalGlassSettings.current.fontFamily
    val resolver = LocalFontFamilyResolver.current
    val typeface = fontFamily?.let { resolver.resolve(it, FontWeight.Bold).value as? Typeface }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val density = context.resources.displayMetrics.density
            val radius = 28 * density
            FrameLayout(context).apply {
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) = outline.setRoundRect(0, 0, view.width, view.height, radius)
                }
                clipToOutline = true
                addView(View(context).apply {
                    background = GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        intArrayOf(SystemColors.Cyan.toArgb(), SystemColors.Pink.toArgb(), SystemColors.Orange.toArgb()),
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setRenderEffect(RenderEffect.createBlurEffect(40 * density, 40 * density, Shader.TileMode.CLAMP))
                    }
                })
                addView(View(context).apply { tag = "tint" })
                addView(TextView(context).apply {
                    tag = "label"
                    text = context.getString(R.string.platform_glass_label)
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    paint.isFakeBoldText = true
                })
            }
        },
        update = { view ->
            view.findViewWithTag<View>("tint").setBackgroundColor(
                (if (tinted) tint.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.35f)).toArgb(),
            )
            view.findViewWithTag<TextView>("label").apply {
                setTextColor(label)
                if (typeface != null) {
                    paint.isFakeBoldText = false
                    this.typeface = typeface
                } else if (!paint.isFakeBoldText) { // the font was unset at runtime: back to the default face
                    paint.isFakeBoldText = true
                    this.typeface = Typeface.DEFAULT
                }
            }
            view.setOnTouchListener(
                if (interactive) View.OnTouchListener { v, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> v.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start()
                        MotionEvent.ACTION_UP -> { v.animate().scaleX(1f).scaleY(1f).setDuration(200).start(); v.performClick() }
                        MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    }
                    true
                } else null,
            )
        },
    )
}

@Preview(name = "Light") @Composable private fun GlassViewLight() = PreviewLight { GlassViewScreen() }
@Preview(name = "Dark") @Composable private fun GlassViewDark() = PreviewDark { GlassViewScreen() }
