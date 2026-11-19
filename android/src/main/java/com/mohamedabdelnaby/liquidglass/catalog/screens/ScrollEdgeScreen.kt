package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.glass.styled
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.EdgeEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBottomBar
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSegmented
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.SecondarySystemBackground
import com.mohamedabdelnaby.liquidglass.catalog.glass.SystemBackground

/** Auto behaves as Soft, as the system does. */
private enum class EdgeStyle(val title: String, val edge: EdgeEffect) {
    Auto("Auto", EdgeEffect.Soft),
    Soft("Soft", EdgeEffect.Soft),
    Hard("Hard", EdgeEffect.Hard),
    Hidden("Hidden", EdgeEffect.Hidden),
}

@Composable
fun ScrollEdgeScreen() {
    var style by remember { mutableStateOf(EdgeStyle.Auto) }
    var category by remember { mutableIntStateOf(0) }

    GlassScreen(
        title = "Scroll Edge",
        topEdge = style.edge,
        backdrop = { PlainBackdrop(it, SystemBackground) },
        // safeAreaBar(edge: .top): a custom bar that insets content and carries the edge effect.
        topAccessory = {
            GlassSegmented(listOf("All", "Recent", "Shared"), category, { category = it }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        },
        bottomBar = {
            GlassBottomBar(edge = style.edge) {
                GlassEffectContainer(Modifier.fillMaxWidth(), spacing = 8.dp) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                        EdgeStyle.entries.forEach { option ->
                            StyleButton(option.title, option == style) { style = option }
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding(), 16.dp, padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items((1..40).toList()) { index ->
                Row(
                    Modifier.fillMaxWidth().background(SecondarySystemBackground, RoundedCornerShape(16.dp)).padding(16.dp),
                    // HStack's default spacing.
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(40.dp).background(Color.hsv(360f * index / 40, 0.6f, 0.9f), CircleShape))
                    Text("Item $index")
                }
            }
        }
    }
}

/**
 * `.glassProminent` when [selected], `.glass` otherwise. One glass that changes its tint in place:
 * swapping two buttons made the container's glass flow from one button into the next.
 */
@Composable
private fun StyleButton(title: String, selected: Boolean, onClick: () -> Unit) {
    val settings = LocalGlassSettings.current
    val accent = MaterialTheme.colorScheme.primary
    // withAnimation's default: ease in and out over 0.35 s.
    val progress by animateFloatAsState(if (selected) 1f else 0f, tween(350), label = "prominent")
    // The variant the plain button has, kept while tinted, so only the colour changes.
    val base = if (settings.clear) Glass.Clear else Glass.Regular
    val wash = base.styled(settings).tint ?: Color.Transparent
    GlassButton(
        { if (!selected) onClick() },
        glass = if (progress == 0f) base else base.tint(lerp(wash, accent, progress)).copy(prominent = selected),
        size = GlassControlSize.Small,
        contentColor = lerp(MaterialTheme.colorScheme.onSurface, Color.White, progress),
    ) { Text(title) }
}

@Preview(name = "Light") @Composable private fun ScrollEdgeLight() = PreviewLight { ScrollEdgeScreen() }
@Preview(name = "Dark") @Composable private fun ScrollEdgeDark() = PreviewDark { ScrollEdgeScreen() }
