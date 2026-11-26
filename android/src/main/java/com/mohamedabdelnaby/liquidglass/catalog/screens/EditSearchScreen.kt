package com.mohamedabdelnaby.liquidglass.catalog.screens

import com.mohamedabdelnaby.liquidglass.glassIcon
import com.mohamedabdelnaby.liquidglass.catalog.glass.platformTextStyle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mohamedabdelnaby.liquidglass.catalog.glass.Fill
import com.mohamedabdelnaby.liquidglass.catalog.glass.Glass
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassBottomBar
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassEffectContainer
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.ListRow
import com.mohamedabdelnaby.liquidglass.catalog.glass.PlainBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassClickable
import com.mohamedabdelnaby.liquidglass.catalog.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.catalog.glass.groupedRow
import com.mohamedabdelnaby.liquidglass.catalog.liquid.LiquidExpandableSearch
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassIconButton
import com.mohamedabdelnaby.liquidglass.catalog.glass.MaterialSearchField
import com.mohamedabdelnaby.liquidglass.catalog.glass.isMaterial
import androidx.activity.compose.BackHandler
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.SystemColors

private enum class Mode { Idle, Searching, Editing }

private val Notes = listOf(
    "Groceries", "Workout plan", "Trip to Tokyo", "Book list", "Recipes",
    "Gift ideas", "Meeting notes", "Budget", "Garden", "Movies to watch",
    "Packing list", "Side project", "Birthday party", "Reading notes",
)

@Composable
fun EditSearchScreen() {
    var mode by remember { mutableStateOf(Mode.Idle) }
    var query by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf(*Notes.toTypedArray()) }
    val selection = remember { mutableStateListOf<String>() }
    val visible = items.filter { it.matchesSearch(query) }

    // LiquidExpandableSearch drops focus and the keyboard as it collapses.
    fun closeSearch() {
        query = ""
        mode = Mode.Idle
    }

    val title = when (mode) {
        Mode.Idle -> "Notes"
        Mode.Searching -> "Search"
        Mode.Editing -> if (selection.isEmpty()) "Select Items" else "${selection.size} Selected"
    }

    // Material: Search and Edit are top app bar actions; search opens a search bar under the bar and
    // editing turns the bar into a contextual one (Delete, Done). Back leaves either first.
    val material = isMaterial
    BackHandler(material && mode != Mode.Idle) { if (mode == Mode.Searching) closeSearch() else { selection.clear(); mode = Mode.Idle } }

    GlassScreen(
        title = title,
        backdrop = { PlainBackdrop(it) },
        photo = false,
        actions = {
            if (material) when (mode) {
                Mode.Idle -> {
                    GlassIconButton(glassIcon("magnifyingglass", Icons.Filled.Search), "Search", { mode = Mode.Searching })
                    GlassIconButton(glassIcon("pencil", Icons.Filled.Edit), "Edit", { mode = Mode.Editing })
                }
                Mode.Searching -> Unit
                Mode.Editing -> {
                    if (selection.isNotEmpty()) GlassIconButton(glassIcon("trash", Icons.Filled.Delete), "Delete ${selection.size}", { items.removeAll(selection); selection.clear() })
                    GlassIconButton(glassIcon("checkmark", Icons.Filled.Check), "Done", { selection.clear(); mode = Mode.Idle })
                }
            }
        },
        topAccessory = if (material && mode == Mode.Searching) {
            { MaterialSearchField(query, { query = it }, "Search notes", autoFocus = true, onClose = ::closeSearch) }
        } else null,
        bottomBar = {
            if (!material) GlassBottomBar {
                ActionBar(
                    mode = mode,
                    query = query,
                    onQuery = { query = it },
                    selected = selection.size,
                    onEdit = { mode = Mode.Editing },
                    onSearch = { mode = Mode.Searching },
                    onCloseSearch = ::closeSearch,
                    onDone = { selection.clear(); mode = Mode.Idle },
                    onDelete = { items.removeAll(selection); selection.clear() },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            NotesList(
                visible = visible,
                editing = mode == Mode.Editing,
                selection = selection,
                padding = padding,
                onDelete = { items.remove(it); selection.remove(it) },
                onMove = { from, to -> items.add(to, items.removeAt(from)) },
            )
            if (mode == Mode.Searching && visible.isEmpty()) {
                Column(
                    Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.Search, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No results for '$query'", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Check the spelling or try a new search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** The glass bar whose shapes morph between the idle, searching and editing states. */
@Composable
private fun ActionBar(
    mode: Mode,
    query: String,
    onQuery: (String) -> Unit,
    selected: Int,
    onEdit: () -> Unit,
    onSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onDone: () -> Unit,
    onDelete: () -> Unit,
) {
    if (mode != Mode.Editing) {
        // The search button grows into the field with its own close button; Edit fades out.
        LiquidExpandableSearch(
            expanded = mode == Mode.Searching,
            onExpandedChange = { if (it) onSearch() else onCloseSearch() },
            query = query,
            onQueryChange = onQuery,
            placeholder = "Search notes",
            modifier = Modifier.fillMaxWidth(),
            height = 52.dp,
        ) {
            Fill()
            CircleAction("Edit", onClick = onEdit) { Icon(glassIcon("pencil", Icons.Filled.Edit), null, tint = MaterialTheme.colorScheme.onSurface) }
        }
        return
    }
    GlassEffectContainer(Modifier.fillMaxWidth(), spacing = 12.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selected > 0) {
                Row(
                    Modifier
                        .height(52.dp)
                        .glassEffect(Glass.Regular.tint(SystemColors.Red.copy(alpha = 0.85f)).interactive())
                        .glassClickable(onClick = onDelete)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(glassIcon("trash", Icons.Filled.Delete), null, tint = Color.White)
                    Text("Delete $selected", color = Color.White, style = platformTextStyle(17.sp, MaterialTheme.typography.bodyLarge), fontWeight = FontWeight.SemiBold)
                }
            }
            Fill()
            CircleAction("Done", glass = Glass.Regular.tint(SystemColors.Blue.copy(alpha = 0.85f)), onClick = onDone) {
                Icon(glassIcon("checkmark", Icons.Filled.Check), null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun CircleAction(label: String, glass: Glass = Glass.Regular, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(52.dp).glassEffect(glass.interactive(), GlassCircle).glassClickable(label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun NotesList(
    visible: List<String>,
    editing: Boolean,
    selection: MutableList<String>,
    padding: PaddingValues,
    onDelete: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
) {
    val listState = rememberLazyListState()
    var dragging by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    LazyColumn(
        Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, padding.calculateBottomPadding() + 8.dp),
    ) {
        itemsIndexed(visible, key = { _, note -> note }) { index, note ->
            val isDragging = dragging == note
            val swipe = rememberSwipeToDismissBoxState()
            SwipeToDismissBox(
                state = swipe,
                enableDismissFromStartToEnd = false,
                gesturesEnabled = !editing,
                onDismiss = { if (it == SwipeToDismissBoxValue.EndToStart) onDelete(note) },
                backgroundContent = {
                    Box(
                        Modifier.fillMaxSize().groupedRow(index, visible.size).background(SystemColors.Red).padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) { Icon(Icons.Filled.Delete, "Delete", tint = Color.White) }
                },
                modifier = Modifier
                    .then(if (isDragging) Modifier.zIndex(1f).graphicsLayer { translationY = dragOffset; shadowElevation = 16f } else Modifier.animateItem()),
            ) {
                val checked = note in selection
                ListRow(
                    Modifier
                        .groupedRow(index, visible.size)
                        .then(if (editing) Modifier.clickable { if (checked) selection.remove(note) else selection.add(note) } else Modifier),
                    divider = index != visible.lastIndex,
                ) {
                    AnimatedVisibility(editing) {
                        Icon(
                            if (checked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                            if (checked) "Selected" else "Not selected",
                            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(glassIcon("note.text", Icons.AutoMirrored.Filled.Notes), null, tint = MaterialTheme.colorScheme.primary)
                    Text(note)
                    Fill()
                    AnimatedVisibility(editing) {
                        Icon(
                            Icons.Filled.DragHandle, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .semantics { contentDescription = "Reorder $note" }
                                .reorderHandle(note, listState, { dragging = it }, { dragOffset = it }, { dragOffset }, onMove),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Drag-to-reorder from a handle: the row follows the finger and swaps with a neighbour once its
 * centre passes the neighbour's centre.
 */
private fun Modifier.reorderHandle(
    key: String,
    listState: LazyListState,
    setDragging: (String?) -> Unit,
    setOffset: (Float) -> Unit,
    offset: () -> Float,
    onMove: (Int, Int) -> Unit,
): Modifier = pointerInput(key) {
    detectDragGestures(
        onDragStart = { setDragging(key); setOffset(0f) },
        onDragEnd = { setDragging(null); setOffset(0f) },
        onDragCancel = { setDragging(null); setOffset(0f) },
    ) { change, amount ->
        change.consume()
        setOffset(offset() + amount.y)
        val items = listState.layoutInfo.visibleItemsInfo
        val current = items.firstOrNull { it.key == key } ?: return@detectDragGestures
        val centre = current.offset + offset() + current.size / 2f
        val target = items.firstOrNull { it.key != key && it.key is String && centre.toInt() in it.offset..(it.offset + it.size) }
        if (target != null) {
            onMove(current.index, target.index)
            // Keep the row under the finger after the list re-lays out around it.
            setOffset(offset() + current.offset - target.offset)
        }
    }
}

@Preview(name = "Light") @Composable private fun EditSearchLight() = PreviewLight { EditSearchScreen() }
@Preview(name = "Dark") @Composable private fun EditSearchDark() = PreviewDark { EditSearchScreen() }
