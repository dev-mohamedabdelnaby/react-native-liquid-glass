package com.mohamedabdelnaby.liquidglass.catalog.screens

import android.content.Context
import android.content.Intent

/** Opens the Android share sheet (SwiftUI's `ShareLink`). */
internal fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(send, null))
}
