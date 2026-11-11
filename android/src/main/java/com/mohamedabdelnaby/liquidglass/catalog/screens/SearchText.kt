package com.mohamedabdelnaby.liquidglass.catalog.screens

import java.text.Normalizer

private val Marks = "\\p{Mn}+".toRegex()

private fun String.folded(): String = Marks.replace(Normalizer.normalize(this, Normalizer.Form.NFD), "").lowercase()

/** Case- and diacritic-insensitive containment, like `localizedStandardContains`. */
internal fun String.matchesSearch(query: String): Boolean = query.isBlank() || folded().contains(query.trim().folded())
