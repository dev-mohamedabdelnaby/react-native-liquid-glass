package com.mohamedabdelnaby.liquidglass.engine

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class GlassThemeTest {
    @Test
    fun primaryIsTheCatalogBlueNotMaterialPurple() {
        assertEquals(SystemColors.Blue, glassColors(dark = false).primary)
        assertEquals(Color(0xFF0A84FF), glassColors(dark = true).primary)
    }

    @Test
    fun labelsFollowTheSystemAppearance() {
        assertEquals(Color.Black, glassColors(dark = false).onSurface)
        assertEquals(Color.White, glassColors(dark = true).onSurface)
    }

    @Test
    fun buttonTextUsesTheIosBodyStyle() {
        // Material's default body style is 16 sp with +0.5 sp tracking, which made labels wider
        // than the space the app gave them.
        assertEquals(-0.43f, GlassTypography.bodyLarge.letterSpacing.value, 0.001f)
    }
}
