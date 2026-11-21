package com.mohamedabdelnaby.liquidglass.catalog.glass

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import kotlin.math.roundToInt
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Material counterparts of the controls: buttons, icon buttons, switch, slider, stepper. Each glass building block
 * calls its counterpart first thing when the style is Material, so the glass code after that call is unchanged.
 */

/**
 * Material's button roles: prominent glass is a filled button in its tint; glass a filled tonal
 * button; clear glass an outlined one; frosted (not liquid) glass, a quiet action, a text button.
 */
@Composable
internal fun MaterialButton(
    onClick: () -> Unit,
    modifier: Modifier,
    prominent: Boolean,
    glass: Glass,
    color: Color,
    shape: Shape,
    size: GlassControlSize,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    val sized = modifier.defaultMinSize(minHeight = size.minHeight)
    if (prominent) {
        val scheme = MaterialTheme.colorScheme
        val label = if (color == scheme.primary) scheme.onPrimary else moreContrasting(color, scheme.surface, scheme.onSurface)
        Button(onClick, sized, enabled, shape, ButtonDefaults.buttonColors(containerColor = color, contentColor = label), content = content)
        return
    }
    // A glass button's own label color (an error red) stays; the default takes the role's.
    val own = color.takeIf { it != MaterialTheme.colorScheme.onSurface }
    when {
        !glass.liquid -> TextButton(onClick, sized, enabled, colors = own?.let { ButtonDefaults.textButtonColors(contentColor = it) } ?: ButtonDefaults.textButtonColors(), content = content)
        glass.clear -> OutlinedButton(onClick, sized, enabled, colors = own?.let { ButtonDefaults.outlinedButtonColors(contentColor = it) } ?: ButtonDefaults.outlinedButtonColors(), content = content)
        else -> FilledTonalButton(onClick, sized, enabled, shape, own?.let { ButtonDefaults.filledTonalButtonColors(contentColor = it) } ?: ButtonDefaults.filledTonalButtonColors(), content = content)
    }
}

/** iOS-style symbols the screens use, as Android draws them; [icon] itself when it has no Android form. */
internal fun androidIcon(icon: ImageVector): ImageVector = when (icon) {
    Icons.Filled.MoreHoriz -> Icons.Filled.MoreVert
    Icons.Outlined.IosShare -> Icons.Filled.Share
    Icons.AutoMirrored.Rounded.KeyboardArrowLeft -> Icons.AutoMirrored.Filled.ArrowBack
    Icons.AutoMirrored.Rounded.KeyboardArrowRight -> Icons.AutoMirrored.Filled.ArrowForward
    Icons.Filled.UnfoldMore -> Icons.Filled.ArrowDropDown
    else -> icon
}

/** [icon] in Android's form while the style is Material. */
@Composable
fun nativeIcon(icon: ImageVector): ImageVector = if (isMaterial) androidIcon(icon) else icon

/** An icon button: plain in a bar, filled tonal on its own. */
@Composable
internal fun MaterialIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier, size: Dp, tint: Color, enabled: Boolean) {
    val own = tint.takeIf { it != MaterialTheme.colorScheme.onSurface }
    val android = androidIcon(icon)
    if (LocalMaterialBar.current) {
        IconButton(onClick, modifier, enabled) { Icon(android, contentDescription, tint = own ?: LocalContentColor.current) }
    } else {
        FilledTonalIconButton(onClick, modifier.size(size), enabled) { Icon(android, contentDescription, tint = own ?: LocalContentColor.current) }
    }
}

/** Which tenth of [range] [value] is in: the slider ticks a haptic each time it changes. */
internal fun sliderDetent(value: Float, range: ClosedFloatingPointRange<Float>): Int {
    val span = range.endInclusive - range.start
    if (span <= 0f) return 0
    return (((value - range.start) / span).coerceIn(0f, 1f) * 10f).toInt()
}

/** The value as the indicator and TalkBack say it: a percent for a 0–1 slider, else the number. */
internal fun sliderLabel(value: Float, range: ClosedFloatingPointRange<Float>): String =
    if (range.endInclusive <= 1f) "${(value * 100).roundToInt()}%" else "${value.roundToInt()}"

private fun contrast(a: Color, b: Color): Float {
    val (hi, lo) = a.luminance().let { la -> b.luminance().let { lb -> maxOf(la, lb) to minOf(la, lb) } }
    return (hi + 0.05f) / (lo + 0.05f)
}

/** Of [first] and [second], the one that reads better on [background]. */
internal fun moreContrasting(background: Color, first: Color, second: Color): Color =
    if (contrast(background, first) >= contrast(background, second)) first else second

/**
 * Material 3 Expressive's slider: the thicker 24 dp track, a 44 dp handle that narrows while
 * pressed, a value indicator above it while held, and a light haptic each tenth of the way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    tint: Color,
    contentDescription: String?,
) {
    val scheme = MaterialTheme.colorScheme
    // A slider's own tint, else the theme's primary (Home's tint when set: see the theme).
    val accent = tint
    val colors = SliderDefaults.colors(
        thumbColor = LocalGlassSettings.current.dropColor?.copy(alpha = 1f) ?: accent,
        activeTrackColor = accent,
        activeTickColor = moreContrasting(accent, scheme.surface, scheme.onSurface),
    )
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val held = interaction.collectIsPressedAsState().value || interaction.collectIsDraggedAsState().value
    val label = sliderLabel(value, valueRange)
    // "Remove animations" (animator scale 0): the indicator shows and hides at once.
    val context = LocalContext.current
    val still = remember { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
    // One node for TalkBack, the slider's own replaced: its label, value ("Blur, 3") and adjust action.
    Box(modifier.clearAndSetSemantics {
        contentDescription?.let { this.contentDescription = it }
        stateDescription = label
        progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange)
        setProgress { onValueChange(it.coerceIn(valueRange)); true }
    }) { Slider(
        value = value,
        onValueChange = {
            // Only on a step, never continuously.
            if (sliderDetent(it, valueRange) != sliderDetent(value, valueRange)) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            onValueChange(it)
        },
        modifier = Modifier.fillMaxWidth(),
        valueRange = valueRange,
        colors = colors,
        interactionSource = interaction,
        thumb = {
            Box(contentAlignment = Alignment.Center) {
                SliderDefaults.Thumb(interaction, colors = colors, thumbSize = DpSize(4.dp, 44.dp))
                AnimatedVisibility(
                    held,
                    // Above the handle, without changing the slider's size.
                    Modifier.layout { measurable, _ ->
                        val p = measurable.measure(Constraints())
                        layout(0, 0) { p.place(-p.width / 2, -p.height - 30.dp.roundToPx()) }
                    },
                    enter = if (still) EnterTransition.None else scaleIn(spring(dampingRatio = 0.6f, stiffness = 800f), transformOrigin = TransformOrigin(0.5f, 1f)) + fadeIn(),
                    exit = if (still) ExitTransition.None else scaleOut(spring(stiffness = 800f), transformOrigin = TransformOrigin(0.5f, 1f)) + fadeOut(),
                ) {
                    Surface(shape = CircleShape, color = scheme.inverseSurface, contentColor = scheme.inverseOnSurface) {
                        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        track = { SliderDefaults.Track(it, Modifier.height(24.dp), colors = colors, thumbTrackGapSize = 6.dp, trackInsideCornerSize = 4.dp) },
    ) }
}

/** Material 3 Expressive's switch: a check or ✕ on the thumb, in the theme's accent. */
@Composable
internal fun MaterialSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier) = Switch(
    checked, onCheckedChange, modifier,
    thumbContent = { Icon(if (checked) Icons.Filled.Check else Icons.Filled.Close, null, Modifier.size(SwitchDefaults.IconSize)) },
)

/** A stepper as Material has none: − and + tonal icon buttons, each disabled at its end of [range]. */
@Composable
internal fun MaterialStepper(value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton({ onValueChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Filled.Remove, "Decrease") }
        FilledTonalIconButton({ onValueChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Filled.Add, "Increase") }
    }
}
