package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/*
 * Material counterparts of menus, popovers, alerts and sheets. Each glass building block
 * calls its counterpart first thing when the style is Material, so the glass code after that call is unchanged.
 */

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
