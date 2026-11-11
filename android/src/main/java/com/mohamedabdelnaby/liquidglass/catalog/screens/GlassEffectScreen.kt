package com.mohamedabdelnaby.liquidglass.catalog.screens

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Tonality
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.CapsuleShape
import com.mohamedabdelnaby.liquidglass.catalog.glass.DemoColumn
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenu
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuDivider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuHeader
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuItem
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.RectShape
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors
import kotlinx.coroutines.launch

private val Palette = listOf(
    "Orange" to SystemColors.Orange, "Blue" to SystemColors.Blue, "Green" to SystemColors.Green,
    "Pink" to SystemColors.Pink, "Purple" to SystemColors.Purple, "Teal" to SystemColors.Teal,
)
private val Greetings = listOf("Hello, Liquid Glass!", "Hi there 👋", "Welcome!")

@Composable
fun GlassEffectScreen() {
    var greeting by remember { mutableStateOf(Greetings[0]) }
    var greetingClear by remember { mutableStateOf(false) }
    var clearDimmed by remember { mutableStateOf(false) }
    val chipTints = remember { mutableStateMapOf<String, Color?>("Orange" to SystemColors.Orange, "Blue" to SystemColors.Blue, "Green" to SystemColors.Green) }
    var starFilled by remember { mutableStateOf(true) }
    var cornerRadius by remember { mutableFloatStateOf(16f) }
    var rectTinted by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    GlassScreen("Glass Effect") { padding ->
        DemoColumn(padding, horizontalAlignment = Alignment.CenterHorizontally) {
            Footnote("Tap any glass item for its menu", textAlign = TextAlign.Center)

            CenteredSection("Regular (default capsule)") {
                GlassMenu(
                    anchor = { open ->
                        AnimatedContent(
                            greeting,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "greeting",
                            modifier = Modifier
                                .glassEffect((if (greetingClear) Glass.Clear else Glass.Regular).interactive())
                                .glassClickable(label = "Greeting menu", onClick = open)
                                .padding(16.dp),
                        ) { Text(it, style = MaterialTheme.typography.headlineSmall) }
                    },
                ) { dismiss ->
                    GlassMenuHeader("Variant")
                    GlassMenuItem("Regular", { greetingClear = false; dismiss() }, icon = Icons.Filled.WaterDrop, checked = !greetingClear)
                    GlassMenuItem("Clear", { greetingClear = true; dismiss() }, icon = Icons.Outlined.WaterDrop, checked = greetingClear)
                    GlassMenuDivider()
                    GlassMenuHeader("Greeting")
                    Greetings.forEach { text -> GlassMenuItem(text, { greeting = text; dismiss() }) }
                    GlassMenuDivider()
                    GlassMenuItem("Copy Text", {
                        scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Greeting", greeting))) }
                        dismiss()
                    }, icon = Icons.Filled.ContentCopy)
                }
            }

            CenteredSection("Clear variant") {
                val dim by animateFloatAsState(if (clearDimmed) 0.35f else 0f, label = "dim")
                GlassMenu(
                    anchor = { open ->
                        Row(
                            Modifier
                                .glassEffect(Glass.Clear.interactive())
                                // A dimming layer between the glass and the label keeps text legible.
                                .background(Color.Black.copy(alpha = dim), CapsuleShape)
                                .glassClickable(label = "Clear glass menu", onClick = open)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.WaterDrop, null, Modifier.size(20.dp))
                            Text("Clear glass", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                ) { dismiss ->
                    GlassMenuItem("Dimming Layer", { clearDimmed = !clearDimmed; dismiss() }, icon = Icons.Outlined.Tonality, checked = clearDimmed)
                }
            }

            CenteredSection("Tinted") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("Orange", "Blue", "Green").forEach { chip ->
                        TintedChip(chip, chipTints[chip]) { chipTints[chip] = it }
                    }
                }
            }

            CenteredSection("Custom shapes") {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassMenu(
                        anchor = { open ->
                            Box(
                                Modifier
                                    .size(70.dp)
                                    .glassEffect(Glass.Regular.interactive(), GlassCircle)
                                    .glassClickable(label = "Favorite menu", onClick = open),
                                contentAlignment = Alignment.Center,
                            ) {
                                AnimatedContent(
                                    starFilled,
                                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                                    label = "star",
                                ) { filled ->
                                    Icon(
                                        if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = if (filled) "Favorite" else "Not favorite",
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                        },
                    ) { dismiss ->
                        GlassMenuItem(
                            if (starFilled) "Unfavorite" else "Favorite",
                            { starFilled = !starFilled; dismiss() },
                            icon = if (starFilled) Icons.Outlined.StarOutline else Icons.Filled.Star,
                        )
                    }

                    val radius by animateDpAsState(cornerRadius.dp, spring(dampingRatio = 1f, stiffness = 158f), label = "radius")
                    GlassMenu(
                        anchor = { open ->
                            Text(
                                "Rounded",
                                Modifier
                                    .glassEffect(Glass.Regular.interactive(), RoundedCornerShape(radius))
                                    .glassClickable(label = "Corner radius menu", onClick = open)
                                    .padding(16.dp),
                            )
                        },
                    ) { dismiss ->
                        GlassMenuHeader("Corner Radius")
                        listOf(4f, 16f, 32f).forEach { r ->
                            GlassMenuItem("${r.toInt()} dp", { cornerRadius = r; dismiss() }, checked = cornerRadius == r)
                        }
                    }

                    val rectTint by animateColorAsState(if (rectTinted) SystemColors.Purple else Color.Transparent, label = "rectTint")
                    GlassMenu(
                        anchor = { open ->
                            Text(
                                "Rect",
                                Modifier
                                    .glassEffect(Glass.Regular.tint(rectTint.takeIf { it.alpha > 0f }).interactive(), RectShape)
                                    .glassClickable(label = "Rect menu", onClick = open)
                                    .padding(16.dp),
                            )
                        },
                    ) { dismiss ->
                        GlassMenuItem("Tint", { rectTinted = !rectTinted; dismiss() }, icon = Icons.Outlined.Palette, checked = rectTinted)
                    }
                }
            }
        }
    }
}

@Composable
private fun TintedChip(name: String, tint: Color?, onTint: (Color?) -> Unit) {
    val animated by animateColorAsState(tint ?: Color.Transparent, label = "chipTint")
    GlassMenu(
        anchor = { open ->
            Text(
                name,
                Modifier
                    .glassEffect(Glass.Regular.tint(animated.takeIf { it.alpha > 0f }).interactive())
                    .glassClickable(label = "$name tint menu", onClick = open)
                    .padding(16.dp),
            )
        },
    ) { dismiss ->
        GlassMenuHeader("Tint")
        Palette.forEach { (label, color) -> GlassMenuItem(label, { onTint(color); dismiss() }) }
        GlassMenuDivider()
        GlassMenuItem("Remove Tint", { onTint(null); dismiss() }, icon = Icons.Outlined.Cancel)
    }
}

@Composable
private fun CenteredSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Preview(name = "Light") @Composable private fun GlassEffectLight() = PreviewLight { GlassEffectScreen() }
@Preview(name = "Dark") @Composable private fun GlassEffectDark() = PreviewDark { GlassEffectScreen() }
