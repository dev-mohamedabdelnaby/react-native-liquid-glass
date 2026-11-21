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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.ripple
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.indication
import androidx.compose.foundation.hoverable
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
    // The button's own color (an error-red label, a prominent tint); null takes the role's.
    color: Color?,
    shape: Shape,
    size: GlassControlSize,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    val sized = modifier.defaultMinSize(minHeight = size.minHeight)
    if (prominent) {
        val scheme = MaterialTheme.colorScheme
        val colors = if (color == null) ButtonDefaults.buttonColors()
        else ButtonDefaults.buttonColors(containerColor = color, contentColor = moreContrasting(color, scheme.surface, scheme.onSurface))
        Button(onClick, sized, enabled, shape, colors, content = content)
        return
    }
    val own = color
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
internal fun MaterialIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier, size: Dp, tint: Color?, enabled: Boolean) {
    // A button's own tint; null takes the role's content color.
    val own = tint
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
 * Material 3's classic slider: a round handle on a thin track (the user's pick over material3
 * 1.4's default bar handle, built from its public parts), and a light haptic each tenth of the way.
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
    val label = sliderLabel(value, valueRange)
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
            // The pressed state is the ripple's halo around the handle.
            Box(
                Modifier
                    .size(20.dp)
                    .hoverable(interaction)
                    .indication(interaction, ripple(bounded = false, radius = 20.dp))
                    .shadow(1.dp, CircleShape)
                    .background(colors.thumbColor, CircleShape),
            )
        },
        track = { SliderDefaults.Track(it, Modifier.height(4.dp), colors = colors, drawStopIndicator = null, thumbTrackGapSize = 0.dp, trackInsideCornerSize = 0.dp) },
    ) }
}

/** Material 3's switch: a check on the thumb while on, nothing while off, in the theme's accent. */
@Composable
internal fun MaterialSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier) = Switch(
    checked, onCheckedChange, modifier,
    thumbContent = if (checked) { { Icon(Icons.Filled.Check, null, Modifier.size(SwitchDefaults.IconSize)) } } else null,
)

/** A stepper as Material has none: − and + tonal icon buttons, each disabled at its end of [range]. */
@Composable
internal fun MaterialStepper(value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton({ onValueChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Filled.Remove, "Decrease") }
        FilledTonalIconButton({ onValueChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Filled.Add, "Increase") }
    }
}

/** A single choice among a few options as Material's filter chips, the chosen one checked. */
@Composable
fun MaterialChoiceChips(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, option ->
            FilterChip(
                selected = index == selected,
                onClick = { onSelect(index) },
                label = { Text(option) },
                leadingIcon = if (index == selected) { { Icon(Icons.Filled.Check, null, Modifier.size(FilterChipDefaults.IconSize)) } } else null,
            )
        }
    }
}
