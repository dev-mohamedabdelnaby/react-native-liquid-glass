// Ported from react-native-liquid's LiquidSearchView: SearchMorph (the "button" variant) and
// applyProgressToChildren, with SearchBarMorph's centered placeholder (the React Native view host
// dropped, the Compose body kept).
package com.mohamedabdelnaby.liquidglass.engine.liquid

import com.mohamedabdelnaby.liquidglass.engine.glass.opaqueFill
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassSettings
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.backdrops.emptyBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.drawBackdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.effects.blur
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.effects.lens
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.effects.vibrancy
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.highlight.Highlight
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private const val GAP_DP = 16f
private val Capsule = RoundedCornerShape(percent = 50)

/**
 * A round glass search button at the trailing edge that morphs into a search field, with a
 * separate close button growing out of the field's trailing edge. [content] is laid out beside
 * the button while collapsed and fades out, shrinking to 0.92, as the field expands.
 */
@Composable
fun LiquidExpandableSearch(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp,
    // Where the collapsed button sits: 1 at the trailing edge, 0.5 centered between the content
    // (iOS's search item between two flexible toolbar spacers).
    collapsedBias: Float = 1f,
    content: @Composable RowScope.() -> Unit = {},
) {
    val dark = isSystemInDarkTheme()
    val labelColor = if (dark) Color.White else Color.Black
    val secondaryColor = if (dark) Color(0x99EBEBF5) else Color(0x993C3C43)
    // LiquidMenuView's glassSurfaceColor for regular glass.
    val surfaceColor = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> if (dark) Color(0xF21C1C1E) else Color(0xF2F2F2F7)
        dark -> Color(0xFF1C1C1E).copy(alpha = 0.45f)
        else -> Color.White.copy(alpha = 0.45f)
    }
    val backdrop = LocalGlassBackdrop.current ?: emptyBackdrop()
    val glassTint = LocalGlassSettings.current.tint
    val opaque = LocalGlassSettings.current.opaque
    val density = LocalDensity.current
    val gap = with(density) { GAP_DP.dp.toPx() }
    val iconSize = with(density) { 20.dp.toPx() }
    val textStart = with(density) { 44.dp.toPx() }
    val textEnd = with(density) { 14.dp.toPx() }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val isExpanded by rememberUpdatedState(expanded)
    val onChange by rememberUpdatedState(onExpandedChange)
    val textStyle = TextStyle(color = labelColor, fontSize = 17.sp)
    val textMeasurer = rememberTextMeasurer()
    val placeholderWidth = remember(placeholder, textMeasurer) {
        textMeasurer.measure(placeholder, textStyle).size.width.toFloat()
    }

    // The field leads; the close button follows with a softer spring so the glass stretches.
    val field = remember { Animatable(if (expanded) 1f else 0f) }
    val close = remember { Animatable(if (expanded) 1f else 0f) }
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        val target = if (expanded) 1f else 0f
        launch { close.animateTo(target, spring(0.7f, 260f)) }
        launch { field.animateTo(target, spring(0.78f, 320f)) }
        if (expanded) {
            // The field is only enabled once expanded: let that recomposition land first.
            withFrameNanos { }
            focusRequester.requestFocus()
            keyboard?.show()
        } else {
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    val glass = { press: () -> Float ->
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule },
            effects = {
                vibrancy()
                blur(8f.dp.toPx())
                lens(12f.dp.toPx(), 18f.dp.toPx())
            },
            highlight = { Highlight.Default },
            layerBlock = {
                val scale = 1f + 0.06f * press()
                scaleX = scale
                scaleY = scale
            },
            onDrawSurface = {
                drawRect(surfaceColor)
                glassTint?.let { drawRect(it) }
                if (opaque) drawRect(opaqueFill(glassTint, dark))
            },
        )
    }

    Box(modifier.height(height)) {
        // The content beside the button (applyProgressToChildren).
        Row(
            Modifier
                .fillMaxSize()
                .padding(end = if (collapsedBias == 1f) height + GAP_DP.dp else 0.dp)
                .graphicsLayer {
                    val progress = field.value.coerceIn(0f, 1f)
                    alpha = 1f - progress
                    scaleX = 1f - 0.08f * progress
                    scaleY = scaleX
                }
                // Hidden under the field while searching.
                .then(if (expanded) Modifier.clearAndSetSemantics { } else Modifier),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )

        // Search field (round button while collapsed).
        Box(
            Modifier
                .layout { measurable, constraints ->
                    val width = constraints.maxWidth.toFloat()
                    val h = constraints.maxHeight.toFloat()
                    val p = field.value
                    val fieldWidth = lerp(h, width - h - gap, p).coerceAtLeast(h * 0.5f)
                    val left = lerp((width - h) * collapsedBias, 0f, p)
                    val placeable = measurable.measure(Constraints.fixed(fieldWidth.roundToInt(), h.roundToInt()))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.placeRelative(left.roundToInt(), 0) }
                }
                .then(glass { if (pressed) 1f else 0f })
                .then(
                    if (expanded) {
                        Modifier
                    } else {
                        Modifier.clearAndSetSemantics {
                            contentDescription = "Search"
                            role = Role.Button
                            onClick { onChange(true); true }
                        }
                    },
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (!isExpanded) {
                                pressed = true
                                tryAwaitRelease()
                                pressed = false
                            }
                        },
                        onTap = { if (!isExpanded) onChange(true) },
                    )
                },
        ) {
            // The icon stays centered in the growing button; as the text fades in, icon and
            // placeholder move from the center of the field to its leading edge.
            val iconX = { fieldWidth: Float, fieldHeight: Float ->
                val leading = fieldHeight * 0.32f
                val reveal = ((field.value - 0.5f) / 0.5f).coerceIn(0f, 1f)
                val iconCentered = (fieldWidth - iconSize) / 2f
                val groupCentered = if (query.isEmpty()) {
                    max((fieldWidth - (textStart - leading) - placeholderWidth) / 2f, leading)
                } else {
                    leading
                }
                lerp(lerp(iconCentered, groupCentered, reveal), leading, reveal)
            }
            SearchIcon(
                color = if (field.value > 0.5f) secondaryColor else labelColor,
                modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(Constraints())
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.placeRelative(
                            iconX(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat()).roundToInt(),
                            ((constraints.maxHeight - placeable.height) / 2f).roundToInt(),
                        )
                    }
                },
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                // While collapsed the field must not take the tap that expands the search.
                enabled = expanded,
                textStyle = textStyle,
                cursorBrush = SolidColor(if (dark) Color(0xFF0A84FF) else Color(0xFF007AFF)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val width = constraints.maxWidth.toFloat()
                        val h = constraints.maxHeight.toFloat()
                        // At rest 44 dp from the leading edge, as in SearchMorph.
                        val x = iconX(width, h) + textStart - h * 0.32f
                        val placeable = measurable.measure(
                            Constraints.fixedWidth((width - x - textEnd).coerceAtLeast(0f).roundToInt()),
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.placeRelative(x.roundToInt(), ((h - placeable.height) / 2f).roundToInt())
                        }
                    }
                    .focusRequester(focusRequester)
                    .semantics { contentDescription = placeholder }
                    .graphicsLayer { alpha = ((field.value - 0.5f) / 0.5f).coerceIn(0f, 1f) },
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            BasicText(
                                placeholder,
                                // The field already carries it as its label.
                                Modifier.clearAndSetSemantics { },
                                style = textStyle.copy(color = secondaryColor),
                                softWrap = false,
                                maxLines = 1,
                            )
                        }
                        inner()
                    }
                },
            )
        }

        // Close button, growing out of the field's trailing edge.
        Box(
            Modifier
                .layout { measurable, constraints ->
                    val width = constraints.maxWidth.toFloat()
                    val h = constraints.maxHeight.toFloat()
                    val p = close.value.coerceIn(0f, 1.2f)
                    val size = lerp(h * 0.4f, h, p.coerceIn(0f, 1f))
                    val placeable = measurable.measure(Constraints.fixed(size.roundToInt(), size.roundToInt()))
                    val centerX = lerp(width - h / 2f - gap, width - h / 2f, p)
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.placeRelative((centerX - size / 2f).roundToInt(), ((h - size) / 2f).roundToInt())
                    }
                }
                .graphicsLayer { alpha = close.value.coerceIn(0f, 1f) }
                .then(glass { 0f })
                .then(
                    if (expanded) {
                        Modifier
                            .clearAndSetSemantics {
                                contentDescription = "Close search"
                                role = Role.Button
                                onClick { onChange(false); true }
                            }
                            .pointerInput(Unit) { detectTapGestures { onChange(false) } }
                    } else {
                        // Collapsed it overlaps the search button, which must get the tap.
                        Modifier.clearAndSetSemantics { }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            BasicText("✕", style = TextStyle(color = labelColor, fontSize = 17.sp))
        }
    }
}

/** Magnifying glass drawn with Compose so the library needs no icon assets. */
@Composable
private fun SearchIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(20.dp)) {
        val stroke = 2.2.dp.toPx()
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(color, radius, center, style = Stroke(stroke))
        val start = Offset(center.x + radius * 0.72f, center.y + radius * 0.72f)
        val end = Offset(size.width * 0.92f, size.height * 0.92f)
        drawLine(color, start, end, stroke, cap = StrokeCap.Round)
    }
}
