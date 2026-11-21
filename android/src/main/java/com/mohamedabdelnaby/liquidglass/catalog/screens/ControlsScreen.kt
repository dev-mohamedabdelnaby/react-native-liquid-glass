package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.catalog.glass.platformTextStyle
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import com.mohamedabdelnaby.liquidglass.catalog.glass.nativeIcon
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBottomSheet
import com.mohamedabdelnaby.liquidglass.catalog.glass.TertiaryFill
import com.mohamedabdelnaby.liquidglass.catalog.glass.SystemStepper
import com.mohamedabdelnaby.liquidglass.catalog.glass.DemoColumn
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassOverlay
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassListSection
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenu
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuDivider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuHeader
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuItem
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSegmented
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSlider
import com.mohamedabdelnaby.liquidglass.catalog.glass.ListRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.panelGlass
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val Flavors = listOf("Vanilla", "Chocolate", "Strawberry")
private val Accents = listOf(SystemColors.Blue, SystemColors.Purple, SystemColors.Pink, SystemColors.Orange, SystemColors.Green, SystemColors.Teal)
private val Usd = NumberFormat.getCurrencyInstance(Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlsScreen() {
    var volume by remember { mutableFloatStateOf(0.5f) }
    var notifications by remember { mutableStateOf(true) }
    var segment by remember { mutableIntStateOf(0) }
    var cups by remember { mutableIntStateOf(3) }
    var flavor by remember { mutableStateOf(Flavors[0]) }
    var accent by remember { mutableStateOf(SystemColors.Blue) }
    var progress by remember { mutableFloatStateOf(0.6f) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val now = LocalTime.now()
    val timeState = rememberTimePickerState(now.hour, now.minute)
    var showColors by remember { mutableStateOf(false) }

    GlassScreen("Controls", backdrop = { PlainBackdrop(it) }) { padding ->
        DemoColumn(padding, spacing = 24.dp) {
            GlassListSection(glass = false, header = "Slider") {
                ListRow(divider = false) {
                    Icon(Icons.AutoMirrored.Filled.VolumeDown, null)
                    GlassSlider(volume, { volume = it }, Modifier.weight(1f), contentDescription = "Volume")
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null)
                }
            }
            GlassListSection(glass = false, header = "Toggle") {
                ListRow(divider = false) { SwitchRow("Notifications", notifications) { notifications = it } }
            }
            GlassListSection(glass = false, header = "Segmented Picker") {
                ListRow(divider = false) { GlassSegmented(listOf("Day", "Week", "Month"), segment, { segment = it }) }
            }
            // Material: the quick pick is a plain stepper, with no presets menu to mention.
            GlassListSection(glass = false, header = "Stepper", footer = if (isMaterial) null else "Tap the number in the glass stepper for a menu of presets.") {
                ListRow {
                    Text("Cups: $cups")
                    Fill()
                    SystemStepper(cups, { cups = it }, 0..10)
                }
                ListRow {
                    Text("Order total")
                    Fill()
                    AnimatedContent(
                        cups,
                        transitionSpec = {
                            val up = targetState > initialState
                            (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith (slideOutVertically { if (up) -it else it } + fadeOut())
                        },
                        label = "total",
                    ) { Text(Usd.format(it * 3.5), style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")) }
                }
                ListRow(divider = false) {
                    Text("Quick pick")
                    Fill()
                    if (isMaterial) SystemStepper(cups, { cups = it }, 0..10) else GlassStepper(cups, 0..10) { cups = it }
                }
            }
            GlassListSection(glass = false, header = "Menu Picker") {
                ListRow(divider = false) {
                    Text("Flavor")
                    Fill()
                    GlassMenu(anchor = { open ->
                        Row(Modifier.glassClickable(label = "Flavor: $flavor", onClick = open), verticalAlignment = Alignment.CenterVertically) {
                            Text(flavor, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(nativeIcon(Icons.Filled.UnfoldMore), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }) { dismiss ->
                        Flavors.forEach { option -> GlassMenuItem(option, { flavor = option; dismiss() }, checked = option == flavor) }
                    }
                }
            }
            GlassListSection(glass = false, header = "Date & Color Pickers") {
                ListRow {
                    Text("Date")
                    Fill()
                    val date = dateState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    } ?: "None"
                    CompactPicker(date) { showDate = true }
                }
                ListRow {
                    Text("Time")
                    Fill()
                    val time = LocalTime.of(timeState.hour, timeState.minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    CompactPicker(time) { showTime = true }
                }
                ListRow(divider = false) {
                    Text("Accent")
                    Fill()
                    ColorWell(accent) { showColors = true }
                }
            }
            GlassListSection(glass = false, header = "Progress") {
                // A ProgressView row (4 pt bar) and a Slider row, as iOS lays them out.
                ListRow {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = accent,
                        trackColor = TertiaryFill,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }
                ListRow(divider = false) { GlassSlider(progress, { progress = it }, tint = accent, contentDescription = "Progress") }
            }
        }
    }

    // ColorPicker: the well opens the system's color sheet; here, the app's accent swatches.
    if (showColors) {
        GlassBottomSheet(onDismissRequest = { showColors = false }) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Colors", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Accents.forEach { color ->
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .then(if (color == accent) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                .selectable(color == accent, role = Role.RadioButton) { accent = color; showColors = false },
                        )
                    }
                }
            }
        }
    }
    if (showDate) {
        GlassOverlay { DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = { GlassProminentButton({ showDate = false }, Modifier.padding(8.dp)) { Text("Done") } },
        ) { DatePicker(dateState) } }
    }
    if (showTime) {
        GlassOverlay { BasicAlertDialog({ showTime = false }) {
            Column(
                Modifier.glassEffect(panelGlass(), RoundedCornerShape(32.dp)).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TimePicker(timeState)
                GlassProminentButton({ showTime = false }) { Text("Done") }
            }
        } }
    }
}

/** A custom "− | N | +" stepper on glass; the number opens a menu of presets. */
@Composable
private fun GlassStepper(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(
        Modifier.glassEffect(Glass.Regular.interactive()).padding(horizontal = 4.dp).height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepButton(Icons.Filled.Remove, "Decrease", value > range.first) { onChange(value - 1) }
        VerticalDivider(Modifier.height(20.dp))
        GlassMenu(anchor = { open ->
            Box(Modifier.widthIn(min = 36.dp).glassClickable(label = "$value cups, presets", onClick = open), contentAlignment = Alignment.Center) {
                AnimatedContent(value, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "stepper") {
                    // .headline.monospacedDigit()
                    Text("$it", style = LocalTextStyle.current.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"))
                }
            }
        }) { dismiss ->
            GlassMenuHeader("Presets")
            listOf(1, 2, 5, 10).forEach { preset -> GlassMenuItem("$preset cups", { onChange(preset); dismiss() }) }
            GlassMenuDivider()
            GlassMenuItem("Reset", { onChange(range.first); dismiss() }, icon = Icons.Filled.RestartAlt, destructive = true)
        }
        VerticalDivider(Modifier.height(20.dp))
        StepButton(Icons.Filled.Add, "Increase", value < range.last) { onChange(value + 1) }
    }
}

@Composable
private fun StepButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp, 34.dp).alpha(if (enabled) 1f else 0.35f).glassClickable(enabled, label, onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray) }
}

/** A compact DatePicker: the value in label color on a gray capsule. */
@Composable
private fun CompactPicker(value: String, onClick: () -> Unit) {
    Text(
        value,
        Modifier.background(TertiaryFill, CircleShape).glassClickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp),
        style = platformTextStyle(17.sp, MaterialTheme.typography.bodyLarge),
    )
}

/** ColorPicker's well: the color inside a rainbow ring. */
@Composable
private fun ColorWell(color: Color, onClick: () -> Unit) {
    val rainbow = Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red))
    Box(
        Modifier.size(28.dp).border(3.dp, rainbow, CircleShape).padding(5.dp).background(color, CircleShape)
            .glassClickable(label = "Accent color", onClick = onClick),
    )
}

@Preview(name = "Light") @Composable private fun ControlsLight() = PreviewLight { ControlsScreen() }
@Preview(name = "Dark") @Composable private fun ControlsDark() = PreviewDark { ControlsScreen() }
