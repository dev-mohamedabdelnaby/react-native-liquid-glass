package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.glassIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

@Composable
fun ButtonsScreen() {
    var lastAction by remember { mutableStateOf("None") }
    GlassScreen("Buttons") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Last action: $lastAction", style = MaterialTheme.typography.titleMedium)
            GlassButton({ lastAction = "Glass" }) { Text("Glass") }
            GlassProminentButton({ lastAction = "Prominent" }) { Text("Glass Prominent") }
            GlassButton({ lastAction = "Clear" }, glass = Glass.Clear) { Text("Clear Glass") }
            GlassProminentButton({ lastAction = "Tinted" }, tint = SystemColors.Orange) { Text("Tinted Prominent") }
            GlassButton({ lastAction = "Large" }, size = GlassControlSize.Large) {
                Icon(glassIcon("bolt.fill", Icons.Filled.Bolt), null)
                Text("Large")
            }
            // Media controls follow time, not reading direction: they keep this order in RTL (Material).
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // lastAction shows iOS's SF Symbol names, as on the original.
                    listOf(
                        Triple(Icons.Filled.PlayArrow, "Play", "play.fill"),
                        Triple(Icons.Filled.Pause, "Pause", "pause.fill"),
                        Triple(Icons.Filled.FastForward, "Forward", "forward.fill"),
                    ).forEach { (icon, name, symbol) ->
                        GlassIconButton(glassIcon(symbol, icon), name, { lastAction = symbol }, size = 46.dp)
                    }
                }
            }
            GlassButton({}, enabled = false) { Text("Disabled") }
        }
    }
}

@Preview(name = "Light") @Composable private fun ButtonsLight() = PreviewLight { ButtonsScreen() }
@Preview(name = "Dark") @Composable private fun ButtonsDark() = PreviewDark { ButtonsScreen() }
