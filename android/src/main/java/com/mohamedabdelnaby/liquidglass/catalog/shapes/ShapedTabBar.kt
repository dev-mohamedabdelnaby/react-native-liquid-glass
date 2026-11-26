package com.mohamedabdelnaby.liquidglass.catalog.shapes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.liquid.badgeText
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidTabItem

/**
 * The bottom tab bar in a [ControlShape]: Pill an accent bar with a white pill behind the selected
 * tab, Labeled an outlined bar, Glow a glowing selected tab. [color] (a screen's bar colour) is
 * the accent when set. It floats (user, 2026-11-26): its own container colour, above the system
 * navigation bar, with content scrolling under it.
 */
@Composable
internal fun ShapedTabBar(selectedIndex: Int, onSelect: (Int) -> Unit, tabs: List<LiquidTabItem>, modifier: Modifier, height: Dp, color: Color?) {
    Box(modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 16.dp, vertical = 8.dp)) {
        ShapedTabRow(tabs.map { it.label }, selectedIndex, onSelect, Modifier.fillMaxWidth().height(height), shapeAccent(color), inset = 6.dp) { i, tint ->
            val tab = tabs[i]
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)) {
                tab.icon?.let { icon ->
                    // M3's 16 dp badge, fixed: the count's measured height (font padding, an app font) made it an oval.
                    BadgedBox(badge = { if (tab.badge > 0) Badge(Modifier.height(16.dp)) { Text("${tab.badge}", style = badgeText()) } }) { Icon(icon, null, Modifier.size(24.dp), tint = tint) }
                }
                Text(tab.label, color = tint, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
