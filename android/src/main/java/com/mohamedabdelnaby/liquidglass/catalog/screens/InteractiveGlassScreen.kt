package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

@Composable
fun InteractiveGlassScreen() {
    var taps by remember { mutableIntStateOf(0) }
    GlassScreen("Interactive") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Press and hold the shapes", style = MaterialTheme.typography.titleMedium)
            Box(
                Modifier
                    .size(120.dp)
                    .glassEffect(Glass.Regular.interactive(), GlassCircle)
                    .glassClickable(label = "Tap") { taps++ },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.TouchApp, null, Modifier.size(44.dp)) }

            Text(
                "Tapped $taps times",
                Modifier
                    .glassEffect(Glass.Regular.tint(SystemColors.Purple).interactive())
                    .glassClickable(label = "Tap") { taps++ }
                    .padding(16.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("Static", Modifier.glassEffect().padding(16.dp))
                Text("Interactive", Modifier.glassEffect(Glass.Regular.interactive()).padding(16.dp))
            }
        }
    }
}

@Preview(name = "Light") @Composable private fun InteractiveLight() = PreviewLight { InteractiveGlassScreen() }
@Preview(name = "Dark") @Composable private fun InteractiveDark() = PreviewDark { InteractiveGlassScreen() }
