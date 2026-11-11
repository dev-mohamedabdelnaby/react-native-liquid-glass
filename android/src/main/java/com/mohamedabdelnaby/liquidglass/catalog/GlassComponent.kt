package com.mohamedabdelnaby.liquidglass.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CallToAction
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.JoinFull
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WebAsset
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mohamedabdelnaby.liquidglass.catalog.screens.AlertsScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.ButtonsScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.ContainerScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.ControlsScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.EditSearchScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.FloatingHeaderScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.GlassEffectScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.GlassViewScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.InteractiveGlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.MorphingScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.RecipesScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.ScrollEdgeScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.SearchScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.SheetsScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.SplitViewScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.TabBarScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.TextScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.ToolbarScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.TransitionsScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.UnionScreen
import com.mohamedabdelnaby.liquidglass.catalog.screens.ZoomTransitionsScreen

/** Each glass component showcased in the catalog, with its own screen. */
enum class GlassComponent(val title: String, val icon: ImageVector, val screen: @Composable () -> Unit) {
    GlassEffect("Glass Effect", Icons.Filled.WaterDrop, { GlassEffectScreen() }),
    GlassView("Glass View", Icons.Filled.FilterNone, { GlassViewScreen() }),
    Interactive("Interactive Glass", Icons.Filled.TouchApp, { InteractiveGlassScreen() }),
    Buttons("Buttons", Icons.Filled.SmartButton, { ButtonsScreen() }),
    Container("Glass Container", Icons.Filled.Layers, { ContainerScreen() }),
    Morphing("Morphing", Icons.Filled.AutoFixHigh, { MorphingScreen() }),
    Union("Union", Icons.Filled.JoinFull, { UnionScreen() }),
    Toolbar("Toolbar", Icons.Filled.WebAsset, { ToolbarScreen() }),
    FloatingHeader("Floating Header", Icons.Filled.Panorama, { FloatingHeaderScreen() }),
    TabBar("Tab Bar", Icons.Filled.CallToAction, { TabBarScreen() }),
    Sheets("Sheets & Menus", Icons.Filled.ViewAgenda, { SheetsScreen() }),
    Controls("Controls", Icons.Filled.Tune, { ControlsScreen() }),
    Text("Text", Icons.Filled.TextFields, { TextScreen() }),
    Transitions("Glass Transitions", Icons.Filled.AutoAwesome, { TransitionsScreen() }),
    Search("Search", Icons.Filled.Search, { SearchScreen() }),
    EditSearch("Edit & Search Buttons", Icons.Filled.EditNote, { EditSearchScreen() }),
    ScrollEdge("Scroll Edge & Bars", Icons.Filled.VerticalAlignTop, { ScrollEdgeScreen() }),
    SplitView("Sidebar & Inspector", Icons.AutoMirrored.Filled.ViewSidebar, { SplitViewScreen() }),
    ZoomTransitions("Zoom Transitions", Icons.Filled.ZoomOutMap, { ZoomTransitionsScreen() }),
    Alerts("Alerts & Dialogs", Icons.Filled.NotificationImportant, { AlertsScreen() }),
    Recipes("Recipes", Icons.Filled.GridView, { RecipesScreen() }),
}
