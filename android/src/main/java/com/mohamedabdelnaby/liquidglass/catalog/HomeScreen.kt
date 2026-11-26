package com.mohamedabdelnaby.liquidglass.catalog

import com.mohamedabdelnaby.liquidglass.catalog.glass.AndroidStyle
import com.mohamedabdelnaby.liquidglass.catalog.shapes.ControlShape
import com.mohamedabdelnaby.liquidglass.catalog.shapes.DEFAULT_GLOW_INTENSITY
import com.mohamedabdelnaby.liquidglass.catalog.glass.MaterialHomeScaffold
import com.mohamedabdelnaby.liquidglass.catalog.glass.MaterialSectionHeader
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial
import com.mohamedabdelnaby.liquidglass.catalog.screens.SwitchRow
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.EdgeEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSegmented
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSlider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassListSection
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassContent
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSurfaceHost
import com.mohamedabdelnaby.liquidglass.catalog.glass.ListRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.groupedRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.ScrollEdge
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    mode: AppearanceMode,
    onModeChange: (AppearanceMode) -> Unit,
    onOpen: (GlassComponent) -> Unit,
    settings: GlassSettings = GlassSettings(),
    onSettingsChange: (GlassSettings) -> Unit = {},
    direction: String = "system",
    onDirectionChange: (String) -> Unit = {},
    // JS `androidStyle`: Glass, or Android's own Material 3 components.
    style: AndroidStyle = AndroidStyle.Glass,
    onStyleChange: (AndroidStyle) -> Unit = {},
    // JS `androidControlShape`: switches, sliders, top and bottom tabs, in both styles.
    shape: ControlShape = ControlShape.Pill,
    onShapeChange: (ControlShape) -> Unit = {},
    // JS `androidGlowIntensity`: only Glow uses it.
    glowIntensity: Float = DEFAULT_GLOW_INTENSITY,
    onGlowIntensityChange: (Float) -> Unit = {},
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val material = isMaterial
    @Composable
    fun Content(padding: PaddingValues) = LazyColumn(
            Modifier.fillMaxSize(),
            state = rememberLazyListState(),
            contentPadding = padding,
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 28.dp)) {
                    // Material: the title is the large top app bar's.
                    if (!material) Text(
                        "Liquid Glass",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    GlassSegmented(
                        options = AppearanceMode.entries.map { it.title },
                        selected = mode.ordinal,
                        onSelect = { onModeChange(AppearanceMode.entries[it]) },
                    )
                    // The iOS Home's "Layout direction" picker: JS `'system' | 'ltr' | 'rtl'`.
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GlassSegmented(
                            options = listOf("System", "LTR", "RTL"),
                            selected = Directions.indexOf(direction).coerceAtLeast(0),
                            onSelect = { onDirectionChange(Directions[it]) },
                        )
                        Footnote("RTL mirrors the whole app, including sheets and full-screen demos.", Modifier.padding(horizontal = 16.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GlassSegmented(
                            options = AndroidStyle.entries.map { it.name },
                            selected = style.ordinal,
                            onSelect = { onStyleChange(AndroidStyle.entries[it]) },
                        )
                        Footnote("Material draws every screen with Android's own components.", Modifier.padding(horizontal = 16.dp))
                        // JS `androidSegmentedCheck`: only Material segmented buttons have the check.
                        if (style == AndroidStyle.Material) Box(Modifier.padding(horizontal = 16.dp)) {
                            SwitchRow("Check on the selected segment", settings.segmentedCheck) { onSettingsChange(settings.copy(segmentedCheck = it)) }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GlassSegmented(
                            options = ControlShape.entries.map { it.name },
                            selected = shape.ordinal,
                            onSelect = { onShapeChange(ControlShape.entries[it]) },
                        )
                        Footnote("The shape of switches, sliders, top tabs and bottom tabs.", Modifier.padding(horizontal = 16.dp))
                        if (shape == ControlShape.Glow) Column(Modifier.padding(horizontal = 16.dp)) {
                            Text("Glow intensity: ${(glowIntensity * 100).roundToInt()}%")
                            GlassSlider(glowIntensity, onGlowIntensityChange, contentDescription = "Glow intensity")
                        }
                    }
                    GlassSettingsPanel(settings, onSettingsChange)
                }
            }
            item {
                if (material) MaterialSectionHeader("Components", Modifier.padding(start = 16.dp, bottom = 8.dp))
                else Text(
                    "Components",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
                )
            }
            val all = GlassComponent.entries
            itemsIndexed(all) { index, component ->
                ListRow(
                    Modifier.groupedRow(index, all.size).clickable { onOpen(component) },
                    divider = index != all.lastIndex,
                ) {
                    Icon(component.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Text(component.title, style = MaterialTheme.typography.bodyLarge)
                    // Material rows open without a chevron.
                    if (!material) {
                        Fill()
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    GlassSurfaceHost(backdrop = { PlainBackdrop(it) }) {
        if (material) {
            MaterialHomeScaffold("Liquid Glass") { bar ->
                GlassContent(Modifier.fillMaxSize()) {
                    Content(PaddingValues(start = 16.dp, end = 16.dp, top = bar.calculateTopPadding() + 8.dp, bottom = insets.calculateBottomPadding() + 24.dp))
                }
            }
            return@GlassSurfaceHost
        }
        GlassContent(Modifier.fillMaxSize()) {
            Content(
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = insets.calculateTopPadding() + 24.dp,
                    bottom = insets.calculateBottomPadding() + 24.dp,
                ),
            )
        }
        ScrollEdge(
            EdgeEffect.Soft,
            top = true,
            Modifier.fillMaxWidth().height(insets.calculateTopPadding() + 16.dp),
        )
    }
}

private val Directions = listOf("system", "ltr", "rtl")

private val DropColors = listOf(SystemColors.Blue, SystemColors.Purple, SystemColors.Pink, SystemColors.Orange, SystemColors.Green, SystemColors.Teal)

// Strong enough to read as the color, still glass.
private const val GlassTintAlpha = 0.35f

/** None (the clear drop) or one of [DropColors]; the picked one is ringed. */
@Composable
private fun ColorChoices(picked: Color?, label: String, onPick: (Color?) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        (listOf<Color?>(null) + DropColors).forEach { color ->
            val selected = picked == color
            Box(
                Modifier
                    .size(32.dp)
                    .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                    .padding(4.dp)
                    .then(if (color != null) Modifier.background(color, CircleShape) else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                    .selectable(selected, interactionSource = null, indication = null, role = Role.RadioButton) { onPick(color) }
                    .semantics { contentDescription = if (color == null) "No $label" else label },
                contentAlignment = Alignment.Center,
            ) { if (color == null) Icon(Icons.Outlined.WaterDrop, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

/** The Glass Lab controls every component in the catalog follows. */
@Composable
private fun GlassSettingsPanel(settings: GlassSettings, onChange: (GlassSettings) -> Unit) {
    // Material has no glass: only the two colors it uses.
    if (isMaterial) return GlassListSection(header = "Colors", footer = "The tint is every screen's accent; the drop color fills slider handles.") {
        ListRow { Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tint")
            ColorChoices(settings.tint?.copy(alpha = 1f), "Glass tint") { onChange(settings.copy(tint = it?.copy(alpha = GlassTintAlpha))) }
        } }
        ListRow { Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Drop color")
            ColorChoices(settings.dropColor, "Drop color") { onChange(settings.copy(dropColor = it)) }
        } }
    }
    GlassListSection(header = "Glass", footer = "Every component follows these, like the Glass Lab in LiquidPlayground.") {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            GlassSegmented(listOf("Regular", "Clear"), if (settings.clear) 1 else 0, { onChange(settings.copy(clear = it == 1)) })
            Text("Blur: ${settings.blur.value.toInt()} dp", Modifier.padding(top = 8.dp))
            GlassSlider(settings.blur.value, { onChange(settings.copy(blur = it.dp)) }, valueRange = 0f..40f, contentDescription = "Glass blur")
            Text("White: ${(settings.white * 100).toInt()}%")
            GlassSlider(settings.white, { onChange(settings.copy(white = it)) }, contentDescription = "White wash")
            Text("Black: ${(settings.black * 100).toInt()}%")
            GlassSlider(settings.black, { onChange(settings.copy(black = it)) }, contentDescription = "Black wash")
            Text("Edge light: ${(settings.edgeLight * 100).toInt()}%")
            GlassSlider(settings.edgeLight, { onChange(settings.copy(edgeLight = it)) }, contentDescription = "Edge light")
            Text("Refraction height: ${settings.refractionHeight.value.toInt()} dp")
            GlassSlider(settings.refractionHeight.value, { onChange(settings.copy(refractionHeight = it.dp)) }, valueRange = 0f..40f, contentDescription = "Refraction height")
            Text("Refraction amount: ${settings.refractionAmount.value.toInt()} dp")
            GlassSlider(settings.refractionAmount.value, { onChange(settings.copy(refractionAmount = it.dp)) }, valueRange = 0f..60f, contentDescription = "Refraction amount")
            // Android only: turn the liquid look off everywhere, or per control family.
            SwitchRow("Liquid glass (off: frosted blur)", settings.liquid) { onChange(settings.copy(liquid = it)) }
            SwitchRow("Liquid sliders", settings.sliderLiquid) { onChange(settings.copy(sliderLiquid = it)) }
            SwitchRow("Liquid toggles", settings.switchLiquid) { onChange(settings.copy(switchLiquid = it)) }
            SwitchRow("Liquid top tabs", settings.segmentedLiquid) { onChange(settings.copy(segmentedLiquid = it)) }
            SwitchRow("Liquid bottom tabs", settings.tabBarLiquid) { onChange(settings.copy(tabBarLiquid = it)) }
            SwitchRow("Bottom tabs drop", settings.tabBarDrop) { onChange(settings.copy(tabBarDrop = it)) }
            // Solid glass: the tint (or the surface color) at full strength hides what is behind.
            SwitchRow("Opaque (no see-through)", settings.opaque) { onChange(settings.copy(opaque = it)) }
            // A tint for every glass surface; items with their own tint keep it.
            Text("Glass tint")
            ColorChoices(settings.tint?.copy(alpha = 1f), "Glass tint") { onChange(settings.copy(tint = it?.copy(alpha = GlassTintAlpha))) }
            // The drop that lifts off tab bars, segmented controls, sliders and switches.
            Text("Liquid drop color")
            ColorChoices(settings.dropColor, "Drop color") { onChange(settings.copy(dropColor = it)) }
        }
    }
}

@Preview(name = "Light")
@Composable
private fun HomeLightPreview() = LiquidGlassTheme(AppearanceMode.Light) { HomeScreen(AppearanceMode.Light, {}, {}) }

@Preview(name = "Dark")
@Composable
private fun HomeDarkPreview() = LiquidGlassTheme(AppearanceMode.Dark) { HomeScreen(AppearanceMode.Dark, {}, {}) }
