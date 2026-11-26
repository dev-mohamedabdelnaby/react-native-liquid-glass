package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.glassIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedabdelnaby.liquidglass.catalog.glass.CapsuleShape
import com.mohamedabdelnaby.liquidglass.catalog.glass.DemoColumn
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSlider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSegmented
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.glass.RectShape
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.rememberLayerBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

@Composable
fun TextScreen() {
    var username by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var letterClear by remember { mutableStateOf(false) }
    var letterBlur by remember { mutableFloatStateOf(8f) }
    var letterRefraction by remember { mutableFloatStateOf(24f) }
    val letterGlass = (if (letterClear) Glass.Clear else Glass.Regular)
        .copy(blur = letterBlur.dp, refractionAmount = letterRefraction.dp)

    GlassScreen("Text") { padding ->
        DemoColumn(padding, spacing = 32.dp, horizontalAlignment = Alignment.CenterHorizontally) {
            TextSection("Glass letterforms") {
                val fontFamily = LocalGlassSettings.current.fontFamily
                GlassLettering("Liquid", TextStyle(fontSize = 88.sp, fontWeight = FontWeight.Black, fontFamily = fontFamily ?: FontFamily.SansSerif), letterGlass)
                GlassLettering("GLASS", TextStyle(fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, fontFamily = fontFamily), letterGlass)
                val panel = rememberLayerBackdrop()
                Column(
                    Modifier.fillMaxWidth().glassEffect(Glass.Regular, RoundedCornerShape(24.dp), exportTo = panel).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // The tabs' drop refracts the panel under it, not the photo behind the panel.
                    CompositionLocalProvider(LocalGlassBackdrop provides panel) {
                        GlassSegmented(listOf("Regular", "Clear"), if (letterClear) 1 else 0, { letterClear = it == 1 })
                    }
                    Text("Blur: ${letterBlur.toInt()} dp", Modifier.padding(top = 8.dp))
                    GlassSlider(letterBlur, { letterBlur = it }, valueRange = 0f..40f, contentDescription = "Letter blur")
                    Text("Refraction: ${letterRefraction.toInt()} dp")
                    GlassSlider(letterRefraction, { letterRefraction = it }, valueRange = 0f..60f, contentDescription = "Letter refraction")
                }
            }
            TextSection("Titles") {
                Text(
                    "Large Title on Glass",
                    Modifier.glassEffect().padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Tinted headline",
                    Modifier.glassEffect(Glass.Regular.tint(SystemColors.Purple)).padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextSection("Paragraph card") {
                Column(
                    Modifier.fillMaxWidth().glassEffect(Glass.Regular, RoundedCornerShape(24.dp)).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Liquid Glass", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    // Hierarchical text styles stay legible on glass.
                    Text(
                        "A dynamic material that blurs content behind it and reflects surrounding color and light.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("Updated just now", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
            TextSection("Tags") {
                GlassEffectContainer(spacing = 8.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SwiftUI", "iOS 26", "Design").forEach { tag ->
                            Text(
                                tag,
                                Modifier.glassEffect(Glass.Regular.interactive()).padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
            TextSection("Clear glass with dimming") {
                // Clear glass needs a dimming layer underneath to keep text legible.
                Row(
                    Modifier.glassEffect(Glass.Clear).background(Color.Black.copy(alpha = 0.35f), CapsuleShape).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(glassIcon("eye", Icons.Outlined.Visibility), null, tint = Color.White)
                    Text("Readable over rich backgrounds", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            TextSection("Text fields") {
                GlassField(username, { username = it }, "Username", CapsuleShape, Modifier.fillMaxWidth(), maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassField(message, { message = it }, "Message", RoundedCornerShape(24.dp), Modifier.weight(1f), maxLines = 4)
                    GlassProminentButton(
                        { message = "" },
                        shape = GlassCircle,
                        size = GlassControlSize.Large,
                        enabled = message.isNotEmpty(),
                    ) { Icon(glassIcon("arrow.up", Icons.Filled.ArrowUpward), "Send") }
                }
            }
        }
    }
}

/**
 * The letters themselves are glass: inside an offscreen layer the glass draws first, then the
 * text composites over it with DstIn, keeping glass only where the letters are. The mask is the
 * real Text, so it also sizes the lettering and stays readable to accessibility services.
 */
@Composable
private fun GlassLettering(text: String, style: TextStyle, glass: Glass) {
    Box {
        Text(text, Modifier.offset(y = 3.dp).blur(6.dp), style = style, color = Color.Black.copy(alpha = 0.15f))
        Box(Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
            Box(Modifier.matchParentSize().glassEffect(glass, RectShape))
            Text(text, Modifier.graphicsLayer { blendMode = BlendMode.DstIn }, style = style, color = Color.Black)
        }
    }
}

@Composable
private fun GlassField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    shape: RoundedCornerShape,
    modifier: Modifier,
    maxLines: Int,
) {
    Box(modifier.glassEffect(Glass.Regular.interactive(), shape).padding(horizontal = 20.dp, vertical = 14.dp)) {
        if (value.isEmpty()) Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
        BasicTextField(
            value,
            onValueChange,
            singleLine = maxLines == 1,
            minLines = 1,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TextSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Preview(name = "Light") @Composable private fun TextLight() = PreviewLight { TextScreen() }
@Preview(name = "Dark") @Composable private fun TextDark() = PreviewDark { TextScreen() }
