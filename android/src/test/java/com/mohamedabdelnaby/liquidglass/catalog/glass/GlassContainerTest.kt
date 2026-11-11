package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class GlassContainerTest {
    private fun member(rect: Rect, unionId: Any? = null) = GlassMember().apply {
        this.rect = rect
        this.unionId = unionId
        placed = true
    }

    @Test
    fun unionMembersDrawAsOneShapeCoveringThemAll() {
        val state = GlassContainerState(CoroutineScope(Dispatchers.Unconfined))
        state.members += member(Rect(0f, 0f, 40f, 40f), unionId = "day")
        state.members += member(Rect(50f, 0f, 90f, 40f), unionId = "day")
        state.members += member(Rect(120f, 0f, 160f, 40f))

        val shapes = state.shapes(offset = 10f, density = Density(1f))

        assertEquals(2, shapes.size)
        val union = shapes[0]
        assertEquals(10f, union.left)
        assertEquals(90f, union.width)
        assertEquals(40f, union.height)
        // A capsule: half the union's short side.
        assertEquals(20f, union.radius)
        assertEquals(130f, shapes[1].left)
    }
}
