package com.mohamedabdelnaby.liquidglass

import android.view.View
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.RNGlassViewManagerDelegate
import com.facebook.react.viewmanagers.RNGlassViewManagerInterface
import com.facebook.react.views.view.ReactViewGroup
import com.facebook.react.views.view.ReactViewManager

/**
 * Extends [ReactViewManager] so the glass view supports every regular `View` prop (overflow,
 * borders, pointerEvents, hitSlop, ...); the glass props come from the codegen interface.
 */
@ReactModule(name = GlassViewManager.NAME)
class GlassViewManager : ReactViewManager(), RNGlassViewManagerInterface<ReactViewGroup> {

  override fun getName(): String = NAME

  override fun createViewInstance(context: ThemedReactContext): ReactViewGroup = GlassView(context)

  override fun onDropViewInstance(view: ReactViewGroup) {
    (view as? GlassView)?.dispose()
    super.onDropViewInstance(view)
  }

  override fun getDelegate(): ViewManagerDelegate<ReactViewGroup> = GlassDelegate(this, super.getDelegate())

  // Glass props go to the generated delegate, everything else to the regular View setters.
  private class GlassDelegate(
    manager: GlassViewManager,
    private val viewProps: ViewManagerDelegate<ReactViewGroup>,
  ) : RNGlassViewManagerDelegate<ReactViewGroup, GlassViewManager>(manager) {
    override fun setProperty(view: ReactViewGroup, propName: String, value: Any?) {
      if (propName in GLASS_PROPS) {
        super.setProperty(view, propName, value)
      } else {
        viewProps.setProperty(view, propName, value)
      }
    }

    override fun receiveCommand(view: ReactViewGroup, commandName: String, args: ReadableArray) =
      viewProps.receiveCommand(view, commandName, args)
  }

  // The ComposeView hosting the glass is a private child at the start: React never sees it.

  override fun addView(parent: ReactViewGroup, child: View, index: Int) {
    parent.addView(child, index + leading(parent))
  }

  override fun getChildCount(parent: ReactViewGroup): Int = parent.childCount - leading(parent)

  override fun getChildAt(parent: ReactViewGroup, index: Int): View? =
    parent.getChildAt(index + leading(parent))

  override fun removeViewAt(parent: ReactViewGroup, index: Int) {
    parent.removeViewAt(index + leading(parent))
  }

  override fun removeAllViews(parent: ReactViewGroup) {
    while (getChildCount(parent) > 0) {
      removeViewAt(parent, 0)
    }
  }

  // Clipped subviews would bypass the index offset above.
  override fun setRemoveClippedSubviews(view: ReactViewGroup, removeClippedSubviews: Boolean) = Unit

  override fun setGlassStyle(view: ReactViewGroup, value: String?) = update(view) { copy(glassStyle = value ?: "regular") }

  override fun setTintColor(view: ReactViewGroup, value: Int?) = update(view) { copy(tintColor = value) }

  override fun setInteractive(view: ReactViewGroup, value: Boolean) = update(view) { copy(interactive = value) }

  override fun setOpaque(view: ReactViewGroup, value: Boolean) = update(view) { copy(opaque = value) }

  override fun setLiquid(view: ReactViewGroup, value: Boolean) = update(view) { copy(liquid = value) }

  override fun setBlur(view: ReactViewGroup, value: Float) = update(view) { copy(blur = value) }

  override fun setWhite(view: ReactViewGroup, value: Float) = update(view) { copy(white = value) }

  override fun setBlack(view: ReactViewGroup, value: Float) = update(view) { copy(black = value) }

  override fun setEdgeLight(view: ReactViewGroup, value: Float) = update(view) { copy(edgeLight = value) }

  override fun setRefractionHeight(view: ReactViewGroup, value: Float) = update(view) { copy(refractionHeight = value) }

  override fun setRefractionAmount(view: ReactViewGroup, value: Float) = update(view) { copy(refractionAmount = value) }

  override fun setShapeKind(view: ReactViewGroup, value: String?) = update(view) { copy(shapeKind = value ?: "capsule") }

  override fun setCornerRadius(view: ReactViewGroup, value: Float) = update(view) { copy(cornerRadius = value) }

  private fun leading(parent: ReactViewGroup) = (parent as? GlassView)?.leading ?: 0

  private fun update(view: ReactViewGroup, change: GlassViewProps.() -> GlassViewProps) {
    (view as? GlassView)?.update(change)
  }

  companion object {
    const val NAME = "RNGlassView"

    private val GLASS_PROPS = setOf(
      "glassStyle", "tintColor", "interactive", "opaque", "liquid", "blur", "white", "black",
      "edgeLight", "refractionHeight", "refractionAmount", "shapeKind", "cornerRadius",
    )
  }
}
