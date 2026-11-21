package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.catalog.glass.platformCopy
import com.mohamedabdelnaby.liquidglass.catalog.glass.platformTextStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.IosShare
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.BarIcon
import com.mohamedabdelnaby.liquidglass.catalog.glass.EdgeEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSegmented
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.SecondarySystemBackground
import com.mohamedabdelnaby.liquidglass.catalog.glass.SystemBackground
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.Image
import androidx.compose.ui.unit.sp
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

private enum class TopEdge(val title: String, val edge: EdgeEffect, val description: String) {
    StatusBar("Status Bar", EdgeEffect.StatusBar, "Only the status bar area gets a fade, so the clock and battery stay legible."),
    Soft("Soft", EdgeEffect.Soft, "A soft scroll edge effect fades content under the whole top bar."),
    None("None", EdgeEffect.Hidden, "No edge effect at all: content scrolls fully visible under the bar buttons."),
}

@Composable
fun FloatingHeaderScreen() {
    var topEdge by remember { mutableStateOf(TopEdge.StatusBar) }
    var favorite by remember { mutableStateOf(false) }
    val context = LocalContext.current

    GlassScreen(
        title = "",
        topEdge = topEdge.edge,
        backdrop = { PlainBackdrop(it, SystemBackground) },
        actions = {
            GlassIconButton(if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, if (favorite) "Unfavorite" else "Favorite", { favorite = !favorite })
            Spacer(Modifier.width(8.dp))
            Row(Modifier.glassEffect(Glass.Regular.interactive())) {
                BarIcon(Icons.Outlined.IosShare, "Share", { context.shareText("Floating Header") })
                BarIcon(Icons.Filled.MoreHoriz, "More", {})
            }
        },
    ) { padding ->
        // Content runs under the status bar and the glass buttons: there is no bar background.
        // Android's stretch overscroll stretches the header itself, so nothing shows above it.
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Header() }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GlassSegmented(TopEdge.entries.map { it.title }, topEdge.ordinal, { topEdge = TopEdge.entries[it] })
                    Footnote(topEdge.description)
                }
            }
            items(30) { index ->
                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .background(SecondarySystemBackground, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // .title2 symbol, .body text.
                    Icon(Icons.Outlined.Image, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Row ${index + 1}", style = platformTextStyle(17.sp, MaterialTheme.typography.bodyLarge))
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(Brush.linearGradient(listOf(SystemColors.Indigo, SystemColors.Purple, SystemColors.Pink, SystemColors.Orange))),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // .largeTitle.bold() and .subheadline.
            Text("Floating Header", style = platformTextStyle(34.sp, MaterialTheme.typography.headlineLarge, 41.sp), fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                platformCopy("No bar background — just glass buttons", "A see-through top app bar over the header"),
                style = platformTextStyle(15.sp, MaterialTheme.typography.bodyMedium, 20.sp),
                color = Color.White,
            )
        }
    }
}

@Preview(name = "Light") @Composable private fun FloatingHeaderLight() = PreviewLight { FloatingHeaderScreen() }
@Preview(name = "Dark") @Composable private fun FloatingHeaderDark() = PreviewDark { FloatingHeaderScreen() }
