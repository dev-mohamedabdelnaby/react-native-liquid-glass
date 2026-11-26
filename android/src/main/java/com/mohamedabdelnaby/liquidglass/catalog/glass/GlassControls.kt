package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidSlider
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidSwitch
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors
import com.mohamedabdelnaby.liquidglass.catalog.shapes.ShapedSlider
import com.mohamedabdelnaby.liquidglass.catalog.shapes.ShapedSwitch
import com.mohamedabdelnaby.liquidglass.catalog.shapes.shapeAccent

/** The slider, in Home's control shape (see ShapedSlider); [dropColor] and [liquid] no longer apply. */
@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    tint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null,
    dropColor: Color? = null,
    // false: no liquid lift for this slider; null follows Home's setting.
    liquid: Boolean? = null,
) =
    // Only a slider's own tint (not the default, the theme's primary) wins over Home's tint.
    ShapedSlider(value, onValueChange, modifier, valueRange, shapeAccent(tint.takeIf { it != MaterialTheme.colorScheme.primary }), contentDescription)

/** The switch, in Home's control shape (see ShapedSwitch); [dropColor] and [liquid] no longer apply. */
@Composable
fun GlassSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, dropColor: Color? = null, liquid: Boolean? = null) =
    ShapedSwitch(checked, onCheckedChange, modifier)

/** The inside of a glass search field: magnifier, text, and a clear button once there is text. */
@Composable
fun SearchFieldContent(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Search, null, tint = secondary)
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) Text(placeholder, color = secondary, maxLines = 1)
            BasicTextField(
                query,
                onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).semantics { contentDescription = placeholder },
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Cancel, "Clear",
                tint = secondary,
                modifier = Modifier.size(20.dp).glassClickable(label = "Clear") { onQueryChange("") },
            )
        }
    }
}

/** iOS `tertiarySystemFill`: the gray behind segmented controls, steppers and compact pickers. */
val TertiaryFill: Color @Composable get() =
    if (isMaterial) MaterialTheme.colorScheme.surfaceContainerHighest else Color(0xFF767680).copy(alpha = if (isSystemInDarkTheme()) 0.24f else 0.12f)

/** UIStepper: a gray capsule split into − and +, each dimmed at its end of [range]. */
@Composable
fun SystemStepper(value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    if (isMaterial) return MaterialStepper(value, onValueChange, range)
    Row(Modifier.size(94.dp, 32.dp).background(TertiaryFill, CircleShape), verticalAlignment = Alignment.CenterVertically) {
        StepperHalf(Icons.Filled.Remove, "Decrease", value > range.first) { onValueChange(value - 1) }
        Box(Modifier.size(0.5.dp, 18.dp).background(MaterialTheme.colorScheme.outlineVariant))
        StepperHalf(Icons.Filled.Add, "Increase", value < range.last) { onValueChange(value + 1) }
    }
}

@Composable
private fun RowScope.StepperHalf(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(Modifier.weight(1f).fillMaxHeight().glassClickable(enabled, label, onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.3f))
    }
}
