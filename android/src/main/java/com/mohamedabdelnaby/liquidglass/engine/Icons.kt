package com.mohamedabdelnaby.liquidglass.engine

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.graphics.vector.ImageVector

/** Material icon for a key of the JS `ANDROID_ICONS` map (plus `done`); null for any other key. */
fun materialIcon(key: String): ImageVector? = when (key) {
    "check" -> Icons.Filled.Check
    "close" -> Icons.Filled.Close
    "add" -> Icons.Filled.Add
    "remove" -> Icons.Filled.Remove
    "search" -> Icons.Filled.Search
    "favorite_border" -> Icons.Outlined.FavoriteBorder
    "share" -> Icons.Filled.Share
    "more_horiz" -> Icons.Filled.MoreHoriz
    "delete" -> Icons.Filled.Delete
    "done" -> Icons.Filled.Done
    else -> null
}
