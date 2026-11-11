package com.mohamedabdelnaby.liquidglass

import com.mohamedabdelnaby.liquidglass.catalog.GlassComponent
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** The JS screen ids (src/screens.tsx) are the catalog's GlassComponent entries, in Home order. */
class ScreenIdsTest {
  @Test
  fun everyJsScreenIdMapsToAGlassComponent() {
    val ids = Regex("""\{ id: '(\w+)'""").findAll(File("../src/screens.tsx").readText()).map { it.groupValues[1] }.toList()
    assertEquals(21, ids.size)
    ids.forEach { assertNotNull(it, screenComponent(it)) }
    assertEquals(GlassComponent.entries.map { it.name.replaceFirstChar(Char::lowercase) }, ids)
  }
}
