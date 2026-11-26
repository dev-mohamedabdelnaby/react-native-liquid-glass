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
/** [symbol]: the SF Symbol the iOS catalog shows for it (the `icons` key); [icon]: Android's own. */
enum class GlassComponent(val title: String, val symbol: String, val icon: ImageVector, val screen: @Composable () -> Unit) {
    GlassEffect("Glass Effect", "drop.fill", Icons.Filled.WaterDrop, { GlassEffectScreen() }),
    GlassView("Glass View", "rectangle.on.rectangle", Icons.Filled.FilterNone, { GlassViewScreen() }),
    Interactive("Interactive Glass", "hand.tap.fill", Icons.Filled.TouchApp, { InteractiveGlassScreen() }),
    Buttons("Buttons", "button.horizontal.fill", Icons.Filled.SmartButton, { ButtonsScreen() }),
    Container("Glass Container", "square.on.square", Icons.Filled.Layers, { ContainerScreen() }),
    Morphing("Morphing", "wand.and.stars", Icons.Filled.AutoFixHigh, { MorphingScreen() }),
    Union("Union", "circle.grid.2x1.fill", Icons.Filled.JoinFull, { UnionScreen() }),
    Toolbar("Toolbar", "menubar.rectangle", Icons.Filled.WebAsset, { ToolbarScreen() }),
    FloatingHeader("Floating Header", "rectangle.portrait.topthird.inset.filled", Icons.Filled.Panorama, { FloatingHeaderScreen() }),
    TabBar("Tab Bar", "dock.rectangle", Icons.Filled.CallToAction, { TabBarScreen() }),
    Sheets("Sheets & Menus", "rectangle.bottomhalf.inset.filled", Icons.Filled.ViewAgenda, { SheetsScreen() }),
    Controls("Controls", "slider.horizontal.3", Icons.Filled.Tune, { ControlsScreen() }),
    Text("Text", "textformat", Icons.Filled.TextFields, { TextScreen() }),
    Transitions("Glass Transitions", "sparkles", Icons.Filled.AutoAwesome, { TransitionsScreen() }),
    Search("Search", "magnifyingglass", Icons.Filled.Search, { SearchScreen() }),
    EditSearch("Edit & Search Buttons", "pencil.and.list.clipboard", Icons.Filled.EditNote, { EditSearchScreen() }),
    ScrollEdge("Scroll Edge & Bars", "rectangle.topthird.inset.filled", Icons.Filled.VerticalAlignTop, { ScrollEdgeScreen() }),
    SplitView("Sidebar & Inspector", "sidebar.leading", Icons.AutoMirrored.Filled.ViewSidebar, { SplitViewScreen() }),
    ZoomTransitions("Zoom Transitions", "arrow.up.left.and.arrow.down.right", Icons.Filled.ZoomOutMap, { ZoomTransitionsScreen() }),
    Alerts("Alerts & Dialogs", "exclamationmark.bubble", Icons.Filled.NotificationImportant, { AlertsScreen() }),
    Recipes("Recipes", "square.grid.2x2.fill", Icons.Filled.GridView, { RecipesScreen() }),
}
