package com.mohamedabdelnaby.liquidglass.engine.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.engine.liquid.LiquidTabBar
import com.mohamedabdelnaby.liquidglass.engine.liquid.LiquidTabItem
import androidx.compose.ui.unit.sp

/** The scrolling column most demo screens use, padded for the glass bars. */
@Composable
fun DemoColumn(
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    spacing: Dp = 28.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

/** A titled group of demos. */
@Composable
fun DemoSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
fun Footnote(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
    )
}

/**
 * A segmented control as react-native-liquid's top tab bar: a glass capsule whose selection pill
 * lifts into a liquid drop under the finger and glides to the chosen option.
 */
@Composable
fun GlassSegmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dropColor: Color? = null,
    // false: the thumb slides without the liquid drop; null follows Home's setting.
    liquid: Boolean? = null,
) = LiquidTabBar(selected, onSelect, options.map { LiquidTabItem(it) }, modifier.fillMaxWidth(), height = 32.dp, segmented = true, dropColor = dropColor, liquid = liquid)

/** A settings-style group: optional header and footer around rows on rounded glass. */
@Composable
fun GlassListSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    glass: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        header?.let {
            Text(
                // iOS 26 section headers: title case, headline weight.
                it,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
        // Opaque sections (like an iOS Form) keep a long settings page to a few live glass surfaces.
        val surface = if (glass) Modifier.glassEffect(Glass.Regular, RoundedCornerShape(26.dp))
        else Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(26.dp))
        Column(Modifier.fillMaxWidth().then(surface), content = content)
        footer?.let { Footnote(it, Modifier.padding(horizontal = 16.dp)) }
    }
}

/** One row of a [GlassListSection]. */
@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    divider: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
        // iOS 26 inset-grouped separators stop short of both edges.
        if (divider) HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun RowScope.Fill() = Spacer(Modifier.weight(1f))

/** An opaque row of an iOS-style grouped list: only the first and last rows round their corners. */
@Composable
fun Modifier.groupedRow(index: Int, count: Int, radius: Dp = 26.dp): Modifier {
    val top = if (index == 0) radius else 0.dp
    val bottom = if (index == count - 1) radius else 0.dp
    return clip(RoundedCornerShape(top, top, bottom, bottom)).background(MaterialTheme.colorScheme.surface)
}

/**
 * Fades and scales [content] in when it first appears. A container morphs only the glass of a
 * member, so a member's label uses this to arrive with its morphing glass.
 */
@Composable
fun AppearAnimated(enabled: Boolean = true, content: @Composable () -> Unit) {
    val state = remember { MutableTransitionState(!enabled).apply { targetState = true } }
    AnimatedVisibility(state, enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.6f), exit = ExitTransition.None) { content() }
}
