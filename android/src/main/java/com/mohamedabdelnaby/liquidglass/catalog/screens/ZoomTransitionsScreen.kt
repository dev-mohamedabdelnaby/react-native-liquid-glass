package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.catalog.glass.platformTextStyle
import com.mohamedabdelnaby.liquidglass.catalog.glass.platformCopy
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.LargeTitle
import com.mohamedabdelnaby.liquidglass.catalog.glass.SystemStepper
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.isSystemInDarkTheme
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.ListRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.groupedRow
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

private enum class Panel { None, Inbox, Compose }

private val PanelShape = RoundedCornerShape(32.dp)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomTransitionsScreen() {
    var panel by remember { mutableStateOf(Panel.None) }
    var unread by remember { mutableIntStateOf(3) }
    BackHandler(panel != Panel.None) { panel = Panel.None }

    // matchedTransitionSource + .navigationTransition(.zoom): each panel is a container transform
    // that grows out of the glass button that opened it and shrinks back into it.
    SharedTransitionLayout {
        val shared = this
        GlassScreen(
            title = "",
            titleContent = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Circle, null, Modifier.size(8.dp), tint = SystemColors.Green)
                    Text("Online", color = SystemColors.Green, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            },
            actions = {
                AnimatedVisibility(panel != Panel.Inbox, enter = fadeIn(), exit = fadeOut()) {
                    // The badge overhangs the button by 4 dp; this Box takes that room (and the
                    // offset puts the button back where it was), because the badge is drawn in the
                    // zoom's overlay clipped to this Box.
                    Box(Modifier.offset(x = 4.dp, y = (-2).dp)) {
                        Box(Modifier.padding(top = 4.dp, end = 4.dp).zoomSource(shared, this@AnimatedVisibility, Panel.Inbox)) {
                            GlassIconButton(Icons.Filled.Inbox, "Inbox, $unread unread", { panel = Panel.Inbox })
                        }
                        if (unread > 0) {
                            // Outside the zooming bounds, whose clip cut the badge while the panel
                            // opened and closed; drawn above the zoom, fading with the button.
                            Text(
                                if (unread > 99) "99+" else "$unread",
                                with(shared) { Modifier.renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f) }
                                    .animateEnterExit(enter = fadeIn(), exit = fadeOut())
                                    .align(Alignment.TopEnd)
                                    .background(SystemColors.Red, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            },
            overlay = {
                AnimatedVisibility(panel == Panel.Inbox, enter = fadeIn(), exit = fadeOut()) {
                    InboxPanel(unread, Modifier.zoomSource(shared, this, Panel.Inbox)) { panel = Panel.None }
                }
                AnimatedVisibility(panel == Panel.Compose, enter = fadeIn(), exit = fadeOut()) {
                    ComposePanel(Modifier.zoomSource(shared, this, Panel.Compose)) { panel = Panel.None }
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
                        "Tap the toolbar buttons — their sheets zoom out of the glass button that presented them.",
                        "Tap the bar buttons — their sheets grow out of the button that opened them.",
                    ),
                    Modifier.glassEffect(Glass.Regular, RoundedCornerShape(24.dp)).padding(16.dp),
                    textAlign = TextAlign.Center,
                )
                AnimatedVisibility(panel != Panel.Compose, enter = fadeIn(), exit = fadeOut()) {
                    GlassProminentButton(
                        { panel = Panel.Compose },
                        Modifier.zoomSource(shared, this, Panel.Compose),
                        size = GlassControlSize.Large,
                    ) {
                        Icon(Icons.Filled.EditNote, null)
                        Text("New Message")
                    }
                }
                Row(
                    Modifier.fillMaxWidth().glassEffect(Glass.Regular, RoundedCornerShape(20.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Unread badge: $unread", style = platformTextStyle(17.sp, MaterialTheme.typography.bodyLarge))
                    Fill()
                    SystemStepper(unread, { unread = it }, 0..99)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.zoomSource(scope: SharedTransitionScope, visibility: AnimatedVisibilityScope, key: Panel): Modifier =
    with(scope) {
        this@zoomSource.sharedBounds(
            rememberSharedContentState(key),
            visibility,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(PanelShape),
        )
    }

/** A sheet: [height] is its detent (1 = .large, 0.5 = .medium), with the large title under its buttons. */
@Composable
private fun PanelFrame(
    title: String,
    modifier: Modifier,
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    height: Float = 1f,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(12.dp), contentAlignment = Alignment.BottomCenter) {
        Column(modifier.fillMaxWidth().fillMaxHeight(height).glassEffect(Glass.Regular, PanelShape).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Box(Modifier.align(Alignment.CenterStart)) { leading() }
                Box(Modifier.align(Alignment.CenterEnd)) { trailing() }
            }
            LargeTitle(title)
            content()
        }
    }
}

@Composable
private fun InboxPanel(unread: Int, modifier: Modifier, onDone: () -> Unit) {
    PanelFrame("Inbox", modifier, leading = {}, trailing = { GlassIconButton(Icons.Filled.Check, "Done", onDone) }, height = 0.5f) {
        val count = maxOf(unread, 1)
        LazyColumn {
            items(count) { index ->
                ListRow(Modifier.groupedRow(index, count), divider = index != count - 1) {
                    Icon(Icons.Filled.MarkEmailUnread, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Message ${index + 1}")
                }
            }
        }
    }
}

@Composable
private fun ComposePanel(modifier: Modifier, onClose: () -> Unit) {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    PanelFrame(
        "New Message", modifier,
        leading = { GlassIconButton(Icons.Filled.Close, "Cancel", onClose) },
        trailing = { GlassProminentButton(onClose, shape = GlassCircle) { Icon(Icons.Filled.ArrowUpward, "Send") } },
    ) {
        Column {
            listOf("To" to to, "Subject" to subject).forEachIndexed { index, (label, value) ->
                ListRow(Modifier.groupedRow(index, 2), divider = index == 0) {
                    BasicTextField(
                        value,
                        { if (index == 0) to = it else subject = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).semantics { contentDescription = label },
                        decorationBox = { field ->
                            Box {
                                if (value.isEmpty()) Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                field()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Preview(name = "Light") @Composable private fun ZoomLight() = PreviewLight { ZoomTransitionsScreen() }
@Preview(name = "Dark") @Composable private fun ZoomDark() = PreviewDark { ZoomTransitionsScreen() }
