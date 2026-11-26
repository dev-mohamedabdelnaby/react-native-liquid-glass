package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.glassIcon
import com.mohamedabdelnaby.liquidglass.catalog.glass.MaterialSectionHeader
import com.mohamedabdelnaby.liquidglass.catalog.glass.platformCopy
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassTransition
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.mohamedabdelnaby.liquidglass.catalog.glass.LargeTitle
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial
import androidx.compose.material3.FloatingActionButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.CollapsedBarTitle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WaterDrop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.BarIcon
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBottomBar
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenu
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuDivider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuItem
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.ListRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.RevealPill
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.groupedRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.rememberReveal
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

private const val ShareUrl = "https://developer.apple.com/documentation/swiftui/applying-liquid-glass-to-custom-views"

private val ComposeActions = listOf(
    Triple("New Note", "note.text", Icons.AutoMirrored.Filled.StickyNote2),
    Triple("New Photo", "camera", Icons.Filled.CameraAlt),
    Triple("New Folder", "folder.badge.plus", Icons.Filled.CreateNewFolder),
)

@Composable
fun ToolbarScreen() {
    var favorite by remember { mutableStateOf(false) }
    var morph by remember { mutableStateOf(true) }
    var groupGlass by remember { mutableStateOf(true) }
    var composeOpen by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("None") }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val material = isMaterial

    GlassScreen(
        title = "Toolbar",
        // A plain List page, like iOS: no photo backdrop.
        backdrop = { PlainBackdrop(it) },
        photo = false,
        // navigationTitle without .inline: a large title that collapses into the bar.
        titleContent = { CollapsedBarTitle("Toolbar", scrolled) },
        actions = {
            GlassIconButton(
                if (favorite) glassIcon("heart.fill", Icons.Filled.Favorite) else glassIcon("heart", Icons.Outlined.FavoriteBorder),
                if (favorite) "Unfavorite" else "Favorite",
                { favorite = !favorite; lastAction = if (favorite) "Favorited" else "Unfavorited" },
            )
            Spacer(Modifier.width(8.dp))
            // One shared capsule behind Share and More, unless its background is hidden; as with
            // iOS's .animation() binding, the capsule materializes in and out.
            GlassEffectContainer { Row(Modifier.glassEffect(Glass.Regular.interactive(), transition = GlassTransition.Materialize, present = groupGlass)) {
                BarIcon(glassIcon("square.and.arrow.up", Icons.Outlined.Share), "Share", { context.shareText(ShareUrl) })
                GlassMenu(anchor = { open -> BarIcon(glassIcon("ellipsis", Icons.Filled.MoreHoriz), "More", open) }) { dismiss ->
                    GlassMenuItem("Morph Compose Button", { morph = !morph; dismiss() }, icon = glassIcon("wand.and.stars", Icons.Filled.AutoFixHigh), checked = morph)
                    GlassMenuItem("Glass Behind Group", { groupGlass = !groupGlass; dismiss() }, icon = glassIcon("drop", Icons.Outlined.WaterDrop), checked = groupGlass)
                    GlassMenuDivider()
                    GlassMenuItem("Select Rows", { lastAction = "Select rows"; dismiss() }, icon = glassIcon("checkmark.circle", Icons.Outlined.CheckCircle))
                    GlassMenuItem("Sort", { lastAction = "Sort"; dismiss() }, icon = glassIcon("arrow.up.arrow.down", Icons.AutoMirrored.Rounded.Sort))
                }
            } }
        },
        bottomBar = {
            GlassBottomBar {
                Row(Modifier.glassEffect(Glass.Regular.interactive())) {
                    BarIcon(glassIcon("chevron.backward", Icons.AutoMirrored.Rounded.KeyboardArrowLeft), "Back", { lastAction = "Back" })
                    BarIcon(glassIcon("chevron.forward", Icons.AutoMirrored.Rounded.KeyboardArrowRight), "Forward", { lastAction = "Forward" })
                }
                Fill()
            }
        },
        floatingActionButton = {
            ComposeButton(composeOpen, morph, onToggle = { composeOpen = !composeOpen }) { action ->
                lastAction = action
                composeOpen = false
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 96.dp,
            ),
        ) {
            // Material: the top app bar shows the title.
            if (!material) item { LargeTitle("Toolbar", Modifier.padding(bottom = 12.dp)) }
            item {
                Column(Modifier.padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column {
                        ListRow(Modifier.groupedRow(0, 3)) {
                            Text("Last action")
                            Fill()
                            Text(lastAction, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ListRow(Modifier.groupedRow(1, 3)) { SwitchRow("Morph compose button", morph) { morph = it } }
                        ListRow(Modifier.groupedRow(2, 3), divider = false) { SwitchRow("Glass behind Share & More", groupGlass) { groupGlass = it } }
                    }
                    Footnote(platformCopy("These options are also in the ••• menu.", "These options are also in the More menu."), Modifier.padding(horizontal = 16.dp))
                }
            }
            if (material) item { MaterialSectionHeader("Rows", Modifier.padding(start = 16.dp, bottom = 8.dp)) }
            else item {
                // Section("Rows"): iOS 26 headers are title case, headline weight.
                Text(
                    "Rows",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
                )
            }
            items(40) { index ->
                ListRow(Modifier.groupedRow(index, 40), divider = index != 39) {
                    Icon(glassIcon("photo", Icons.Filled.Photo), null, tint = MaterialTheme.colorScheme.primary)
                    Text("Row ${index + 1}")
                }
            }
        }
    }
}

/**
 * As on iOS: with morphing on (`glassEffectID`), each action's glass grows out of the compose
 * button and flows back into it on close, riding one spring with its label. With it off
 * (`.materialize`), each action grows in place and the container stops merging.
 */
@Composable
private fun ComposeButton(open: Boolean, morph: Boolean, onToggle: () -> Unit, onAction: (String) -> Unit) {
    // A fixed frame, so the container's origin stays put while the column grows; taps pass through it.
    GlassEffectContainer(Modifier.size(240.dp, 260.dp), spacing = if (morph) 10.dp else 0.dp) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val reveal = rememberReveal(open)
                if (reveal.shown) {
                    ComposeActions.forEachIndexed { index, (title, symbol, icon) ->
                        RevealPill(title, glassIcon(symbol, icon), Glass.Regular.interactive(), reveal, below = ComposeActions.size - 1 - index, morph = morph) { onAction(title) }
                    }
                }
                // Material: Android's floating action button.
                if (isMaterial) FloatingActionButton(onToggle) {
                    AnimatedContent(open, transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) }, label = "compose") {
                        Icon(if (it) glassIcon("xmark", Icons.Filled.Close) else glassIcon("square.and.pencil", Icons.Filled.EditNote), if (it) "Close" else "Compose")
                    }
                } else Box(
                    Modifier
                        .size(60.dp)
                        .glassEffect(Glass.Regular.tint(SystemColors.Blue.copy(alpha = 0.85f)).interactive(), GlassCircle)
                        .glassClickable(label = if (open) "Close" else "Compose", onClick = onToggle),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(open, transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) }, label = "compose") {
                        Icon(if (it) glassIcon("xmark", Icons.Filled.Close) else glassIcon("square.and.pencil", Icons.Filled.EditNote), null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

@Preview(name = "Light") @Composable private fun ToolbarLight() = PreviewLight { ToolbarScreen() }
@Preview(name = "Dark") @Composable private fun ToolbarDark() = PreviewDark { ToolbarScreen() }
