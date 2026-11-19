package com.mohamedabdelnaby.liquidglass.host

import android.content.Context
import android.view.Window
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.LifecycleOwner
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.findViewTreeNavigationEventDispatcherOwner
import com.facebook.react.bridge.ReactContext
import com.facebook.react.views.view.ReactViewGroup

/**
 * A React view (no React children) whose one ComposeView fills its bounds. The composition lives
 * as long as the React view, so its state survives React detaching and re-attaching the view.
 */
internal open class ComposeHost(context: Context) : ReactViewGroup(context) {

  private val lifecycleOwner = GlassLifecycleOwner()
  // Null when the base constructor already calls requestLayout().
  private var composeView: ComposeView? = null
  // The screen's BackHandlers get the back key before React Native pops the screen.
  private val back = BackRouter()
  private var keyWindow: Window? = null

  protected fun setContent(content: @Composable () -> Unit) {
    val navigationOwner = object : NavigationEventDispatcherOwner {
      override val navigationEventDispatcher = back.navigation
    }
    val onBackPressedOwner = object : OnBackPressedDispatcherOwner, LifecycleOwner by lifecycleOwner {
      override val onBackPressedDispatcher = back.onBackPressed
    }
    composeView = ComposeView(context).apply {
      lifecycleOwner.install(this)
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        CompositionLocalProvider(
          LocalNavigationEventDispatcherOwner provides navigationOwner,
          LocalOnBackPressedDispatcherOwner provides onBackPressedOwner,
          content = content,
        )
      }
    }.also { addView(it) }
  }

  override fun onAttachedToWindow() {
    lifecycleOwner.onAttached()
    super.onAttachedToWindow()
    findViewTreeNavigationEventDispatcherOwner()?.let { back.attach(it.navigationEventDispatcher) }
    enterKeyWindow()
    // A layout made before the window came (native-stack lays out a pushed screen first) was skipped.
    requestLayout()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    leaveBack()
    lifecycleOwner.onDetached()
  }

  private var layoutPosted = false
  private val layoutRunnable = Runnable {
    layoutPosted = false
    enterKeyWindow()
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
    // Measured outside a window, a ComposeView throws: it has no recomposer yet.
    val view = composeView?.takeIf { it.isAttachedToWindow } ?: return
    view.measure(
      MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
    )
    view.layout(0, 0, width, height)
  }

  // React's current activity can still be null on attach: the next posted layout tries again.
  private fun enterKeyWindow() {
    if (keyWindow != null || !isAttachedToWindow) return
    keyWindow = (context as? ReactContext)?.currentActivity?.window?.takeIf { it.decorView === rootView }
    keyWindow?.let { BackKey.add(it, back) }
  }

  private fun leaveBack() {
    back.detach()
    keyWindow?.let { BackKey.remove(it, back) }
    keyWindow = null
  }

  /** Releases the composition. Called when React drops the view. */
  open fun dispose() {
    leaveBack()
    lifecycleOwner.destroy()
  }
}
