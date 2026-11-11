package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.outlined.AutoFixNormal
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import com.mohamedabdelnaby.liquidglass.catalog.glass.AppearAnimated
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect

private val Tools = listOf(
    Icons.Filled.Edit to "Pencil",
    Icons.Outlined.AutoFixNormal to "Eraser",
    Icons.Filled.Brush to "Paintbrush",
    Icons.Filled.Gesture to "Lasso",
)

@Composable
fun MorphingScreen() {
    var expanded by remember { mutableStateOf(false) }
    GlassScreen("Morphing") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            // As iOS's centred VStack: the toggle rests at the centre and slides down on the spring as
            // the tools grow out of it. The frame keeps its size, so the container's origin never jumps.
            val shift by animateDpAsState(if (expanded) 0.dp else -(60.dp + 16.dp) * Tools.size / 2, spring(0.75f, 380f), label = "stack")
            Box(Modifier.offset { IntOffset(0, shift.roundToPx()) }.height(60.dp * 5 + 16.dp * 4), contentAlignment = Alignment.BottomCenter) {
                GlassEffectContainer(spacing = 20.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (expanded) {
                            Tools.forEach { (icon, name) ->
                                Box(
                                    Modifier.size(60.dp).glassEffect(Glass.Regular.interactive(), GlassCircle).glassClickable(label = name) {},
                                    contentAlignment = Alignment.Center,
                                ) { AppearAnimated { Icon(icon, name, Modifier.size(22.dp)) } }
                            }
                        }
                        Box(
                            Modifier
                                .size(60.dp)
                                .glassEffect(Glass.Regular.interactive(), GlassCircle)
                                .glassClickable(label = if (expanded) "Close tools" else "Show tools") { expanded = !expanded },
                            contentAlignment = Alignment.Center,
                        ) {
                            AnimatedContent(
                                expanded,
                                transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                                label = "toggle",
                            ) { open -> Icon(if (open) Icons.Filled.Close else Icons.Filled.Add, null) }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            // Default content colour (iOS's primary label), unlike Footnote's secondary grey.
            Text("Tap the button to morph the tools out of it", Modifier.glassEffect().padding(16.dp), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(name = "Light") @Composable private fun MorphingLight() = PreviewLight { MorphingScreen() }
@Preview(name = "Dark") @Composable private fun MorphingDark() = PreviewDark { MorphingScreen() }
