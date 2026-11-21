package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Footnote
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBottomBar
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.ListRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.SearchFieldContent
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.groupedRow
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidExpandableSearch
import com.mohamedabdelnaby.liquidglass.catalog.glass.MaterialSectionHeader
import com.mohamedabdelnaby.liquidglass.catalog.glass.MaterialSearchField
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial

private val Fruits = listOf(
    "Apple", "Apricot", "Banana", "Blueberry", "Cherry", "Coconut", "Date", "Fig",
    "Grape", "Kiwi", "Lemon", "Lime", "Mango", "Melon", "Orange", "Papaya",
    "Peach", "Pear", "Pineapple", "Plum", "Raspberry", "Strawberry", "Watermelon",
)
private val MoreFruits = listOf("Açaí", "Guava", "Lychee", "Nectarine", "Pomegranate", "Quince")

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    var minimize by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var reversed by remember { mutableStateOf(false) }
    val fruits = remember { mutableStateListOf(*Fruits.toTypedArray()) }
    val results = fruits.filter { it.matchesSearch(query) }.let { if (reversed) it.reversed() else it }
    val sort = @Composable {
        GlassIconButton(Icons.AutoMirrored.Filled.Sort, if (reversed) "Sort A to Z" else "Sort Z to A", { reversed = !reversed }, size = 52.dp)
    }
    val add = @Composable {
        GlassIconButton(Icons.Filled.Add, "Add fruit", {
            MoreFruits.firstOrNull { it !in fruits }?.let { fruits.add(it) }
        }, size = 52.dp)
    }

    val searching = minimize && (expanded || query.isNotEmpty())
    val material = isMaterial
    // No back button while searching, as on iOS: back closes the search first.
    BackHandler(searching) { expanded = false; query = "" }

    GlassScreen(
        title = "Search",
        // Material: search sits at the top, under the top app bar, and Sort and Add are bar actions.
        showTopBar = material || !searching,
        backdrop = { PlainBackdrop(it) },
        actions = { if (material) { sort(); add() } },
        topAccessory = if (material) { { MaterialSearchField(query, { query = it }, "Search fruits") } } else null,
        bottomBar = {
            if (!material) GlassBottomBar {
                if (minimize) {
                    // The glass button morphs into the field; Sort and Add fade out beside it.
                    LiquidExpandableSearch(
                        expanded = expanded || query.isNotEmpty(),
                        onExpandedChange = { expanded = it; if (!it) query = "" },
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = "Search fruits",
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { if (!it.hasFocus && query.isEmpty()) expanded = false },
                        height = 52.dp,
                        collapsedBias = 0.5f,
                    ) {
                        sort()
                        Fill()
                        add()
                    }
                } else {
                    GlassEffectContainer(spacing = 12.dp, modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            sort()
                            SearchFieldContent(
                                query, { query = it }, "Search fruits",
                                modifier = Modifier
                                    .weight(1f)
                                    .glassEffect(Glass.Regular.interactive())
                                    .height(52.dp)
                                    .padding(horizontal = 16.dp),
                            )
                            add()
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            // With the bar hidden, the list still starts below the status bar.
            contentPadding = PaddingValues(
                16.dp,
                maxOf(padding.calculateTopPadding(), WindowInsets.statusBars.asPaddingValues().calculateTopPadding()) + 8.dp,
                16.dp,
                padding.calculateBottomPadding() + 8.dp,
            ),
        ) {
            // iOS only: Material's search bar is always open.
            if (!material) item {
                Column(Modifier.padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ListRow(Modifier.groupedRow(0, 1), divider = false) { SwitchRow("Minimize search field", minimize) { minimize = it } }
                    Footnote("When minimized, search appears as a glass button until tapped.", Modifier.padding(horizontal = 16.dp))
                }
            }
            if (material) item { MaterialSectionHeader("Fruits", Modifier.padding(start = 16.dp, bottom = 8.dp)) }
            else item {
                // iOS 26 section headers: title case, headline weight (as GlassListSection).
                Text(
                    "Fruits",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
                )
            }
            itemsIndexed(results, key = { _, fruit -> fruit }) { index, fruit ->
                ListRow(Modifier.animateItem().groupedRow(index, results.size), divider = index != results.lastIndex) { Text(fruit) }
            }
            if (results.isEmpty()) item { Footnote("No fruits match \"$query\"", Modifier.padding(16.dp)) }
        }
    }
}

@Preview(name = "Light") @Composable private fun SearchLight() = PreviewLight { SearchScreen() }
@Preview(name = "Dark") @Composable private fun SearchDark() = PreviewDark { SearchScreen() }
