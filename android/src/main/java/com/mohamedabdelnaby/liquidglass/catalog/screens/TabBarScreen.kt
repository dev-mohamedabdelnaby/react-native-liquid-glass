package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.WaterDrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.CapsuleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CallToAction
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenuItem
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassMenu
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.outlined.SavedSearch
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.mohamedabdelnaby.liquidglass.catalog.glass.CollapsedBarTitle
import com.mohamedabdelnaby.liquidglass.catalog.glass.LargeTitle
import com.mohamedabdelnaby.liquidglass.catalog.glass.EdgeEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalOnBack
import com.mohamedabdelnaby.liquidglass.catalog.glass.ScrollEdge
import com.mohamedabdelnaby.liquidglass.catalog.glass.SearchFieldContent
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidTabBar
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidTabItem

private enum class Tab(val title: String, val icon: ImageVector, val badge: Int = 0) {
    Home("Home", Icons.Filled.Home),
    // Unread count shown as a badge, matching a typical app layout.
    Library("Library", Icons.AutoMirrored.Filled.LibraryBooks, badge = 3),
    Profile("Profile", Icons.Filled.AccountCircle),
    Search("Search", Icons.Filled.Search),
    Settings("Settings", Icons.Filled.Settings),
}

/** The tab bar layouts this screen can demo. */
private enum class Variant(val title: String, val icon: ImageVector) {
    NowPlaying("With Now Playing Bar", Icons.Filled.MusicNote),
    SearchInBar("Search Inside Tabs", Icons.Outlined.ManageSearch),
    SearchSeparated("Search Separated", Icons.Outlined.SavedSearch),
    NoSearch("Tabs Without Search", Icons.Outlined.CallToAction),
}

private val MainTabs = listOf(Tab.Home, Tab.Library, Tab.Profile)

/** The bar fill picked on the Tab Bar page: null keeps the liquid glass. */
private val LocalBarColor = staticCompositionLocalOf<Color?> { null }

private val BarColors = listOf(SystemColors.Blue, SystemColors.Purple, SystemColors.Pink, SystemColors.Orange, SystemColors.Green, Color(0xFF1C1C1E))

/** A bar piece's surface: liquid glass, or the picked solid color in its place. */
@Composable
private fun Modifier.barSurface(shape: RoundedCornerShape = CapsuleShape): Modifier {
    val color = LocalBarColor.current ?: return glassEffect(Glass.Regular.interactive(), shape)
    return background(color, shape)
}

private val onDarkBar: Boolean @Composable get() = LocalBarColor.current?.let { it.luminance() < 0.5f } == true

/** Labels on the bar: white on a dark fill. */
private val BarContent: Color @Composable get() = if (onDarkBar) Color.White else MaterialTheme.colorScheme.onSurface

/** The selected tab's color on the bar. */
private val BarAccent: Color @Composable get() = if (onDarkBar) Color.White else MaterialTheme.colorScheme.primary

/**
 * As on iOS: a `TabView` must be the root of its screen, so the page picks a layout and presents
 * its demo full screen.
 */
@Composable
fun TabBarScreen() {
    var presented by rememberSaveable { mutableStateOf<Variant?>(null) }
    // The last demo stays composed while the cover slides away.
    var shown by rememberSaveable { mutableStateOf(Variant.NowPlaying) }
    var minimizeOnScroll by rememberSaveable { mutableStateOf(true) }
    var searchFullWidth by rememberSaveable { mutableStateOf(false) }
    var barColor by remember { mutableStateOf<Color?>(null) }
    Box(Modifier.fillMaxSize()) {
        GlassScreen(title = "Tab Bar") { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Floating glass tab bars. Pick a layout below.",
                    Modifier.glassEffect(Glass.Regular, RoundedCornerShape(24.dp)).padding(16.dp),
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                )
                Column(
                    Modifier.fillMaxWidth().glassEffect(Glass.Regular, RoundedCornerShape(20.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.CloseFullscreen, null)
                        Box(Modifier.weight(1f)) { SwitchRow("Minimize on scroll", minimizeOnScroll) { minimizeOnScroll = it } }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    // Applies to "Search Separated": the field can fill the bar or keep the active tab beside it.
                    GlassMenu(anchor = { open ->
                        Row(
                            Modifier.glassClickable(label = "Search field", onClick = open).padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(if (searchFullWidth) "Full Width" else "With Active Tab", fontSize = 17.sp)
                            Icon(Icons.Filled.UnfoldMore, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }) { dismiss ->
                        GlassMenuItem("With Active Tab", { searchFullWidth = false; dismiss() }, checked = !searchFullWidth)
                        GlassMenuItem("Full Width", { searchFullWidth = true; dismiss() }, checked = searchFullWidth)
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    // Bar color: Glass keeps the liquid glass; a swatch fills every bar piece instead.
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BarSwatch(null, barColor == null) { barColor = null }
                        BarColors.forEach { c -> BarSwatch(c, barColor == c) { barColor = c } }
                    }
                }
                Variant.entries.forEach { variant ->
                    GlassProminentButton({ shown = variant; presented = variant }, Modifier.fillMaxWidth(), size = GlassControlSize.Large) {
                        Icon(variant.icon, null)
                        Text(variant.title)
                    }
                }
            }
        }
        // fullScreenCover: slides up over the page and back down when closed.
        AnimatedVisibility(presented != null, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
            BackHandler { presented = null }
            // A fresh identity per variant so no state carries over between demos.
            CompositionLocalProvider(LocalOnBack provides { presented = null }, LocalBarColor provides barColor) {
                key(shown) {
                    if (shown == Variant.SearchSeparated) SeparatedSearchDemo(minimizeOnScroll, searchFullWidth)
                    else SystemTabBarDemo(shown, minimizeOnScroll)
                }
            }
        }
    }
}

/** tabBarMinimizeBehavior(.onScrollDown): reports scrolling down (true) or up (false). */
@Composable
private fun rememberScrollDirection(onDirection: (down: Boolean) -> Unit): NestedScrollConnection {
    val current by rememberUpdatedState(onDirection)
    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -2f) current(true) else if (available.y > 2f) current(false)
                return Offset.Zero
            }
        }
    }
}

/** The system `TabView` tab bar, in the Now Playing, Search Inside Tabs and Without Search layouts. */
@Composable
private fun SystemTabBarDemo(variant: Variant, minimizeOnScroll: Boolean) {
    val tabs = MainTabs + if (variant == Variant.NoSearch) Tab.Settings else Tab.Search
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var minimized by remember { mutableStateOf(false) }
    val scrollDirection = rememberScrollDirection { down -> if (minimizeOnScroll) minimized = down }
    // One NavigationStack per tab: each keeps its own scroll position and large title.
    val listState = key(tab) { rememberLazyListState() }
    val scrolled by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    GlassScreen(
        title = tab.title,
        closeButton = true,
        // The large title collapses into the bar once it scrolls away.
        titleContent = { CollapsedBarTitle(tab.title, scrolled) },
        bottomBar = {
            TabBar(
                tabs, tab, minimized,
                accessory = variant == Variant.NowPlaying,
                // Only Tab(role: .search) splits into its own circle when the bar minimizes.
                searchRole = variant == Variant.NowPlaying,
            ) { tab = it; minimized = false }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().nestedScroll(scrollDirection)) {
            if (tab == Tab.Search) SearchTab(padding, listState) else TabList(tab.title, padding, listState)
        }
    }
}

/**
 * A custom glass tab bar with the tabs in one capsule and search as a separate glass circle.
 * Tapping search collapses the tabs into a single circle (or away, at full width) and morphs the
 * search circle into a field. With minimizing on, scrolling down collapses the tabs too.
 */
@Composable
private fun SeparatedSearchDemo(minimizeOnScroll: Boolean, searchFullWidth: Boolean) {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var searching by remember { mutableStateOf(false) }
    var minimized by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val scrollDirection = rememberScrollDirection { down -> if (minimizeOnScroll && !searching) minimized = down }
    // Every tab keeps its scroll position; only the selected one is shown.
    val states = MainTabs.associateWith { rememberLazyListState() }
    val searchState = rememberLazyListState()
    val listState = if (searching) searchState else states.getValue(tab)
    val scrolled by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val title = if (searching) "Search" else tab.title
    fun exitSearch() { searching = false; query = ""; keyboard?.hide() }
    BackHandler(searching) { exitSearch() }
    LaunchedEffect(searching) { if (searching) { withFrameNanos { }; focus.requestFocus(); keyboard?.show() } }

    GlassScreen(
        title = title,
        closeButton = true,
        titleContent = { CollapsedBarTitle(title, scrolled) },
        bottomBar = {
            Box(Modifier.fillMaxWidth()) {
                ScrollEdge(EdgeEffect.Soft, top = false, Modifier.matchParentSize())
                GlassEffectContainer(
                    Modifier.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    spacing = 12.dp,
                ) {
                    // The tabs piece minimizes on the other tab bars' spring and progress: it narrows
                    // into a circle as its tabs shrink into it and the selected tab's icon grows in.
                    val collapsed = searching || minimized
                    val p by animateFloatAsState(if (collapsed) 1f else 0f, MinimizeSpring, label = "minimize")
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                    // The tabs' full width: the bar less the search circle and the gap.
                    val full = maxWidth - SeparatedGap - SeparatedHeight
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // One glass member per piece, so the container morphs each between its shapes.
                        if (!(searching && searchFullWidth)) {
                            val q = p.coerceIn(0f, 1f)
                            Box(
                                Modifier
                                    .width(lerp(full, SeparatedHeight, q))
                                    .height(SeparatedHeight)
                                    .barSurface()
                                    .then(if (collapsed) Modifier.glassClickable(label = "Show tabs") { exitSearch(); minimized = false } else Modifier),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (q < 1f) Box(
                                    Modifier
                                        .wrapContentWidth(Alignment.Start, unbounded = true)
                                        .requiredWidth(full)
                                        .fillMaxHeight()
                                        .graphicsLayer {
                                            scaleX = lerp(1f, SeparatedHeight / full, q)
                                            transformOrigin = TransformOrigin(0f, 0.5f)
                                            alpha = 1f - q
                                        },
                                ) { TabsCapsule(tab) { tab = it } }
                                if (q > 0f) Icon(
                                    tab.icon, null,
                                    Modifier.align(Alignment.Center).size(24.dp).graphicsLayer {
                                        alpha = q
                                        scaleX = lerp(0.6f, 1f, q)
                                        scaleY = scaleX
                                    },
                                    tint = BarAccent,
                                )
                            }
                            Spacer(Modifier.width(SeparatedGap))
                        }
                        if (!searching) Fill()
                        Box(
                            (if (searching) Modifier.weight(1f).height(SeparatedHeight) else Modifier.size(SeparatedHeight))
                                .barSurface()
                                .then(if (searching) Modifier else Modifier.glassClickable(label = "Search") { searching = true; minimized = false }),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (searching) {
                                Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SearchFieldContent(query, { query = it }, "Search", Modifier.weight(1f), focus)
                                    // With no tab circle beside the field, this is the way back to the tabs.
                                    if (searchFullWidth) Text("Cancel", Modifier.glassClickable { exitSearch() }, color = BarContent, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                                }
                            } else {
                                Icon(Icons.Filled.Search, null, Modifier.size(24.dp), tint = BarContent)
                            }
                        }
                    }
                    }
                }
            }
        },
    ) { padding ->
        // Only the shown list is composed: hidden lists stacked at alpha 0 still took every drag, so
        // the shown one never scrolled. Its state above keeps each tab's scroll position.
        Box(Modifier.fillMaxSize().nestedScroll(scrollDirection)) {
            if (searching) SearchTab(padding, searchState, query) else TabList(tab.title, padding, states.getValue(tab))
        }
    }
}

private val SeparatedHeight = 64.dp
private val SeparatedGap = 12.dp

/** Minimizing and restoring a tab bar on scroll: one spring moves every piece, in every layout. */
private val MinimizeSpring = spring<Float>(dampingRatio = 0.85f, stiffness = 320f)

/** One bar color choice: a swatch, or the glass drop for no color; the picked one is ringed. */
@Composable
private fun BarSwatch(color: Color?, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
            .padding(4.dp)
            .then(if (color != null) Modifier.background(color, CircleShape) else Modifier.glassEffect(Glass.Regular, GlassCircle))
            .selectable(selected, interactionSource = null, indication = null, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = if (color == null) "Glass bar" else "Solid bar color" },
        contentAlignment = Alignment.Center,
    ) { if (color == null) Icon(Icons.Outlined.WaterDrop, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) }
}

/** All tabs in one glass capsule, with a highlight sliding behind the selected tab. */
@Composable
private fun TabsCapsule(selected: Tab, onSelect: (Tab) -> Unit) {
    val index by animateFloatAsState(MainTabs.indexOf(selected).toFloat(), spring(dampingRatio = 1f, stiffness = 158f), label = "selection")
    val highlight = BarContent.copy(alpha = if (onDarkBar) 0.2f else 0.1f)
    Row(
        Modifier.fillMaxSize().padding(4.dp).drawBehind {
            val w = size.width / MainTabs.size
            drawRoundRect(highlight, Offset(w * index, 0f), Size(w, size.height), CornerRadius(size.height / 2f))
        },
    ) {
        MainTabs.forEach { t ->
            val tint = if (t == selected) BarAccent else BarContent
            Column(
                Modifier.weight(1f).fillMaxHeight().selectable(t == selected, interactionSource = null, indication = null, role = Role.Tab) { onSelect(t) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            ) {
                Box {
                    Icon(t.icon, null, Modifier.size(22.dp), tint = tint)
                    if (t.badge > 0) {
                        Text(
                            "${t.badge}",
                            Modifier.align(Alignment.TopEnd).offset(x = 14.dp, y = (-6).dp).defaultMinSize(18.dp, 18.dp)
                                .background(SystemColors.Red, CircleShape).wrapContentSize(),
                            color = Color.White, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(t.title, color = tint, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TabList(title: String, padding: PaddingValues, state: LazyListState) {
    LazyColumn(
        Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding(), 16.dp, padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { LargeTitle(title) }
        items(30) { index ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("$title item ${index + 1}", fontSize = 17.sp) }
        }
    }
}

/** Search results; [external] is the query from a field outside the list, which then hides its own. */
@Composable
private fun SearchTab(padding: PaddingValues, state: LazyListState, external: String? = null) {
    var own by remember { mutableStateOf("") }
    val query = external ?: own
    val results = (1..20).map { "Result $it" }.filter { it.contains(query, ignoreCase = true) }
    LazyColumn(
        Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding(), 16.dp, padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { LargeTitle("Search") }
        if (external == null) item {
            SearchFieldContent(
                own, { own = it }, "Search",
                Modifier.fillMaxWidth().glassEffect(Glass.Regular.interactive()).padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        items(results) { result ->
            Text(result, Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).padding(16.dp))
        }
    }
}

private val AccessoryHeight = 48.dp
private val BarHeight = 62.dp
private val AccessoryGap = 8.dp
// Minimized: the selected tab's circle, the accessory between, Search's circle.
private val MiniCircle = 48.dp
private val MiniGap = 12.dp

@Composable
private fun TabBar(
    tabs: List<Tab>,
    selected: Tab,
    minimized: Boolean,
    accessory: Boolean,
    searchRole: Boolean,
    onSelect: (Tab) -> Unit,
) {
    // One spring moves every piece, so the bar, the accessory and the circles change as one.
    val p by animateFloatAsState(if (minimized) 1f else 0f, MinimizeSpring, label = "minimize")
    Box(Modifier.fillMaxWidth()) {
        ScrollEdge(EdgeEffect.Soft, top = false, Modifier.matchParentSize())
        Box(
            // LiquidBottomTabBar: 20 dp from the screen edges, 12 dp above the system bar.
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(lerp(if (accessory) AccessoryHeight + AccessoryGap + BarHeight else BarHeight, MiniCircle, p.coerceIn(0f, 1f))),
        ) {
            // iOS 26: one full-width bar with Search as its last tab; minimizing shrinks it into the
            // selected tab's circle from the leading edge.
            if (p < 1f) {
                LiquidTabBar(
                    selectedIndex = tabs.indexOf(selected),
                    color = LocalBarColor.current,
                    onSelect = { onSelect(tabs[it]) },
                    tabs = tabs.map { LiquidTabItem(it.title, it.icon, it.badge) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .graphicsLayer {
                            val q = p.coerceIn(0f, 1f)
                            scaleX = lerp(1f, MiniCircle.toPx() / size.width, q)
                            scaleY = lerp(1f, MiniCircle.toPx() / size.height, q)
                            transformOrigin = TransformOrigin(0f, 1f)
                            alpha = 1f - q
                        },
                )
            }
            if (p > 0f) {
                MiniTab(selected.icon, selected.title, BarAccent, p, Modifier.align(Alignment.BottomStart)) { onSelect(selected) }
                if (searchRole) MiniTab(Tab.Search.icon, Tab.Search.title, BarContent, p, Modifier.align(Alignment.BottomEnd)) { onSelect(Tab.Search) }
            }
            // tabViewBottomAccessory: above the bar, then inline between the two circles.
            if (accessory) Row(
                Modifier
                    .layout { measurable, constraints ->
                        val q = p.coerceIn(0f, 1f)
                        val inset = (lerp(0.dp, MiniCircle + MiniGap, q)).roundToPx()
                        val placeable = measurable.measure(Constraints.fixed(constraints.maxWidth - inset * 2, AccessoryHeight.roundToPx()))
                        layout(constraints.maxWidth, placeable.height) { placeable.place(inset, 0) }
                    }
                    .barSurface()
                    .glassClickable(label = "Now Playing") {},
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.MusicNote, null, tint = BarContent)
                Text("Now Playing", color = BarContent, fontSize = 17.sp)
            }
        }
    }
}

/** A minimized bar's circle: grows in where the bar shrinks to, or beside it for Search. */
@Composable
private fun MiniTab(icon: ImageVector, label: String, tint: Color, p: Float, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(MiniCircle)
            .graphicsLayer {
                val q = p.coerceIn(0f, 1f)
                alpha = q
                scaleX = lerp(0.6f, 1f, q)
                scaleY = lerp(0.6f, 1f, q)
            }
            .barSurface(GlassCircle)
            .glassClickable(label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, Modifier.size(26.dp), tint = tint) }
}

@Preview(name = "Light") @Composable private fun TabBarLight() = PreviewLight { TabBarScreen() }
@Preview(name = "Dark") @Composable private fun TabBarDark() = PreviewDark { TabBarScreen() }
