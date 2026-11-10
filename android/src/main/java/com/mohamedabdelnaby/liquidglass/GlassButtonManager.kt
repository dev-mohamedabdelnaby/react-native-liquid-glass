package com.mohamedabdelnaby.liquidglass

import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.RNGlassButtonManagerDelegate
import com.facebook.react.viewmanagers.RNGlassButtonManagerInterface
import com.facebook.react.views.view.ReactViewGroup

/** The glass button has no React children, so a [SimpleViewManager] is enough. */
@ReactModule(name = GlassButtonManager.NAME)
class GlassButtonManager : SimpleViewManager<ReactViewGroup>(), RNGlassButtonManagerInterface<ReactViewGroup> {

  private val delegate = RNGlassButtonManagerDelegate(this)

  override fun getName(): String = NAME

  override fun getDelegate(): ViewManagerDelegate<ReactViewGroup> = delegate

  override fun createViewInstance(context: ThemedReactContext): ReactViewGroup = GlassButtonView(context)

  override fun onDropViewInstance(view: ReactViewGroup) {
    (view as? GlassButtonView)?.dispose()
    super.onDropViewInstance(view)
  }

  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> =
    (super.getExportedCustomDirectEventTypeConstants() ?: emptyMap()) +
      (GlassButtonView.EVENT_NAME to mapOf("registrationName" to "onButtonPress"))

  // The label goes on the Compose button, the accessible element, not on the host view.
  override fun setAccessibilityLabel(view: ReactViewGroup, accessibilityLabel: String?) =
    update(view) { copy(label = accessibilityLabel) }

  override fun setTitle(view: ReactViewGroup, value: String?) = update(view) { copy(title = value) }

  override fun setSystemImage(view: ReactViewGroup, value: String?) = Unit

  override fun setAndroidIcon(view: ReactViewGroup, value: String?) = update(view) { copy(androidIcon = value) }

  override fun setProminent(view: ReactViewGroup, value: Boolean) = update(view) { copy(prominent = value) }

  override fun setSize(view: ReactViewGroup, value: String?) = update(view) { copy(size = value ?: "regular") }

  override fun setDisabled(view: ReactViewGroup, value: Boolean) = update(view) { copy(disabled = value) }

  override fun setGlassStyle(view: ReactViewGroup, value: String?) = glass(view) { copy(glassStyle = value ?: "regular") }

  override fun setTintColor(view: ReactViewGroup, value: Int?) = glass(view) { copy(tintColor = value) }

  override fun setInteractive(view: ReactViewGroup, value: Boolean) = glass(view) { copy(interactive = value) }

  override fun setOpaque(view: ReactViewGroup, value: Boolean) = glass(view) { copy(opaque = value) }

  override fun setLiquid(view: ReactViewGroup, value: Boolean) = glass(view) { copy(liquid = value) }

  override fun setBlur(view: ReactViewGroup, value: Float) = glass(view) { copy(blur = value) }

  override fun setWhite(view: ReactViewGroup, value: Float) = glass(view) { copy(white = value) }

  override fun setBlack(view: ReactViewGroup, value: Float) = glass(view) { copy(black = value) }

  override fun setEdgeLight(view: ReactViewGroup, value: Float) = glass(view) { copy(edgeLight = value) }

  override fun setRefractionHeight(view: ReactViewGroup, value: Float) = glass(view) { copy(refractionHeight = value) }

  override fun setRefractionAmount(view: ReactViewGroup, value: Float) = glass(view) { copy(refractionAmount = value) }

  override fun setShapeKind(view: ReactViewGroup, value: String?) = glass(view) { copy(shapeKind = value ?: "capsule") }

  override fun setCornerRadius(view: ReactViewGroup, value: Float) = glass(view) { copy(cornerRadius = value) }

  private fun update(view: ReactViewGroup, change: GlassButtonProps.() -> GlassButtonProps) {
    (view as? GlassButtonView)?.update(change)
  }

  private fun glass(view: ReactViewGroup, change: GlassViewProps.() -> GlassViewProps) =
    update(view) { copy(glass = glass.change()) }

  companion object {
    const val NAME = "RNGlassButton"
  }
}
