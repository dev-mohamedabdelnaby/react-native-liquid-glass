package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import com.mohamedabdelnaby.liquidglass.catalog.glass.CapsuleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.runtime.LaunchedEffect
import kotlin.math.roundToInt
import android.os.Build
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.layout.layout
import androidx.compose.runtime.Stable
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.blur
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.DemoColumn
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSlider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassTransition
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial
import androidx.compose.material3.FloatingActionButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.RevealPill
import com.mohamedabdelnaby.liquidglass.catalog.glass.growFrom
import com.mohamedabdelnaby.liquidglass.catalog.glass.rememberReveal
import com.mohamedabdelnaby.liquidglass.catalog.glass.revealContent
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.layerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun RecipesScreen() {
    GlassScreen(
        "Recipes",
        // Floats above the scroll view, pinned to the bottom-right corner, as on iOS.
        overlay = { FloatingActionMenu(Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp)) },
    ) { padding ->
        DemoColumn(padding, spacing = 40.dp, horizontalAlignment = Alignment.CenterHorizontally) {
            Recipe("Sliding selector") { SlidingSelector() }
            Recipe("Map controls") { MapControls() }
            Recipe("Mini player") { MiniPlayer() }
            Recipe("Floating action menu") {
                Footnote("The + button floats at the bottom-right corner. Its actions open upward without moving it.", textAlign = TextAlign.Center)
            }
            // With the 40 spacing and 16 padding, matches iOS's 16 + 80 so the last content scrolls above the button.
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Recipe(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun SlidingSelector() {
    val options = listOf("Day", "Week", "Month", "Year")
    var selected by remember { mutableIntStateOf(0) }
    // SwiftUI's .smooth (about 0.5 s, no bounce), which the iOS selector slides its indicator on.
    val position by animateFloatAsState(selected.toFloat(), spring(dampingRatio = 1f, stiffness = 160f), label = "indicator")
    val indicator = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    // As on iOS: one glass capsule, 4 dp inset, with a tinted capsule sliding behind the choice.
    Row(
        Modifier
            .fillMaxWidth()
            .glassEffect(Glass.Regular.interactive())
            .padding(4.dp)
            .drawBehind {
                val segment = size.width / options.size
                // Drawn, so placed by hand: the first choice sits at the start edge.
                val x = if (layoutDirection == LayoutDirection.Rtl) size.width - segment * (position + 1f) else segment * position
                drawRoundRect(indicator, Offset(x, 0f), Size(segment, size.height), CornerRadius(size.height / 2f))
            },
    ) {
        options.forEachIndexed { index, option ->
            Text(
                option,
                Modifier
                    .weight(1f)
                    .selectable(index == selected, interactionSource = null, indication = null, role = Role.Tab) { selected = index }
                    .padding(vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MapControls() {
    var tracking by remember { mutableStateOf(false) }
    // The controls float over the map, so the map is their own glass source.
    val mapLayer = rememberLayerBackdrop()
    CompositionLocalProvider(LocalGlassBackdrop provides mapLayer) {
        Box(Modifier.fillMaxWidth().height(220.dp)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .layerBackdrop(mapLayer)
                    .background(
                        Brush.linearGradient(listOf(SystemColors.Green.copy(alpha = 0.6f), SystemColors.Teal.copy(alpha = 0.6f), SystemColors.Blue.copy(alpha = 0.5f))),
                        RoundedCornerShape(28.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Map, null, Modifier.size(80.dp), tint = Color.White.copy(alpha = 0.4f)) }

            // iOS unions the two buttons into one glass capsule, which swells as one when pressed.
            Column(
                Modifier.align(Alignment.TopEnd).padding(12.dp).glassEffect(Glass.Regular.interactive(), RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MapButton(Icons.Filled.Layers, "Layers") {}
                HorizontalDivider(Modifier.width(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
                MapButton(if (tracking) Icons.Filled.MyLocation else Icons.Outlined.LocationSearching, if (tracking) "Stop tracking" else "Locate") {
                    tracking = !tracking
                }
            }
            GlassIconButton(Icons.Outlined.TravelExplore, "Look Around", {}, Modifier.align(Alignment.BottomStart).padding(12.dp), size = 48.dp, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun MapButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .glassClickable(label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(icon, transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) }, label = label) { Icon(it, null) }
    }
}

@Composable
private fun MiniPlayer() {
    var playing by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.35f) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Under the 12 dp gap, so player and controls only bridge while they move, as on iOS.
        GlassEffectContainer(Modifier.fillMaxWidth(), spacing = 8.dp) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .glassEffect(Glass.Regular.interactive(), RoundedCornerShape(24.dp))
                        .glassClickable(label = if (expanded) "Collapse player" else "Expand player") { expanded = !expanded }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(44.dp).background(Brush.verticalGradient(listOf(SystemColors.Pink, SystemColors.Orange)), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.MusicNote, null, tint = Color.White) }
                    Column {
                        Text("Glass Animals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Heat Waves", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Fill()
                    Box(Modifier.size(44.dp).glassClickable(label = if (playing) "Pause" else "Play") { playing = !playing }, contentAlignment = Alignment.Center) {
                        AnimatedContent(playing, transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) }, label = "play") {
                            Icon(if (it) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, Modifier.size(32.dp))
                        }
                    }
                }
                // As on iOS: the controls' glass grows out from under the player (a narrower tab tucked
                // behind it) into place, riding one spring with the controls; closing runs it back.
                val reveal = rememberReveal(expanded)
                if (reveal.shown) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            // The page sees the gap and the height grow with the spring, so what follows
                            // slides down, and nothing jumps when the panel is finally removed.
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                val gap = 12.dp.roundToPx()
                                layout(placeable.width, ((gap + placeable.height) * reveal.progress.value.coerceIn(0f, 1f)).roundToInt()) {
                                    placeable.place(0, gap)
                                }
                            },
                    ) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .growFrom(reveal) { width, _ ->
                                    val gap = 12.dp.roundToPx()
                                    // Wholly inside the player, so it merges without a bulge and leaves without a pop.
                                    IntRect(width * 15 / 100, -gap - 40.dp.roundToPx(), width * 85 / 100, -gap - 8.dp.roundToPx())
                                }
                                .glassEffect(Glass.Regular, RoundedCornerShape(24.dp), GlassTransition.Identity),
                        )
                        Column(
                            Modifier.fillMaxWidth().revealContent(reveal).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            GlassSlider(progress, { progress = it }, contentDescription = "Playback position")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally)) {
                                PlayerIcon(Icons.Filled.FastRewind, "Back") { progress = (progress - 0.1f).coerceAtLeast(0f) }
                                PlayerIcon(Icons.Filled.Shuffle, "Shuffle") { progress = 0f }
                                PlayerIcon(Icons.Filled.FastForward, "Forward") { progress = (progress + 0.1f).coerceAtMost(1f) }
                            }
                        }
                    }
                }
            }
        }
        Footnote("Tap the player to expand it", textAlign = TextAlign.Center)
    }
}

@Composable
private fun PlayerIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).glassClickable(label = label, onClick = onClick), contentAlignment = Alignment.Center) { Icon(icon, null) }
}

@Composable
private fun FloatingActionMenu(modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (open) 45f else 0f, label = "fab")
    val actions = listOf(
        Triple("Photo", Icons.Filled.CameraAlt, SystemColors.Blue),
        Triple("Note", Icons.AutoMirrored.Filled.StickyNote2, SystemColors.Yellow),
        Triple("Reminder", Icons.Filled.Checklist, SystemColors.Green),
    )
    // A fixed frame, so the container's origin never moves as the column grows and shrinks;
    // otherwise its glass would drift with it. It has no touch handling, so taps pass through.
    // Under the 12 dp gaps, so the pills stay apart at rest and only melt into the + button as they move.
    GlassEffectContainer(modifier.size(220.dp, 260.dp), spacing = 8.dp) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // As on iOS: each pill's glass grows out of the + button's circle into the pill, its
                // label riding along on the same spring; closing runs the same path back into the button.
                val reveal = rememberReveal(open)
                if (reveal.shown) {
                    actions.forEachIndexed { index, (title, icon, color) ->
                        RevealPill(title, icon, Glass.Regular.tint(color.copy(alpha = 0.5f)), reveal, below = actions.size - 1 - index) { open = false }
                    }
                }
                // Material: Android's floating action button.
                if (isMaterial) FloatingActionButton({ open = !open }) {
                    Icon(Icons.Filled.Add, if (open) "Close menu" else "Open menu", Modifier.rotate(rotation))
                } else Box(
                    Modifier
                        .size(60.dp)
                        .glassEffect(Glass.Regular.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)).copy(prominent = true).interactive(), GlassCircle)
                        .glassClickable(label = if (open) "Close menu" else "Open menu") { open = !open },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Add, null, Modifier.size(30.dp).rotate(rotation), tint = Color.White) }
            }
        }
    }
}

@Preview(name = "Light") @Composable private fun RecipesLight() = PreviewLight { RecipesScreen() }
@Preview(name = "Dark") @Composable private fun RecipesDark() = PreviewDark { RecipesScreen() }
