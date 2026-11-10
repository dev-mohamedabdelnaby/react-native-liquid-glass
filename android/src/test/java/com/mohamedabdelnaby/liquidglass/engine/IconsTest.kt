package com.mohamedabdelnaby.liquidglass.engine

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IconsTest {
    @Test
    fun everyMappedKeyResolves() {
        listOf("check", "close", "add", "remove", "search", "favorite_border", "share", "more_horiz", "delete", "done")
            .forEach { assertNotNull(it, materialIcon(it)) }
    }

    @Test
    fun unknownKeyIsNull() {
        assertNull(materialIcon("nope"))
    }
}
