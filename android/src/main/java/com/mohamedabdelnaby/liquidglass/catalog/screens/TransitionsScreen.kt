package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
                GlassEffectContainer(spacing = 24.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(72.dp).glassEffect(Glass.Regular, GlassCircle), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, "Avatar", Modifier.size(32.dp))
                        }
                        if (showing) {
                            Badges.forEach { (icon, name) ->
                                Box(
                                    Modifier.size(56.dp).glassEffect(Glass.Regular.tint(accent.copy(alpha = 0.4f)), GlassCircle, kind.transition),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AppearAnimated(enabled = kind != TransitionKind.Identity) { Icon(icon, name) }
                                }
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

@Preview(name = "Light") @Composable private fun TransitionsLight() = PreviewLight { TransitionsScreen() }
@Preview(name = "Dark") @Composable private fun TransitionsDark() = PreviewDark { TransitionsScreen() }
