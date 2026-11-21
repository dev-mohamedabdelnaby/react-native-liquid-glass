package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.catalog.glass.platformCopy
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBottomSheet
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSurfaceHost
import com.mohamedabdelnaby.liquidglass.catalog.glass.ListRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

private val Places = listOf(
    "Mountains" to Icons.Filled.Landscape,
    "Ocean" to Icons.Filled.Waves,
    "Desert" to Icons.Filled.WbSunny,
    "Forest" to Icons.Filled.Forest,
)
private val HeaderColors = listOf(SystemColors.Orange, SystemColors.Pink, SystemColors.Purple, SystemColors.Indigo)
private val SidebarWidth = 280.dp
private val InspectorWidth = 300.dp
private val HeaderHeight = 300.dp

@Composable
fun SplitViewScreen() {
    var presented by remember { mutableStateOf(false) }
    GlassScreen(
        "Split View",
        overlay = {
            // fullScreenCover: a slide, no fade.
            AnimatedVisibility(presented, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                SplitViewDemo(onClose = { presented = false })
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                platformCopy(
                    "The sidebar and inspector float on Liquid Glass. The header artwork mirrors and blurs beneath them with the background extension effect.",
                    "The sidebar and inspector sit beside the content on wide screens.",
                ),
                Modifier.glassEffect(Glass.Regular, RoundedCornerShape(24.dp)).padding(16.dp),
                textAlign = TextAlign.Center,
            )
            GlassProminentButton({ presented = true }, size = GlassControlSize.Large) {
                Icon(Icons.AutoMirrored.Filled.ViewSidebar, null)
                Text("Open Split View")
            }
            Footnote("Best viewed on iPad or in landscape.")
        }
    }
}

/** A NavigationSplitView with an inspector: three panes when wide, navigation on a phone. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SplitViewDemo(onClose: () -> Unit) {
    var selection by remember { mutableStateOf(Places[0]) }
    var inspector by remember { mutableStateOf(true) }
    var extension by remember { mutableStateOf(true) }
    var phoneDetail by remember { mutableStateOf(false) }
    BackHandler { if (phoneDetail) phoneDetail = false else onClose() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        if (wide) {
            // Everything non-glass is the backdrop source, so the floating panes refract it.
            GlassSurfaceHost(backdrop = { source ->
                Box(source) {
                    PlainBackdrop()
                    if (extension) {
                        ExtensionStrip(HeaderColors.first(), Modifier.width(SidebarWidth + 16.dp).align(Alignment.TopStart), mirrorOf = HeaderColors[1])
                        if (inspector) ExtensionStrip(HeaderColors.last(), Modifier.width(InspectorWidth + 16.dp).align(Alignment.TopEnd), mirrorOf = HeaderColors[2])
                    }
                    Detail(
                        selection,
                        Modifier.padding(start = SidebarWidth + 16.dp, end = if (inspector) InspectorWidth + 16.dp else 0.dp),
                    )
                }
            }) {
                Row(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(8.dp)) {
                    Sidebar(selection, { selection = it }, onClose, Modifier.width(SidebarWidth).fillMaxHeight())
                    Box(Modifier.weight(1f)) {
                        GlassIconButton(
                            Icons.Filled.Info, if (inspector) "Hide inspector" else "Show inspector", { inspector = !inspector },
                            Modifier.align(Alignment.TopEnd).padding(8.dp),
                        )
                    }
                    // Slides from the trailing edge; the slide offset is not mirrored for us.
                    val trailing = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
                    AnimatedVisibility(inspector, enter = slideInHorizontally { it * trailing } + fadeIn(), exit = slideOutHorizontally { it * trailing } + fadeOut()) {
                        Inspector(extension, { extension = it }, Modifier.width(InspectorWidth).fillMaxHeight().glassEffect(Glass.Regular, RoundedCornerShape(28.dp)))
                    }
                }
            }
        } else {
            GlassSurfaceHost(backdrop = { source -> Box(source) { PlainBackdrop(); if (phoneDetail) Detail(selection) } }) {
                AnimatedContent(phoneDetail, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "pane") { detail ->
                    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(8.dp)) {
                        if (detail) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                GlassIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Places", { phoneDetail = false })
                                GlassIconButton(Icons.Filled.Info, if (inspector) "Hide inspector" else "Show inspector", { inspector = !inspector })
                            }
                        } else {
                            Sidebar(selection, { selection = it; phoneDetail = true }, onClose, Modifier.fillMaxSize())
                        }
                    }
                }
                // .inspector on a compact width: a sheet over the detail (inside its host, so it refracts it).
                if (phoneDetail && inspector) {
                    GlassBottomSheet(onDismissRequest = { inspector = false }) {
                        Inspector(extension, { extension = it }, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(selection: Pair<String, ImageVector>, onSelect: (Pair<String, ImageVector>) -> Unit, onClose: () -> Unit, modifier: Modifier) {
    Column(modifier.glassEffect(Glass.Regular, RoundedCornerShape(28.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        GlassIconButton(Icons.Filled.Close, "Close", onClose)
        Text("Places", style = MaterialTheme.typography.headlineLarge)
        Places.forEach { place ->
            val selected = place == selection
            ListRow(
                Modifier
                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent, RoundedCornerShape(14.dp))
                    .selectable(selected, role = Role.Tab) { onSelect(place) },
                divider = false,
            ) {
                Icon(place.second, null, tint = MaterialTheme.colorScheme.primary)
                Text(place.first)
            }
        }
    }
}

@Composable
private fun Inspector(extension: Boolean, onExtension: (Boolean) -> Unit, modifier: Modifier) {
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Inspector", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        SwitchRow("Background extension", extension, onExtension)
    }
}

@Composable
private fun Detail(place: Pair<String, ImageVector>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(HeaderHeight).background(Brush.horizontalGradient(HeaderColors)), contentAlignment = Alignment.Center) {
            Icon(place.second, null, Modifier.size(80.dp), tint = Color.White)
        }
        Text(place.first, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(
            "Toggle the background extension effect from the inspector to compare how the artwork fills the space under the sidebar and inspector.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * `backgroundExtensionEffect()`: the header's edge strip, mirrored outward and heavily blurred,
 * drawn under a side pane. For a gradient header the mirror of its edge is the edge color running
 * back toward its neighbour, so [edge] meets the header and [mirrorOf] lies at the far side.
 */
@Composable
private fun ExtensionStrip(edge: Color, modifier: Modifier, mirrorOf: Color, height: Dp = HeaderHeight) {
    val atStart = edge == HeaderColors.first()
    val colors = if (atStart) listOf(mirrorOf, edge) else listOf(edge, mirrorOf)
    Box(modifier.height(height).blur(40.dp).background(Brush.horizontalGradient(colors)))
}

@Preview(name = "Light") @Composable private fun SplitViewLight() = PreviewLight { SplitViewScreen() }
@Preview(name = "Dark") @Composable private fun SplitViewDark() = PreviewDark { SplitViewScreen() }
