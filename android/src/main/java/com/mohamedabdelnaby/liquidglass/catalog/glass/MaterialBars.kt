package com.mohamedabdelnaby.liquidglass.catalog.glass

import com.mohamedabdelnaby.liquidglass.glassIcon
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
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.animateColorAsState
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
    // No edge effect asked: the bar is see-through over the page until content scrolls under it.
    val seeThrough = topEdge == EdgeEffect.Hidden || topEdge == EdgeEffect.StatusBar
    val defaults = TopAppBarDefaults.topAppBarColors()
    val scrolled by remember(scroll) { derivedStateOf { scroll.state.overlappedFraction > 0.01f } }
    // The bar and its accessory share one background, so the accessory takes the bar's scrolled
    // color with it, with no seam.
    val barColor by animateColorAsState(
        when {
            scrolled -> defaults.scrolledContainerColor
            seeThrough -> defaults.containerColor.copy(alpha = 0f)
            else -> defaults.containerColor
        },
        label = "top bar",
    )
    val page = MaterialTheme.colorScheme.surface
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
                    Column(Modifier.background(barColor)) { Box {
                        // See-through over content (a header photo): a scrim of the page color keeps the
                        // bar's icons readable.
                        if (seeThrough && !scrolled) Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(page.copy(alpha = 0.8f), page.copy(alpha = 0f)))))
                        TopAppBar(
                            title = titleContent,
                            navigationIcon = {
                                IconButton(LocalOnBack.current) {
                                    Icon(
                                        if (closeButton) glassIcon("xmark", Icons.Filled.Close) else glassIcon("chevron.backward", Icons.AutoMirrored.Filled.ArrowBack),
                                        if (closeButton) "Close" else "Back",
                                    )
                                }
                            },
                            actions = { CompositionLocalProvider(LocalMaterialBar provides true) { actions() } },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                            scrollBehavior = scroll,
                        )
                        }
                        topAccessory?.invoke()
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

/**
 * Material's search bar at the top of a screen (under its top app bar): a search icon, the field
 * with its placeholder, and a clear button once there is text (or [onClose], when given).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    autoFocus: Boolean = false,
    onClose: (() -> Unit)? = null,
) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    if (autoFocus) LaunchedEffect(Unit) { focus.requestFocus() }
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
                trailingIcon = when {
                    onClose != null -> { { IconButton({ focusManager.clearFocus(); onClose() }) { Icon(Icons.Filled.Close, "Close search") } } }
                    query.isNotEmpty() -> { { IconButton({ onQueryChange("") }) { Icon(Icons.Filled.Close, "Clear") } } }
                    else -> null
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
