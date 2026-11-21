package com.mohamedabdelnaby.liquidglass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.LiquidGlassScreenManagerDelegate
import com.facebook.react.viewmanagers.LiquidGlassScreenManagerInterface
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSettings

/** The hosted screen has no React children, so a [SimpleViewManager] is enough. */
@ReactModule(name = LiquidGlassScreenManager.NAME)
internal class LiquidGlassScreenManager :
  SimpleViewManager<LiquidGlassScreenView>(),
  LiquidGlassScreenManagerInterface<LiquidGlassScreenView> {

  private val delegate = LiquidGlassScreenManagerDelegate(this)

  override fun getName(): String = NAME

  override fun getDelegate(): ViewManagerDelegate<LiquidGlassScreenView> = delegate

  override fun createViewInstance(context: ThemedReactContext) = LiquidGlassScreenView(context)

  override fun onDropViewInstance(view: LiquidGlassScreenView) {
    view.dispose()
    super.onDropViewInstance(view)
  }

  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> =
    (super.getExportedCustomDirectEventTypeConstants() ?: emptyMap()) + mapOf(
      LiquidGlassScreenView.EVENT_BACK to mapOf("registrationName" to "onBack"),
      LiquidGlassScreenView.EVENT_OPEN to mapOf("registrationName" to "onOpen"),
      LiquidGlassScreenView.EVENT_SETTINGS_CHANGE to mapOf("registrationName" to "onSettingsChange"),
    )

  override fun setScreen(view: LiquidGlassScreenView, value: String?) {
    view.screen = value
  }

  override fun setAppearance(view: LiquidGlassScreenView, value: String?) {
    view.mode = appearanceMode(value)
  }

  override fun setLayoutDirection(view: LiquidGlassScreenView, value: String?) {
    view.jsLayoutDirection = value ?: "system"
  }

  override fun setAndroidStyle(view: LiquidGlassScreenView, value: String?) {
    view.androidStyle = androidStyleOf(value ?: "glass")
  }

  override fun setAndroidSegmentedCheck(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(segmentedCheck = value) }

  override fun setTintColor(view: LiquidGlassScreenView, value: Int?) = update(view) { copy(tint = value?.let { Color(it) }) }

  override fun setClear(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(clear = value) }

  override fun setBlur(view: LiquidGlassScreenView, value: Float) = update(view) { copy(blur = value.dp) }

  override fun setWhite(view: LiquidGlassScreenView, value: Float) = update(view) { copy(white = value) }

  override fun setBlack(view: LiquidGlassScreenView, value: Float) = update(view) { copy(black = value) }

  override fun setEdgeLight(view: LiquidGlassScreenView, value: Float) = update(view) { copy(edgeLight = value) }

  override fun setRefractionHeight(view: LiquidGlassScreenView, value: Float) = update(view) { copy(refractionHeight = value.dp) }

  override fun setRefractionAmount(view: LiquidGlassScreenView, value: Float) = update(view) { copy(refractionAmount = value.dp) }

  override fun setOpaque(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(opaque = value) }

  override fun setLiquid(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(liquid = value) }

  override fun setDropColor(view: LiquidGlassScreenView, value: Int?) = update(view) { copy(dropColor = value?.let { Color(it) }) }

  override fun setSliderLiquid(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(sliderLiquid = value) }

  override fun setSwitchLiquid(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(switchLiquid = value) }

  override fun setSegmentedLiquid(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(segmentedLiquid = value) }

  override fun setTabBarLiquid(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(tabBarLiquid = value) }

  override fun setTabBarDrop(view: LiquidGlassScreenView, value: Boolean) = update(view) { copy(tabBarDrop = value) }

  override fun setFontRegular(view: LiquidGlassScreenView, value: String?) = faces(view) { copy(regular = value.orEmpty()) }

  override fun setFontMedium(view: LiquidGlassScreenView, value: String?) = faces(view) { copy(medium = value.orEmpty()) }

  override fun setFontSemibold(view: LiquidGlassScreenView, value: String?) = faces(view) { copy(semibold = value.orEmpty()) }

  override fun setFontBold(view: LiquidGlassScreenView, value: String?) = faces(view) { copy(bold = value.orEmpty()) }

  override fun setFontHeavy(view: LiquidGlassScreenView, value: String?) = faces(view) { copy(heavy = value.orEmpty()) }

  private fun faces(view: LiquidGlassScreenView, change: FontFaces.() -> FontFaces) {
    view.faces = view.faces.change()
  }

  private fun update(view: LiquidGlassScreenView, change: GlassSettings.() -> GlassSettings) {
    view.settings = view.settings.change()
  }

  companion object {
    const val NAME = "LiquidGlassScreen"
  }
}
