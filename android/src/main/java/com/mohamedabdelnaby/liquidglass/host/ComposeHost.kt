package com.mohamedabdelnaby.liquidglass.host

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.facebook.react.uimanager.PointerEvents
import com.facebook.react.uimanager.ReactPointerEventsView
import com.facebook.react.views.view.ReactViewGroup
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.Backdrop
import com.mohamedabdelnaby.liquidglass.engine.liquid.backdrop.backdrops.emptyBackdrop

/**
 * A React view group that renders Jetpack Compose below its React children.
 *
 * The ComposeViews are private children kept at the start of the child list; the view manager
 * hides them from React by offsetting child indices by [leading].
 */
internal open class ComposeHost(context: Context) : ReactViewGroup(context) {

  private val lifecycleOwner = GlassLifecycleOwner()
  // What is laid out over the host: the frames wrapping the ComposeViews.
  private val hostedViews = ArrayList<View>()
  // Primitive so it is valid (0) when the base constructor already calls requestLayout().
  private var composeViewCount = 0
  private val backdrops = ArrayList<ViewTreeBackdrop>()
  private var observedViewTree: ViewTreeObserver? = null

  var leading = 0
    private set

  private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
    // Backdrops exist only from API 31 (see createBackdrop); the check also satisfies lint.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      for (backdrop in backdrops) {
        backdrop.record()
      }
    }
    true
  }

  init {
    clipChildren = false
  }

  /** Adds a ComposeView below the React children; [accessible] exposes its semantics to TalkBack. */
  protected fun addComposeView(accessible: Boolean = false, content: @Composable () -> Unit) {
    // React detaches and reattaches views when an ancestor changes (a navigator pushing or
    // popping a screen, a tab switch). After that, Compose stops re-reading the graphicsLayer
    // blocks of the content, so animated layers freeze. Rebuilding the content on reattach
    // restores them.
    val attachGeneration = mutableIntStateOf(0)
    val view = ComposeView(context).apply {
      lifecycleOwner.install(this)
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      clipChildren = false
      if (!accessible) importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
      setContent { key(attachGeneration.intValue) { content() } }
      var attachedBefore = false
      addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
          if (attachedBefore) {
            attachGeneration.intValue++
          }
          attachedBefore = true
          layoutComposeViews()
        }

        override fun onViewDetachedFromWindow(view: View) = Unit
      })
    }
    // React never picks the ComposeView as a touch target, so JS touchables above it get their
    // presses; Compose still gets the native touches it handles.
    val frame = NoReactTouchFrame(context).apply {
      clipChildren = false
      addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }
    addView(frame, leading)
    leading++
    hostedViews.add(frame)
    composeViewCount++
  }

  /**
   * A backdrop that samples what is drawn behind this view, refreshed before every frame. Before
   * API 31 (no RenderEffect: the glass is a plain frost) it is empty, so nothing records.
   */
  protected fun createBackdrop(): Backdrop {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
      return emptyBackdrop()
    }
    return ViewTreeBackdrop(this).also { backdrops.add(it) }
  }

  override fun onAttachedToWindow() {
    lifecycleOwner.onAttached()
    super.onAttachedToWindow()
    if (backdrops.isNotEmpty()) {
      observedViewTree = viewTreeObserver.also { it.addOnPreDrawListener(preDrawListener) }
    }
  }

  override fun onDetachedFromWindow() {
    observedViewTree?.takeIf { it.isAlive }?.removeOnPreDrawListener(preDrawListener)
    observedViewTree = null
    super.onDetachedFromWindow()
    lifecycleOwner.onDetached()
  }

  private var layoutPosted = false
  private val layoutRunnable = Runnable {
    layoutPosted = false
    layoutComposeViews()
    // Compose lays out and places its content while drawing; make sure that draw happens.
    for (frame in hostedViews) {
      frame.invalidate()
      (frame as? NoReactTouchFrame)?.getChildAt(0)?.invalidate()
    }
  }

  /**
   * React views ignore layout requests because React lays them out. Compose content requests
   * layouts while it animates, so those are handled here.
   */
  override fun requestLayout() {
    super.requestLayout()
    if (!layoutPosted && composeViewCount > 0) {
      layoutPosted = true
      postOnAnimation(layoutRunnable)
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    layoutComposeViews()
  }

  /** React lays out its own children; the ComposeViews are ours to size (once attached). */
  private fun layoutComposeViews() {
    for (frame in hostedViews) {
      if (!frame.isAttachedToWindow) {
        continue
      }
      frame.measure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
      )
      frame.layout(0, 0, width, height)
    }
  }

  /** Releases compositions and backdrops. Called when React drops the view. */
  fun dispose() {
    lifecycleOwner.destroy()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      backdrops.forEach { it.discard() }
    }
  }
}

/** Holds a ComposeView that React's touch targeting skips. */
private class NoReactTouchFrame(context: Context) :
  android.widget.FrameLayout(context),
  ReactPointerEventsView {
  override val pointerEvents: PointerEvents
    get() = PointerEvents.NONE
}
