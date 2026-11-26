package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.LocalGlassIcons
import com.mohamedabdelnaby.liquidglass.glassIcon
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenu
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSlider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuDivider
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuHeader
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuItem
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

/** A forecast shown as one symbol in a union group. */
private data class Forecast(
    val name: String,
    val detail: String,
    val group: String,
    // The SF Symbol iOS shows (the `icons` key); [icon] and [accent] are Android's two-colour stand-in.
    val symbol: String,
    val icon: ImageVector,
    val color: Color,
    val accent: Pair<ImageVector, Color>? = null,
)

private val Forecasts = listOf(
    Forecast("Thunderstorm", "18° · 80% rain · Afternoon", "day", "cloud.bolt.rain.fill", Icons.Filled.Cloud, Color(0xFF8E8E93), Icons.Filled.Bolt to SystemColors.Yellow),
    Forecast("Sun Showers", "22° · 40% rain · Morning", "day", "sun.rain.fill", Icons.Filled.WbSunny, SystemColors.Orange, Icons.Filled.WaterDrop to SystemColors.Blue),
    Forecast("Clear Night", "14° · Starry skies · Evening", "night", "moon.stars.fill", Icons.Filled.NightsStay, SystemColors.Yellow),
    Forecast("Calm Night", "12° · Light wind · Late night", "night", "moon.fill", Icons.Filled.DarkMode, SystemColors.Indigo),
)

@Composable
fun UnionScreen() {
    var selected by remember { mutableStateOf<Forecast?>(null) }
    // A details change waits until the menu has flowed back into its symbol: the column re-centring
    // under a closing menu left its drop overlapping the moving pill, with the symbol still hidden.
    var afterMenu by remember { mutableStateOf<(() -> Unit)?>(null) }
    val alerts = remember { mutableStateListOf<String>() }
    // shortcut: 0–24 dp keeps four 64 dp symbols on a 360 dp-wide phone; derive it from the width for smaller screens.
    var gap by remember { mutableFloatStateOf(20f) }
    val context = LocalContext.current

    GlassScreen("Union") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).animateContentSize(spring(stiffness = 400f)),
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Day and night symbols are grouped into two unions. Tap a symbol for its menu.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            GlassEffectContainer(spacing = 20.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
                    Forecasts.forEach { forecast ->
                        val alerted = forecast.name in alerts
                        GlassMenu(
                            onClosed = { afterMenu?.invoke(); afterMenu = null },
                            anchor = { open ->
                                Box(
                                    Modifier
                                        .size(64.dp)
                                        // Members with the same union id draw as one capsule.
                                        .glassEffect(Glass.Regular.interactive(), GlassCircle, unionId = forecast.group)
                                        .glassClickable(label = "${forecast.name} menu", onClick = open),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ForecastIcon(forecast, 30.dp)
                                    // withAnimation: the bell fades in and out.
                                    val bell by animateFloatAsState(if (alerted) 1f else 0f, label = "bell")
                                    if (bell > 0f) {
                                        Icon(
                                            glassIcon("bell.fill", Icons.Filled.Notifications), "Alert set",
                                            tint = SystemColors.Orange,
                                            // The size of iOS's caption2 bell, with more room above it than
                                            // iOS's 6 pt so it sits inside the glass's curved rim, and
                                            // far enough to the end to clear the symbol's rays.
                                            modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { alpha = bell }.padding(top = 10.dp, end = 7.dp).size(14.dp),
                                        )
                                    }
                                }
                            },
                        ) { dismiss ->
                            GlassMenuHeader("${forecast.name} · ${forecast.detail}")
                            GlassMenuItem("Show Details", { afterMenu = { selected = forecast }; dismiss() }, icon = glassIcon("info.circle", Icons.Outlined.Info))
                            GlassMenuItem(
                                if (alerted) "Remove Alert" else "Set Alert",
                                { if (alerted) alerts.remove(forecast.name) else alerts.add(forecast.name); dismiss() },
                                icon = if (alerted) glassIcon("bell.slash", Icons.Outlined.NotificationsOff) else glassIcon("bell", Icons.Outlined.Notifications),
                            )
                            GlassMenuItem("Share", { context.shareText("${forecast.name}: ${forecast.detail}"); dismiss() }, icon = glassIcon("square.and.arrow.up", Icons.Outlined.Share))
                            if (selected == forecast) {
                                GlassMenuDivider()
                                GlassMenuItem("Hide Details", { afterMenu = { selected = null }; dismiss() }, icon = glassIcon("xmark", Icons.Filled.Close), destructive = true)
                            }
                        }
                    }
                }
            }

            // Below the container's 20 dp spacing the two unions melt together; above it they part.
            Column(
                Modifier.fillMaxWidth().glassEffect(Glass.Regular, RoundedCornerShape(24.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Padding: ${gap.toInt()} dp")
                GlassSlider(gap, { gap = it }, valueRange = 0f..24f, contentDescription = "Padding between symbols")
            }

            AnimatedContent(
                selected,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith (fadeOut() + scaleOut(targetScale = 1.04f)) },
                label = "details",
            ) { forecast ->
                if (forecast != null) {
                    // A blur-replace: content sharpens in and blurs out (blur needs API 31+).
                    val blur by transition.animateDp(label = "blur") { if (it == EnterExitState.Visible) 0.dp else 12.dp }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .blur(blur)
                            .glassEffect(Glass.Regular, RoundedCornerShape(28.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ForecastIcon(forecast, 44.dp)
                        Text(forecast.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(forecast.detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** A two-color weather symbol, standing in for SF Symbols' multicolor rendering. */
@Composable
private fun ForecastIcon(forecast: Forecast, size: Dp) {
    // The app's icon replaces the whole symbol, accent and all.
    if (forecast.symbol in LocalGlassIcons.current) return Icon(glassIcon(forecast.symbol, forecast.icon), forecast.name, tint = forecast.color, modifier = Modifier.size(size))
    Box {
        Icon(forecast.icon, forecast.name, tint = forecast.color, modifier = Modifier.size(size))
        forecast.accent?.let { (icon, color) ->
            Icon(icon, null, tint = color, modifier = Modifier.size(size * 0.5f).align(Alignment.BottomStart))
        }
    }
}

@Preview(name = "Light") @Composable private fun UnionLight() = PreviewLight { UnionScreen() }
@Preview(name = "Dark") @Composable private fun UnionDark() = PreviewDark { UnionScreen() }
