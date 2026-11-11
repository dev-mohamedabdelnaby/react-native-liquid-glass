package com.mohamedabdelnaby.liquidglass.engine

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector

/** Material icon for a key of the JS `ANDROID_ICONS` map (plus `done`); null for any other key. */
fun materialIcon(key: String): ImageVector? = when (key) {
    "check" -> Icons.Filled.Check
    "close" -> Icons.Filled.Close
    "add" -> Icons.Filled.Add
    "remove" -> Icons.Filled.Remove
    "search" -> Icons.Filled.Search
    "favorite_border" -> Icons.Outlined.FavoriteBorder
    "favorite" -> Icons.Filled.Favorite
    "share" -> Icons.Filled.Share
    "more_horiz" -> Icons.Filled.MoreHoriz
    "delete" -> Icons.Filled.Delete
    "done" -> Icons.Filled.Done
    // The catalog's back button: iOS-style chevron.
    "arrow_back_ios" -> Icons.AutoMirrored.Rounded.ArrowBackIos
    "star_border" -> Icons.Outlined.StarBorder
    "star" -> Icons.Filled.Star
    "settings" -> Icons.Outlined.Settings
    "notifications" -> Icons.Outlined.Notifications
    "home" -> Icons.Outlined.Home
    "person" -> Icons.Outlined.Person
    "bolt" -> Icons.Filled.Bolt
    "play_arrow" -> Icons.Filled.PlayArrow
    "pause" -> Icons.Filled.Pause
    "fast_forward" -> Icons.Filled.FastForward
    else -> null
}
