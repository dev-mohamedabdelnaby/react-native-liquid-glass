package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidTabItem

/*
 * Material counterparts of the screen scaffolding: top and bottom bars, tab bars, surfaces. Each glass building block
 * calls its counterpart first thing when the style is Material, so the glass code after that call is unchanged.
 */

/** Inside a Material top or bottom bar: icon buttons are plain and glass groups draw no surface. */
internal val LocalMaterialBar = staticCompositionLocalOf { false }

/** A glass surface's place: a tonal surface, solid in the glass's tint when it has one. */
@Composable
internal fun Modifier.materialSurface(tint: Color?, shape: Shape): Modifier = when {
    tint != null && tint.alpha > 0f -> clip(shape).background(tint.copy(alpha = 1f))
    LocalMaterialBar.current -> this
    else -> clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialScreen(
    modifier: Modifier,
    actions: @Composable RowScope.() -> Unit,
    titleContent: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    topEdge: EdgeEffect,
    topAccessory: (@Composable () -> Unit)?,
    closeButton: Boolean,
    showTopBar: Boolean,
    backdrop: @Composable (Modifier) -> Unit,
    overlay: @Composable BoxScope.() -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    GlassSurfaceHost(modifier, backdrop) {
        Scaffold(
            Modifier.nestedScroll(scroll.nestedScrollConnection),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            topBar = {
                AnimatedVisibility(
                    showTopBar,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    Column {
                        TopAppBar(
                            title = titleContent,
                            navigationIcon = {
                                IconButton(LocalOnBack.current) {
                                    Icon(
                                        if (closeButton) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                        if (closeButton) "Close" else "Back",
                                    )
                                }
                            },
                            actions = { CompositionLocalProvider(LocalMaterialBar provides true) { actions() } },
                            // No edge effect: the bar is see-through over the page until it scrolls under.
                            colors = if (topEdge == EdgeEffect.Hidden || topEdge == EdgeEffect.StatusBar)
                                TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) else TopAppBarDefaults.topAppBarColors(),
                            scrollBehavior = scroll,
                        )
                        topAccessory?.let { Box(Modifier.background(MaterialTheme.colorScheme.surface)) { it() } }
                    }
                }
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
        ) { padding ->
            GlassContent(Modifier.fillMaxSize()) { content(padding) }
        }
        overlay()
    }
}

/** A bar title: the top app bar gives it its style. */
@Composable
internal fun MaterialBarTitle(text: String) = Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)

@Composable
internal fun MaterialBottomBar(modifier: Modifier, content: @Composable RowScope.() -> Unit) =
    CompositionLocalProvider(LocalMaterialBar provides true) {
        BottomAppBar(modifier, windowInsets = WindowInsets.navigationBars.union(WindowInsets.ime), content = content)
    }

/** Segmented controls are segmented buttons; bottom tab bars are a navigation bar. */
@Composable
internal fun MaterialTabs(selectedIndex: Int, onSelect: (Int) -> Unit, tabs: List<LiquidTabItem>, modifier: Modifier, segmented: Boolean, color: Color?) {
    if (segmented) {
        val check = LocalGlassSettings.current.segmentedCheck
        SingleChoiceSegmentedButtonRow(modifier) {
            tabs.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index, tabs.size),
                    icon = { if (check) SegmentedButtonDefaults.Icon(index == selectedIndex) },
                ) { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
        }
        return
    }
    // A picked bar color fills the bar; on a dark one the items turn white.
    val onDark = color != null && color.luminance() < 0.5f
    NavigationBar(modifier, containerColor = color ?: NavigationBarDefaults.containerColor) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = {
                    BadgedBox(badge = { if (tab.badge > 0) Badge { Text("${tab.badge}") } }) { tab.icon?.let { Icon(it, null) } }
                },
                label = { Text(tab.label) },
                colors = if (onDark) NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.White,
                    indicatorColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.75f),
                    unselectedTextColor = Color.White.copy(alpha = 0.75f),
                ) else NavigationBarItemDefaults.colors(),
            )
        }
    }
}

/**
 * The expandable search: [content] with a search icon button, which opens Material's search bar in
 * their place; its ✕ closes it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialExpandableSearch(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    // One node for the caller's modifier in both states: a screen's focus listener on it must not
    // see a fresh, unfocused node the moment the bar opens (that closed it again).
    Box(modifier) {
        if (!expanded) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                content()
                IconButton({ onExpandedChange(true) }) { Icon(Icons.Filled.Search, "Search") }
            }
        } else {
            MaterialSearchBar(query, onQueryChange, placeholder) { onExpandedChange(false) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialSearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String, onClose: () -> Unit) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) { focus.requestFocus() }
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { focusManager.clearFocus() },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.focusRequester(focus),
                placeholder = { Text(placeholder) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    IconButton({ focusManager.clearFocus(); onClose() }) { Icon(Icons.Filled.Close, "Close search") }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = Modifier.fillMaxWidth(),
    ) {}
}

/** Home: a large top app bar that collapses into a small one as the list scrolls under it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialHomeScaffold(title: String, content: @Composable (PaddingValues) -> Unit) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(scroll.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = { LargeTopAppBar(title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }, scrollBehavior = scroll) },
        content = content,
    )
}

/** A page's large title: Material's headline. */
@Composable
internal fun MaterialLargeTitle(text: String, modifier: Modifier) =
    Text(text, modifier.padding(bottom = 4.dp), style = MaterialTheme.typography.headlineMedium)
