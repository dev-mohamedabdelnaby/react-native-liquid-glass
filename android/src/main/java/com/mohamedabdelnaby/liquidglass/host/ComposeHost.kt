package com.mohamedabdelnaby.liquidglass.host

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.facebook.react.views.view.ReactViewGroup

/**
 * A React view (no React children) whose one ComposeView fills its bounds. The composition lives
 * as long as the React view, so its state survives React detaching and re-attaching the view.
 */
internal open class ComposeHost(context: Context) : ReactViewGroup(context) {

  private val lifecycleOwner = GlassLifecycleOwner()
  // Null when the base constructor already calls requestLayout().
  private var composeView: ComposeView? = null

  protected fun setContent(content: @Composable () -> Unit) {
    composeView = ComposeView(context).apply {
      lifecycleOwner.install(this)
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent(content)
    }.also { addView(it) }
  }

  override fun onAttachedToWindow() {
    lifecycleOwner.onAttached()
    super.onAttachedToWindow()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    lifecycleOwner.onDetached()
  }

  private var layoutPosted = false
  private val layoutRunnable = Runnable {
    layoutPosted = false
    layoutComposeView()
    // Compose lays out and places its content while drawing; make sure that draw happens.
    composeView?.invalidate()
  }

  /**
   * React views ignore layout requests because React lays them out. Compose content requests
   * layouts while it animates, so those are handled here.
   */
  override fun requestLayout() {
    super.requestLayout()
    if (!layoutPosted && composeView != null) {
      layoutPosted = true
      postOnAnimation(layoutRunnable)
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    layoutComposeView()
  }

  /** React lays out the host; the ComposeView is ours to size. */
  private fun layoutComposeView() {
    val view = composeView ?: return
    view.measure(
      MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
    )
    view.layout(0, 0, width, height)
  }

  /** Releases the composition. Called when React drops the view. */
  open fun dispose() {
    lifecycleOwner.destroy()
  }
}
