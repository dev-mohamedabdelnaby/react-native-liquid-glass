package com.mohamedabdelnaby.liquidglass.catalog.glass

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import kotlin.math.roundToInt
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidTabItem

/*
 * The Material 3 counterpart of each building block, drawn when the style is Material. Each glass
 * block calls its counterpart first thing, so the glass code below that call is unchanged.
 */

/** Inside a Material top or bottom bar: icon buttons are plain and glass groups draw no surface. */
private val LocalMaterialBar = staticCompositionLocalOf { false }

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

/**
 * Material's button roles: prominent glass is a filled button in its tint; glass a filled tonal
 * button; clear glass an outlined one; frosted (not liquid) glass, a quiet action, a text button.
 */
@Composable
internal fun MaterialButton(
    onClick: () -> Unit,
    modifier: Modifier,
    prominent: Boolean,
    glass: Glass,
    color: Color,
    shape: Shape,
    size: GlassControlSize,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    val sized = modifier.defaultMinSize(minHeight = size.minHeight)
    if (prominent) {
        val scheme = MaterialTheme.colorScheme
        val label = if (color == scheme.primary) scheme.onPrimary else moreContrasting(color, scheme.surface, scheme.onSurface)
        Button(onClick, sized, enabled, shape, ButtonDefaults.buttonColors(containerColor = color, contentColor = label), content = content)
        return
    }
    // A glass button's own label color (an error red) stays; the default takes the role's.
    val own = color.takeIf { it != MaterialTheme.colorScheme.onSurface }
    when {
        !glass.liquid -> TextButton(onClick, sized, enabled, colors = own?.let { ButtonDefaults.textButtonColors(contentColor = it) } ?: ButtonDefaults.textButtonColors(), content = content)
        glass.clear -> OutlinedButton(onClick, sized, enabled, colors = own?.let { ButtonDefaults.outlinedButtonColors(contentColor = it) } ?: ButtonDefaults.outlinedButtonColors(), content = content)
        else -> FilledTonalButton(onClick, sized, enabled, shape, own?.let { ButtonDefaults.filledTonalButtonColors(contentColor = it) } ?: ButtonDefaults.filledTonalButtonColors(), content = content)
    }
}

/** iOS-style symbols the screens use, as Android draws them; [icon] itself when it has no Android form. */
internal fun androidIcon(icon: ImageVector): ImageVector = when (icon) {
    Icons.Filled.MoreHoriz -> Icons.Filled.MoreVert
    Icons.Outlined.IosShare -> Icons.Filled.Share
    Icons.AutoMirrored.Rounded.KeyboardArrowLeft -> Icons.AutoMirrored.Filled.ArrowBack
    Icons.AutoMirrored.Rounded.KeyboardArrowRight -> Icons.AutoMirrored.Filled.ArrowForward
    Icons.Filled.UnfoldMore -> Icons.Filled.ArrowDropDown
    else -> icon
}

/** [icon] in Android's form while the style is Material. */
@Composable
fun nativeIcon(icon: ImageVector): ImageVector = if (isMaterial) androidIcon(icon) else icon

/** An icon button: plain in a bar, filled tonal on its own. */
@Composable
internal fun MaterialIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier, size: Dp, tint: Color, enabled: Boolean) {
    val own = tint.takeIf { it != MaterialTheme.colorScheme.onSurface }
    val android = androidIcon(icon)
    if (LocalMaterialBar.current) {
        IconButton(onClick, modifier, enabled) { Icon(android, contentDescription, tint = own ?: LocalContentColor.current) }
    } else {
        FilledTonalIconButton(onClick, modifier.size(size), enabled) { Icon(android, contentDescription, tint = own ?: LocalContentColor.current) }
    }
}

/** Which tenth of [range] [value] is in: the slider ticks a haptic each time it changes. */
internal fun sliderDetent(value: Float, range: ClosedFloatingPointRange<Float>): Int {
    val span = range.endInclusive - range.start
    if (span <= 0f) return 0
    return (((value - range.start) / span).coerceIn(0f, 1f) * 10f).toInt()
}

/** The value as the indicator and TalkBack say it: a percent for a 0–1 slider, else the number. */
internal fun sliderLabel(value: Float, range: ClosedFloatingPointRange<Float>): String =
    if (range.endInclusive <= 1f) "${(value * 100).roundToInt()}%" else "${value.roundToInt()}"

private fun contrast(a: Color, b: Color): Float {
    val (hi, lo) = a.luminance().let { la -> b.luminance().let { lb -> maxOf(la, lb) to minOf(la, lb) } }
    return (hi + 0.05f) / (lo + 0.05f)
}

/** Of [first] and [second], the one that reads better on [background]. */
internal fun moreContrasting(background: Color, first: Color, second: Color): Color =
    if (contrast(background, first) >= contrast(background, second)) first else second

/**
 * Material 3 Expressive's slider: the thicker 24 dp track, a 44 dp handle that narrows while
 * pressed, a value indicator above it while held, and a light haptic each tenth of the way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    tint: Color,
    contentDescription: String?,
) {
    val scheme = MaterialTheme.colorScheme
    // A slider's own tint, else the theme's primary (Home's tint when set: see the theme).
    val accent = tint
    val colors = SliderDefaults.colors(
        thumbColor = LocalGlassSettings.current.dropColor?.copy(alpha = 1f) ?: accent,
        activeTrackColor = accent,
        activeTickColor = moreContrasting(accent, scheme.surface, scheme.onSurface),
    )
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val held = interaction.collectIsPressedAsState().value || interaction.collectIsDraggedAsState().value
    val label = sliderLabel(value, valueRange)
    // "Remove animations" (animator scale 0): the indicator shows and hides at once.
    val context = LocalContext.current
    val still = remember { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
    // One node for TalkBack, the slider's own replaced: its label, value ("Blur, 3") and adjust action.
    Box(modifier.clearAndSetSemantics {
        contentDescription?.let { this.contentDescription = it }
        stateDescription = label
        progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange)
        setProgress { onValueChange(it.coerceIn(valueRange)); true }
    }) { Slider(
        value = value,
        onValueChange = {
            // Only on a step, never continuously.
            if (sliderDetent(it, valueRange) != sliderDetent(value, valueRange)) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            onValueChange(it)
        },
        modifier = Modifier.fillMaxWidth(),
        valueRange = valueRange,
        colors = colors,
        interactionSource = interaction,
        thumb = {
            Box(contentAlignment = Alignment.Center) {
                SliderDefaults.Thumb(interaction, colors = colors, thumbSize = DpSize(4.dp, 44.dp))
                AnimatedVisibility(
                    held,
                    // Above the handle, without changing the slider's size.
                    Modifier.layout { measurable, _ ->
                        val p = measurable.measure(Constraints())
                        layout(0, 0) { p.place(-p.width / 2, -p.height - 30.dp.roundToPx()) }
                    },
                    enter = if (still) EnterTransition.None else scaleIn(spring(dampingRatio = 0.6f, stiffness = 800f), transformOrigin = TransformOrigin(0.5f, 1f)) + fadeIn(),
                    exit = if (still) ExitTransition.None else scaleOut(spring(stiffness = 800f), transformOrigin = TransformOrigin(0.5f, 1f)) + fadeOut(),
                ) {
                    Surface(shape = CircleShape, color = scheme.inverseSurface, contentColor = scheme.inverseOnSurface) {
                        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        track = { SliderDefaults.Track(it, Modifier.height(24.dp), colors = colors, thumbTrackGapSize = 6.dp, trackInsideCornerSize = 4.dp) },
    ) }
}

/** Material 3 Expressive's switch: a check or ✕ on the thumb, in the theme's accent. */
@Composable
internal fun MaterialSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier) = Switch(
    checked, onCheckedChange, modifier,
    thumbContent = { Icon(if (checked) Icons.Filled.Check else Icons.Filled.Close, null, Modifier.size(SwitchDefaults.IconSize)) },
)

/** A stepper as Material has none: − and + tonal icon buttons, each disabled at its end of [range]. */
@Composable
internal fun MaterialStepper(value: Int, onValueChange: (Int) -> Unit, range: IntRange) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton({ onValueChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Filled.Remove, "Decrease") }
        FilledTonalIconButton({ onValueChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Filled.Add, "Increase") }
    }
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

/** A menu anchored to [anchor]: Material's dropdown menu. [onClosed] runs once it has closed. */
@Composable
internal fun MaterialMenu(
    modifier: Modifier,
    onClosed: () -> Unit,
    anchor: @Composable (open: () -> Unit) -> Unit,
    items: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var opened by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) {
        if (expanded) opened = true else if (opened) { opened = false; onClosed() }
    }
    Box(modifier) {
        anchor { expanded = true }
        DropdownMenu(expanded, { expanded = false }) { items { expanded = false } }
    }
}

@Composable
internal fun MaterialDropdownMenu(expanded: Boolean, onDismissRequest: () -> Unit, modifier: Modifier, offset: DpOffset, content: @Composable ColumnScope.() -> Unit) =
    DropdownMenu(expanded, onDismissRequest, modifier, offset, content = content)

@Composable
internal fun MaterialMenuItem(text: String, onClick: () -> Unit, icon: ImageVector?, destructive: Boolean, checked: Boolean) {
    val error = MaterialTheme.colorScheme.error
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        leadingIcon = icon?.let { { Icon(it, null) } },
        trailingIcon = if (checked) { { Icon(Icons.Filled.Check, "Selected") } } else null,
        colors = if (destructive) MenuDefaults.itemColors(textColor = error, leadingIconColor = error) else MenuDefaults.itemColors(),
    )
}

@Composable
internal fun MaterialMenuHeader(text: String) = Text(
    text,
    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
internal fun MaterialMenuDivider() = HorizontalDivider()

/** A popover is a dropdown menu holding its content, anchored to the parent. */
@Composable
internal fun MaterialPopover(expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) =
    DropdownMenu(expanded, onDismissRequest, content = content)

/**
 * An alert: title, message and its actions as text buttons in a row at the end, the first (the
 * confirming one, as the screens list them) rightmost, as Material orders them.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MaterialAlertDialog(title: String, message: String, onDismissRequest: () -> Unit, actions: @Composable ColumnScope.() -> Unit) =
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            Column {
                val column = this
                // Laid out in the opposite direction so the first action ends up at the end; each
                // button keeps the screen's direction.
                CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Ltr else LayoutDirection.Rtl) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)) {
                        CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) { column.actions() }
                    }
                }
            }
        },
        title = { Text(title) },
        text = if (message.isEmpty()) null else { { Text(message) } },
    )

@Composable
internal fun MaterialDialogAction(text: String, onClick: () -> Unit, destructive: Boolean) =
    TextButton(onClick) { Text(text, color = if (destructive) MaterialTheme.colorScheme.error else Color.Unspecified) }

/** A modal bottom sheet with its drag handle; filling it gives the half and full heights. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialBottomSheet(onDismissRequest: () -> Unit, sheetState: SheetState, content: @Composable ColumnScope.() -> Unit) =
    ModalBottomSheet(onDismissRequest, sheetState = sheetState) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, content = content)
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
    if (!expanded) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            content()
            IconButton({ onExpandedChange(true) }) { Icon(Icons.Filled.Search, "Search") }
        }
        return
    }
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
                    IconButton({ focusManager.clearFocus(); onExpandedChange(false) }) { Icon(Icons.Filled.Close, "Close search") }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier,
    ) {}
}

/** Inside a [MaterialListSection]: each row is its own segment of the group. */
private val LocalMaterialSection = staticCompositionLocalOf { false }

/** The gap between a group's segments, and their corners: small inside the group, large at its ends. */
private val SegmentGap = 2.dp
private val SegmentCorner = 4.dp
private val GroupCorner = 20.dp

/**
 * A settings group as Android 16 draws it: a primary-colored section header, then each row on its
 * own tonal segment with a small gap between, the group's ends rounded, and the footer below.
 */
@Composable
internal fun MaterialListSection(modifier: Modifier, header: String?, footer: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        header?.let { MaterialSectionHeader(it, Modifier.padding(horizontal = 16.dp)) }
        CompositionLocalProvider(LocalMaterialSection provides true) {
            // Clipping the group rounds the ends of its first and last segments.
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(GroupCorner)), verticalArrangement = Arrangement.spacedBy(SegmentGap), content = content)
        }
        footer?.let { Text(it, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

/** A section header: `titleSmall` in the primary color, as Android's settings lists have them. */
@Composable
internal fun MaterialSectionHeader(text: String, modifier: Modifier = Modifier) =
    Text(text, modifier, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

/** One list row: 56 dp at least, no divider; in a section, its own tonal segment. */
@Composable
internal fun MaterialListRow(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    val segment = if (LocalMaterialSection.current) Modifier.clip(RoundedCornerShape(SegmentCorner)).background(MaterialTheme.colorScheme.surfaceContainerHigh) else Modifier
    Row(
        modifier.then(segment).fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

/** A row of a list laid out by hand ([index] of [count]): a segment of a group, as in [MaterialListSection]. */
@Composable
internal fun Modifier.materialSegment(index: Int, count: Int): Modifier {
    val top = if (index == 0) GroupCorner else SegmentCorner
    val bottom = if (index == count - 1) GroupCorner else SegmentCorner
    return padding(bottom = if (index == count - 1) 0.dp else SegmentGap)
        .clip(RoundedCornerShape(top, top, bottom, bottom))
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
