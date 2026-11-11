package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.outlined.AutoFixNormal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSlider
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect

@Composable
fun ContainerScreen() {
    var spacing by remember { mutableFloatStateOf(40f) }
    var pull by remember { mutableFloatStateOf(0f) }
    GlassScreen("Container") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            GlassEffectContainer(spacing = spacing.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    ToolShape(Icons.Filled.Draw, "Scribble")
                    // Moving the shape closer makes the two glass shapes melt together.
                    ToolShape(Icons.Outlined.AutoFixNormal, "Eraser", Modifier.offset { IntOffset(-pull.dp.roundToPx(), 0) })
                }
            }
            Spacer(Modifier.weight(1f))
            Column(
                Modifier.fillMaxWidth().glassEffect(Glass.Regular, RoundedCornerShape(24.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Container spacing: ${spacing.toInt()}")
                GlassSlider(spacing, { spacing = it }, valueRange = 0f..80f, contentDescription = "Container spacing")
                Text("Pull together: ${pull.toInt()}")
                GlassSlider(pull, { pull = it }, valueRange = 0f..80f, contentDescription = "Pull together")
            }
        }
    }
}

@Composable
private fun ToolShape(icon: ImageVector, name: String, modifier: Modifier = Modifier) {
    Box(modifier.size(80.dp).glassEffect(Glass.Regular, GlassCircle), contentAlignment = Alignment.Center) {
        Icon(icon, name, Modifier.size(36.dp))
    }
}

@Preview(name = "Light") @Composable private fun ContainerLight() = PreviewLight { ContainerScreen() }
@Preview(name = "Dark") @Composable private fun ContainerDark() = PreviewDark { ContainerScreen() }
