package com.mohamedabdelnaby.reactnativeliquidglass

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.ReactNativeLiquidGlassViewManagerInterface
import com.facebook.react.viewmanagers.ReactNativeLiquidGlassViewManagerDelegate

@ReactModule(name = ReactNativeLiquidGlassViewManager.NAME)
class ReactNativeLiquidGlassViewManager : SimpleViewManager<ReactNativeLiquidGlassView>(),
  ReactNativeLiquidGlassViewManagerInterface<ReactNativeLiquidGlassView> {
  private val mDelegate: ViewManagerDelegate<ReactNativeLiquidGlassView>

  init {
    mDelegate = ReactNativeLiquidGlassViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<ReactNativeLiquidGlassView>? {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): ReactNativeLiquidGlassView {
    return ReactNativeLiquidGlassView(context)
  }

  @ReactProp(name = "color")
  override fun setColor(view: ReactNativeLiquidGlassView?, color: Int?) {
    view?.setBackgroundColor(color ?: Color.TRANSPARENT)
  }

  companion object {
    const val NAME = "ReactNativeLiquidGlassView"
  }
}
