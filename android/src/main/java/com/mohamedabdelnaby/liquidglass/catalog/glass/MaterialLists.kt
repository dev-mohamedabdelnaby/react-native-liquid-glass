package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/*
 * Material counterparts of the grouped lists: sections, headers, rows. Each glass building block
 * calls its counterpart first thing when the style is Material, so the glass code after that call is unchanged.
 */

/** Inside a [MaterialListSection]: each row is its own segment of the group. */
private val LocalMaterialSection = staticCompositionLocalOf { false }

/** The gap between a group's segments, and their corners: small inside the group, large at its ends. */
private val SegmentGap = 2.dp
private val SegmentCorner = 4.dp
private val GroupCorner = 20.dp

/**
 * A settings group as Android 16 draws it: a primary-colored section header, then each row on its
 * own tonal segment with a small gap between, the group's ends rounded, and the footer below.
 */
@Composable
internal fun MaterialListSection(modifier: Modifier, header: String?, footer: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        header?.let { MaterialSectionHeader(it, Modifier.padding(horizontal = 16.dp)) }
        CompositionLocalProvider(LocalMaterialSection provides true) {
            // Clipping the group rounds the ends of its first and last segments.
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(GroupCorner)), verticalArrangement = Arrangement.spacedBy(SegmentGap), content = content)
        }
        footer?.let { Text(it, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

/** A section header: `titleSmall` in the primary color, as Android's settings lists have them. */
@Composable
internal fun MaterialSectionHeader(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

/** One list row: 56 dp at least, no divider; in a section, its own tonal segment. */
@Composable
internal fun MaterialListRow(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    val segment = if (LocalMaterialSection.current) Modifier.clip(RoundedCornerShape(SegmentCorner)).background(MaterialTheme.colorScheme.surfaceContainerHigh) else Modifier
    Row(
        // The segment first, so a clickable row's ripple follows its corners.
        segment.then(modifier).fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

/** A row of a list laid out by hand ([index] of [count]): a segment of a group, as in [MaterialListSection]. */
@Composable
internal fun Modifier.materialSegment(index: Int, count: Int): Modifier {
    val top = if (index == 0) GroupCorner else SegmentCorner
    val bottom = if (index == count - 1) GroupCorner else SegmentCorner
    return padding(bottom = if (index == count - 1) 0.dp else SegmentGap)
        .clip(RoundedCornerShape(top, top, bottom, bottom))
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
}
