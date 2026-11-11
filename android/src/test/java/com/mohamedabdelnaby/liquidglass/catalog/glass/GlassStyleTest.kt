package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/** An item's own values win over Home's general Glass settings; unset ones follow them. */
class GlassStyleTest {
    private val general = GlassSettings(clear = true, blur = 3.dp, white = 0.25f, black = 0.05f, edgeLight = 0.15f)

    @Test
    fun followsGeneral() {
        val styled = Glass.Regular.styled(general)
        assertEquals(Glass.Variant.Clear, styled.variant)
        assertEquals(3.dp, styled.blur)
        assertEquals(0.15f, styled.edgeLight)
        assertEquals(whiteBlackWash(0.25f, 0.05f), styled.tint)
    }

    @Test
    fun itemWins() {
        val styled = Glass.Regular.copy(ownVariant = true, blur = 9.dp, white = 0.5f, black = 0f, edgeLight = 0.6f).styled(general)
        assertEquals(Glass.Variant.Regular, styled.variant)
        assertEquals(9.dp, styled.blur)
        assertEquals(0.6f, styled.edgeLight)
        assertEquals(whiteBlackWash(0.5f, 0f), styled.tint)
    }

    @Test
    fun tintedKeepsItsLook() {
        val styled = Glass.Regular.tint(Color.Blue).styled(general)
        assertEquals(Glass.Variant.Regular, styled.variant)
        assertEquals(Color.Blue, styled.tint)
    }

    @Test
    fun opaqueFillsSolid() {
        val opaque = general.copy(opaque = true, tint = Color.Red.copy(alpha = 0.35f))
        // The general tint at full strength, and an item's own tint wins.
        assertEquals(Color.Red, Glass.Regular.styled(opaque).tint)
        assertEquals(Color.Blue, Glass.Regular.tint(Color.Blue.copy(alpha = 0.5f)).styled(opaque).tint)
        // No tint at all: the surface color fills it.
        assertEquals(null, Glass.Regular.styled(general.copy(opaque = true)).tint)
        assertEquals(Color.White, opaqueFill(null, dark = false))
    }
}
