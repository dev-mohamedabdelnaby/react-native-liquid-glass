package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.AppearAnimated
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSegmented
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassTransition
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect

private enum class TransitionKind(val title: String, val transition: GlassTransition, val description: String) {
    Matched("Matched", GlassTransition.MatchedGeometry, "Badges grow out of the nearby avatar shape, since they're within the container's spacing."),
    Materialize("Materialize", GlassTransition.Materialize, "Badges fade and materialize in place — best for shapes far from each other."),
    Identity("Identity", GlassTransition.Identity, "No glass animation: badges appear and disappear instantly."),
}

private val Badges = listOf(Icons.Filled.Star to "Star", Icons.Filled.Favorite to "Heart", Icons.Filled.Bolt to "Bolt")

@Composable
fun TransitionsScreen() {
    var kind by remember { mutableStateOf(TransitionKind.Matched) }
    var showing by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    GlassScreen("Transitions") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GlassSegmented(TransitionKind.entries.map { it.title }, kind.ordinal, { kind = TransitionKind.entries[it] })
            Spacer(Modifier.weight(1f))
            Box(Modifier.height(72.dp), contentAlignment = Alignment.Center) {
                // The container fills the width and the row centres inside it, so when the badges come
                // and go the avatar's glass slides to its new place (withAnimation) instead of jumping
                // with a re-centred container.
                GlassEffectContainer(Modifier.fillMaxWidth(), spacing = 24.dp) {
                    // HStack(spacing: 24) whose avatar re-centres at once when the badges hide (its
                    // glass slides there), while hiding badges keep their places until their glass
                    // has flowed back into it, their symbols fading on the way.
                    Layout(
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Box(Modifier.size(72.dp).glassEffect(Glass.Regular, GlassCircle), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, "Avatar", Modifier.size(32.dp))
                            }
                            Badges.forEach { (icon, name) -> Badge(icon, name, showing, kind, accent) }
                        },
                    ) { measurables, constraints ->
                        val items = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
                        val gap = 24.dp.roundToPx()
                        val avatar = items.first()
                        val rowWidth = items.sumOf { it.width } + gap * (items.size - 1)
                        val height = items.maxOf { it.height }
                        layout(constraints.maxWidth, height) {
                            val rowStart = (constraints.maxWidth - rowWidth) / 2
                            val avatarX = if (showing) rowStart else (constraints.maxWidth - avatar.width) / 2
                            avatar.place(avatarX, (height - avatar.height) / 2)
                            var x = rowStart + avatar.width + gap
                            items.drop(1).forEach { badge ->
                                badge.place(x, (height - badge.height) / 2)
                                x += badge.width + gap
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                kind.description,
                Modifier.glassEffect(Glass.Regular, RoundedCornerShape(20.dp)).padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            GlassProminentButton({ showing = !showing }, size = GlassControlSize.Large) {
                Text(if (showing) "Hide Badges" else "Show Badges")
            }
        }
    }
}

/** A badge kept while it hides, its symbol fading on its glass as it flows back. */
@Composable
private fun Badge(icon: ImageVector, name: String, showing: Boolean, kind: TransitionKind, accent: Color) {
    AnimatedVisibility(
        showing,
        enter = EnterTransition.None,
        // Identity glass has no animation, so neither has its symbol.
        exit = if (kind == TransitionKind.Identity) ExitTransition.None else fadeOut(tween(250)),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .glassEffect(
                    Glass.Regular.tint(accent.copy(alpha = 0.4f)), GlassCircle, kind.transition,
                    present = transition.targetState == EnterExitState.Visible,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AppearAnimated(enabled = kind != TransitionKind.Identity) { Icon(icon, name) }
        }
    }
}

@Preview(name = "Light") @Composable private fun TransitionsLight() = PreviewLight { TransitionsScreen() }
@Preview(name = "Dark") @Composable private fun TransitionsDark() = PreviewDark { TransitionsScreen() }
