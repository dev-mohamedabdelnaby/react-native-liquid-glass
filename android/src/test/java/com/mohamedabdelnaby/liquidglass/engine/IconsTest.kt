package com.mohamedabdelnaby.liquidglass.engine

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IconsTest {
    @Test
    fun everyKeyOfTheJsMapResolves() {
        // The JS map is the source of truth; reading it keeps the two lists in step.
        val js = File("../src/icons.ts").readText()
        val keys = Regex("""'[^']+':\s*'([^']+)'""").findAll(js).map { it.groupValues[1] }.toList()
        assertTrue("found ${keys.size} keys", keys.size >= 20)
        (keys + "done").forEach { assertNotNull(it, materialIcon(it)) }
    }

    @Test
    fun unknownKeyIsNull() {
        assertNull(materialIcon("nope"))
    }
}
