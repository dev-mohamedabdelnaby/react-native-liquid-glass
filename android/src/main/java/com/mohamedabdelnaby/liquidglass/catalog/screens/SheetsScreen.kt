package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassPopover
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBottomSheet
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenu
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuItem
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetsScreen() {
    var sheet by remember { mutableStateOf(false) }
    var popover by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf("None") }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    GlassScreen("Sheets & Menus") { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GlassProminentButton({ sheet = true }) { Text("Show Sheet") }
            Box {
                GlassButton({ popover = true }) { Text("Show Popover") }
                GlassPopover(popover, { popover = false }) {
                    Text("A glass popover", Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
                }
            }
            GlassMenu(anchor = { open -> GlassButton(open) { Text("Menu: $selection") } }) { dismiss ->
                listOf("A", "B", "C").forEach { option ->
                    GlassMenuItem("Option $option", { selection = option; dismiss() })
                }
            }
        }
    }

    if (sheet) {
        GlassBottomSheet(onDismissRequest = { sheet = false }, sheetState = sheetState) {
            // .presentationDetents([.medium]): half the screen, the content centered in it.
            Column(
                Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Partial-height sheets float on Liquid Glass",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                // Frosted, not liquid: a liquid lens on the sheet's own glass reads as a lens on a lens.
                GlassButton(
                    { scope.launch { sheetState.hide() }.invokeOnCompletion { sheet = false } },
                    glass = Glass.Regular.copy(liquid = false),
                ) { Text("Done") }
            }
        }
    }
}

@Preview(name = "Light") @Composable private fun SheetsLight() = PreviewLight { SheetsScreen() }
@Preview(name = "Dark") @Composable private fun SheetsDark() = PreviewDark { SheetsScreen() }
