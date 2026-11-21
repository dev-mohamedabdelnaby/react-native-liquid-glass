package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.TertiaryFill
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassPopover
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.DialogAction
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassAlertDialog
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenu
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuDivider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuItem
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlertsScreen() {
    var alert by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("None") }
    val haptics = LocalHapticFeedback.current

    GlassScreen("Alerts & Dialogs") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Last action: $lastAction", style = MaterialTheme.typography.titleMedium)
            GlassProminentButton({ alert = true }) {
                Icon(Icons.Filled.Warning, null)
                Text("Show Alert")
            }
            // On iOS 26 the confirmation dialog is a glass popover anchored to its button.
            Box {
                // .glass with .tint(.red): red label on plain glass.
                GlassButton({ dialog = true }, contentColor = MaterialTheme.colorScheme.error) {
                    Icon(Icons.Filled.Delete, null)
                    Text("Show Confirmation Dialog")
                }
                // iOS 27: a popover above the button, its actions as pills; tapping outside cancels.
                // Material: a confirmation is an alert dialog (below).
                if (!isMaterial) GlassPopover(dialog, { dialog = false; lastAction = "Cancelled" }) {
                    Column(
                        Modifier.width(240.dp).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Delete this photo?", Modifier.padding(vertical = 4.dp), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        DialogPill("Delete", MaterialTheme.colorScheme.error) { lastAction = "Deleted"; dialog = false }
                        DialogPill("Archive", MaterialTheme.colorScheme.onSurface) { lastAction = "Archived"; dialog = false }
                    }
                }
            }
            // .contextMenu: the screen blurs, the artwork lifts and the menu grows below it.
            GlassMenu(preview = {
                // iOS's preview: the 120 pt symbol padded by 40 on the system's plain preview platter.
                Box(
                    Modifier.requiredSize(200.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Wallpaper, null, Modifier.size(120.dp)) }
            }, anchor = { open ->
                // As iOS's context menu: the tile shrinks a little while held, then pops into the preview.
                val hold = remember { MutableInteractionSource() }
                val held by hold.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    if (held) 0.94f else 1f,
                    if (held) tween(600, easing = LinearOutSlowInEasing) else spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "hold",
                )
                Box(
                    Modifier
                        .size(160.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .glassEffect(Glass.Regular, RoundedCornerShape(32.dp))
                        .semantics { contentDescription = "Artwork, long-press for actions" }
                        .combinedClickable(
                            interactionSource = hold,
                            indication = null,
                            onLongClickLabel = "Show actions",
                            onLongClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); open() },
                            onClick = {},
                        ),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Wallpaper, null, Modifier.size(60.dp)) }
            }) { dismiss ->
                GlassMenuItem("Copy", { lastAction = "Copied"; dismiss() }, icon = Icons.Filled.ContentCopy)
                GlassMenuItem("Share", { lastAction = "Shared"; dismiss() }, icon = Icons.Outlined.Share)
                GlassMenuDivider()
                GlassMenuItem("Delete", { lastAction = "Deleted"; dismiss() }, icon = Icons.Filled.Delete, destructive = true)
            }
            Footnote("Long-press the artwork for a context menu")
        }
    }

    if (dialog && isMaterial) {
        GlassAlertDialog(
            title = "Delete this photo?",
            message = "",
            onDismissRequest = { dialog = false; lastAction = "Cancelled" },
        ) {
            DialogAction("Delete", { lastAction = "Deleted"; dialog = false }, destructive = true)
            DialogAction("Archive", { lastAction = "Archived"; dialog = false })
            DialogAction("Cancel", { lastAction = "Cancelled"; dialog = false })
        }
    }
    if (alert) {
        GlassAlertDialog(
            title = "Enable Notifications?",
            message = "Alerts in iOS 26 and later use Liquid Glass.",
            onDismissRequest = { alert = false; lastAction = "Not now" },
        ) {
            DialogAction("Enable", { alert = false; lastAction = "Enabled" }, prominent = true)
            DialogAction("Not Now", { alert = false; lastAction = "Not now" })
        }
    }
}

/** One action of the confirmation popover: a full-width gray pill. */
@Composable
private fun DialogPill(label: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(44.dp).background(TertiaryFill, CircleShape).glassClickable(label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = color, fontSize = 17.sp) }
}

@Preview(name = "Light") @Composable private fun AlertsLight() = PreviewLight { AlertsScreen() }
@Preview(name = "Dark") @Composable private fun AlertsDark() = PreviewDark { AlertsScreen() }
