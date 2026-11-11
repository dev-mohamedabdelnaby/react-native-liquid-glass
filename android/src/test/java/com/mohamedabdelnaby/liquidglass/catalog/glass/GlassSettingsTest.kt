package com.mohamedabdelnaby.liquidglass.catalog.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The white/black wash matches react-native-liquid's `whiteBlackColor`. */
class GlassSettingsTest {
    @Test
    fun washes() {
        assertNull(whiteBlackWash(0f, 0f))

        val white = whiteBlackWash(1f, 0f)!!
        assertEquals(1f, white.red, 0.005f)
        assertEquals(0.7f, white.alpha, 0.005f)

        val black = whiteBlackWash(0f, 1f)!!
        assertEquals(0f, black.red, 0.005f)
        assertEquals(0.7f, black.alpha, 0.005f)

        // Both together make a grey: alpha 1 - 0.3 * 0.3, grey 0.3 * 0.7 / alpha.
        val grey = whiteBlackWash(1f, 1f)!!
        assertEquals(0.91f, grey.alpha, 0.005f)
        assertEquals(0.21f / 0.91f, grey.red, 0.005f)
    }
}
