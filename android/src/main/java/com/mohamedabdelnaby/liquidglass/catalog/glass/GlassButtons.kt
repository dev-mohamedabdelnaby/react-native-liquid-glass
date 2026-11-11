package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** SwiftUI's `controlSize`, at react-native-liquid's LiquidButton sizes. */
enum class GlassControlSize(val minHeight: Dp, val horizontal: Dp, val vertical: Dp, val fontSize: TextUnit) {
    Small(34.dp, 14.dp, 6.dp, 15.sp),
    Regular(44.dp, 20.dp, 8.dp, 17.sp),
    Large(52.dp, 24.dp, 10.dp, 17.sp),
}

/** A click with no ripple: the glass itself is the press feedback. [label] names icon-only controls. */
fun Modifier.glassClickable(enabled: Boolean = true, label: String? = null, onClick: () -> Unit): Modifier =
    (if (label != null) semantics { contentDescription = label } else this).clickable(
        interactionSource = null,
        indication = null,
        enabled = enabled,
        onClickLabel = label,
        role = Role.Button,
        onClick = onClick,
    )

/** `.buttonStyle(.glass)`: a glass surface with a primary-colored label. */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glass: Glass = Glass.Regular,
    shape: RoundedCornerShape = CapsuleShape,
    size: GlassControlSize = GlassControlSize.Regular,
    enabled: Boolean = true,
    // iOS and react-native-liquid label glass buttons in the label color, not the accent.
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    transition: GlassTransition = GlassTransition.MatchedGeometry,
    unionId: Any? = null,
    content: @Composable RowScope.() -> Unit,
) {
    GlassButtonSurface(onClick, modifier, glass, shape, size, enabled, contentColor, transition, unionId, content)
}

/** `.buttonStyle(.glassProminent)`: glass tinted with the accent and a white label. */
@Composable
fun GlassProminentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    shape: RoundedCornerShape = CapsuleShape,
    size: GlassControlSize = GlassControlSize.Regular,
    enabled: Boolean = true,
    transition: GlassTransition = GlassTransition.MatchedGeometry,
    unionId: Any? = null,
    content: @Composable RowScope.() -> Unit,
) {
    GlassButtonSurface(
        onClick, modifier, Glass.Regular.tint(tint).copy(prominent = true), shape, size, enabled, Color.White,
        transition, unionId, content,
    )
}

@Composable
private fun GlassButtonSurface(
    onClick: () -> Unit,
    modifier: Modifier,
    glass: Glass,
    shape: RoundedCornerShape,
    size: GlassControlSize,
    enabled: Boolean,
    contentColor: Color,
    transition: GlassTransition,
    unionId: Any?,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.45f)
            .glassEffect(glass.interactive(enabled), shape, transition, unionId)
            .glassClickable(enabled, onClick = onClick)
            .defaultMinSize(minWidth = size.minHeight, minHeight = size.minHeight)
            .padding(horizontal = if (shape === GlassCircle) 0.dp else size.horizontal, vertical = size.vertical),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides MaterialTheme.typography.bodyLarge.copy(fontSize = size.fontSize, fontWeight = FontWeight.SemiBold),
        ) { content() }
    }
}

/** A circular glass icon button, the usual toolbar item. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glass: Glass = Glass.Regular,
    size: Dp = 44.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    transition: GlassTransition = GlassTransition.MatchedGeometry,
    unionId: Any? = null,
) {
    Box(
        modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.45f)
            .glassEffect(glass.interactive(enabled), GlassCircle, transition, unionId)
            .glassClickable(enabled, contentDescription, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.45f))
    }
}

/** An icon with no glass of its own, for use inside a shared glass group. */
@Composable
fun BarIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(44.dp)
            .clickable(source, null, role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}
