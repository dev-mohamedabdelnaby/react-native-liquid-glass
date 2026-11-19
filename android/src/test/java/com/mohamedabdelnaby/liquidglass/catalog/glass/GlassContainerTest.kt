package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.MonotonicFrameClock
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

    @Test
    fun glassOfTheFirstLayoutStartsInPlaceAndLaterGlassGrowsOutOfItsNeighbour() {
        val rect = Rect(100f, 0f, 160f, 60f)
        val neighbour = Rect(0f, 0f, 60f, 60f)
        val fresh = GlassMember()

        // On the container's first layout nothing animates in, whatever the transition.
        assertEquals(rect, morphStart(fresh, rect, firstLayout = true) { neighbour })
        fresh.transition = GlassTransition.Materialize
        assertEquals(rect, morphStart(fresh, rect, firstLayout = true) { neighbour })

        // Later, matched glass grows out of its neighbour (or its own centre), other glass from its centre.
        fresh.transition = GlassTransition.MatchedGeometry
        assertEquals(neighbour, morphStart(fresh, rect, firstLayout = false) { neighbour })
        assertEquals(Rect(rect.center, rect.center), morphStart(fresh, rect, firstLayout = false) { null })
        fresh.transition = GlassTransition.Materialize
        assertEquals(Rect(rect.center, rect.center), morphStart(fresh, rect, firstLayout = false) { neighbour })

        // Glass already placed starts where it is drawn.
        val placed = member(Rect(0f, 0f, 60f, 60f))
        assertEquals(placed.displayed, morphStart(placed, rect, firstLayout = true) { neighbour })
    }

    /** A box at [origin] in the container's space; the container is the one at zero. */
    private class Box(val origin: Offset, private val side: Int) : LayoutCoordinates {
        override val size = IntSize(side, side)
        override val providedAlignmentLines = emptySet<AlignmentLine>()
        override val parentLayoutCoordinates: LayoutCoordinates? = null
        override val parentCoordinates: LayoutCoordinates? = null
        override val isAttached = true
        override fun windowToLocal(relativeToWindow: Offset) = relativeToWindow - origin
        override fun localToWindow(relativeToLocal: Offset) = relativeToLocal + origin
        override fun localToRoot(relativeToLocal: Offset) = relativeToLocal + origin
        override fun localPositionOf(sourceCoordinates: LayoutCoordinates, relativeToSource: Offset) =
            (sourceCoordinates as Box).origin + relativeToSource - origin
        override fun localBoundingBoxOf(sourceCoordinates: LayoutCoordinates, clipBounds: Boolean) =
            Rect(localPositionOf(sourceCoordinates, Offset.Zero), sourceCoordinates.size.toSize())
        override fun get(alignmentLine: AlignmentLine) = AlignmentLine.Unspecified
    }

    /** Frames on demand. */
    private class Clock : MonotonicFrameClock {
        private val waiting = mutableListOf<CancellableContinuation<Long>>()
        private var now = 0L
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R =
            onFrame(suspendCancellableCoroutine { waiting += it })
        fun frame() {
            now += 16_666_667L
            waiting.toList().also { waiting.clear() }.forEach { it.resume(now) }
        }
    }

    @Test
    fun shapesPlacedOneByOneInTheFirstLayoutAllStartInPlaceAndLaterOnesGrow() {
        val clock = Clock()
        val job = Job()
        val state = GlassContainerState(CoroutineScope(Dispatchers.Unconfined + clock + job))
        state.coordinates = Box(Offset.Zero, 400)
        val shapes = List(3) { GlassMember() }
        shapes.forEach { state.members += it }

        // Compose positions the shapes one after another in one pass, each syncing the container.
        shapes.forEachIndexed { i, shape ->
            shape.coordinates = Box(Offset(100f * i, 0f), 60)
            state.sync()
        }
        shapes.forEachIndexed { i, shape ->
            assertEquals("shape $i", Rect(100f * i, 0f, 100f * i + 60f, 60f), shape.displayed)
        }

        // A frame later, new glass grows out of the nearest shape (matched geometry).
        clock.frame()
        val later = GlassMember().apply { coordinates = Box(Offset(300f, 0f), 60) }
        state.members += later
        state.sync()
        assertEquals(Rect(200f, 0f, 260f, 60f), later.displayed)
        job.cancel()
    }
}
