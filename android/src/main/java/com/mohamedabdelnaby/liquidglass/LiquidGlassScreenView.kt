package com.mohamedabdelnaby.liquidglass

import android.content.Context
import android.view.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.i18nmanager.I18nUtil
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.mohamedabdelnaby.liquidglass.catalog.GlassComponent
import com.mohamedabdelnaby.liquidglass.catalog.HomeScreen
import com.mohamedabdelnaby.liquidglass.catalog.glass.AndroidStyle
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalAndroidStyle
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.glass.GlassWindowBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassWindowBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalOnBack
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme
import com.mohamedabdelnaby.liquidglass.host.ComposeHost
import com.mohamedabdelnaby.liquidglass.host.BarsRequest
import com.mohamedabdelnaby.liquidglass.host.SystemBarIcons

/** The catalog screen for a JS screen id (`buttons` → [GlassComponent.Buttons]); null when unknown. */
fun screenComponent(id: String): GlassComponent? =
  GlassComponent.entries.firstOrNull { it.name == id.replaceFirstChar(Char::uppercase) }

/** `'system' | 'light' | 'dark'`, as JS sends it; anything else is System. */
internal fun appearanceMode(value: String?): AppearanceMode =
  AppearanceMode.entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: AppearanceMode.System

/** `'system' | 'ltr' | 'rtl'`, as JS sends it; anything else is System (as on iOS), which follows [systemIsRtl]. */
fun layoutDirectionOf(value: String, systemIsRtl: Boolean): LayoutDirection = when (value) {
  "rtl" -> LayoutDirection.Rtl
  "ltr" -> LayoutDirection.Ltr
  else -> if (systemIsRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
}

/** `'glass' | 'material'`, as JS sends it; anything else is Glass. */
fun androidStyleOf(value: String): AndroidStyle = if (value == "material") AndroidStyle.Material else AndroidStyle.Glass

/** `'#RRGGBBAA'`, as the events send colours; `''` for none. */
internal fun Color?.toRgba(): String = this?.toArgb()?.let { "#%08X".format((it shl 8) or (it ushr 24)) } ?: ""

/**
 * One catalog screen (or Home) hosted as the Android app's `LiquidGlassApp` hosts it. The props
 * are Compose state, so a prop change recomposes the mounted screen.
 */
internal class LiquidGlassScreenView(context: Context) : ComposeHost(context) {

  var screen by mutableStateOf<String?>(null)
  var mode by mutableStateOf(AppearanceMode.System)
  var settings by mutableStateOf(GlassSettings())
  // The JS `layoutDirection` prop (not View.layoutDirection), as JS sends it.
  var jsLayoutDirection by mutableStateOf("system")
  var androidStyle by mutableStateOf(AndroidStyle.Glass)

  private val onBack: () -> Unit = { emit(EVENT_BACK, null) }

  // What this screen's appearance asks of the window's system bar icons, while it is in the React tree.
  private val bars = BarsRequest<Window>(
    claim = { window, dark -> SystemBarIcons.claim(this, window, dark) },
    release = { window -> SystemBarIcons.release(this, window) },
  )

  init {
    setContent {
      // Outside the theme: Material takes Android's own colors and type, with Home's tint as the accent.
      CompositionLocalProvider(LocalAndroidStyle provides androidStyle, LocalGlassSettings provides settings) { LiquidGlassTheme(mode) {
        // The theme's forced appearance, or the system's.
        val dark = isSystemInDarkTheme()
        SideEffect { bars.appearance(dark) }
        CompositionLocalProvider(
          LocalOnBack provides onBack,
          LocalGlassWindowBackdrop provides remember { GlassWindowBackdrop() },
          // System is React Native's own direction (I18nManager.isRTL), so it agrees with the JS screens.
          LocalLayoutDirection provides layoutDirectionOf(jsLayoutDirection, I18nUtil.instance.isRTL(context)),
        ) {
          when (val id = screen) {
            null -> Unit
            HOME -> HomeScreen(
              mode = mode,
              onModeChange = { emitSettings(it, settings, jsLayoutDirection, androidStyle) },
              onOpen = { emit(EVENT_OPEN, Arguments.createMap().apply { putString("screen", it.name.replaceFirstChar(Char::lowercase)) }) },
              settings = settings,
              onSettingsChange = { emitSettings(mode, it, jsLayoutDirection, androidStyle) },
              direction = jsLayoutDirection,
              onDirectionChange = { emitSettings(mode, settings, it, androidStyle) },
              style = androidStyle,
              onStyleChange = { emitSettings(mode, settings, jsLayoutDirection, it) },
            )
            else -> screenComponent(id)?.screen?.invoke()
          }
        }
      } }
    }
  }

  // Leaving the window keeps the request: a native-stack push or a cover detaches a screen that is
  // still mounted. It ends when React drops the view.
  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (context as? ReactContext)?.currentActivity?.window?.let(bars::entered)
  }

  override fun dispose() {
    bars.left()
    super.dispose()
  }

  // Only Home's own controls call this, never the prop setters: JS state and props would echo.
  private fun emitSettings(mode: AppearanceMode, s: GlassSettings, direction: String, style: AndroidStyle) = emit(
    EVENT_SETTINGS_CHANGE,
    Arguments.createMap().apply {
      putString("appearance", mode.name.lowercase())
      putString("layoutDirection", direction)
      putString("androidStyle", style.name.lowercase())
      putBoolean("androidSegmentedCheck", s.segmentedCheck)
      putString("tint", s.tint.toRgba())
      putBoolean("clear", s.clear)
      putDouble("blur", s.blur.value.toDouble())
      putDouble("white", s.white.toDouble())
      putDouble("black", s.black.toDouble())
      putDouble("edgeLight", s.edgeLight.toDouble())
      putDouble("refractionHeight", s.refractionHeight.value.toDouble())
      putDouble("refractionAmount", s.refractionAmount.value.toDouble())
      putBoolean("opaque", s.opaque)
      putBoolean("liquid", s.liquid)
      putString("dropColor", s.dropColor.toRgba())
      putBoolean("sliderLiquid", s.sliderLiquid)
      putBoolean("switchLiquid", s.switchLiquid)
      putBoolean("segmentedLiquid", s.segmentedLiquid)
      putBoolean("tabBarLiquid", s.tabBarLiquid)
      putBoolean("tabBarDrop", s.tabBarDrop)
    },
  )

  private fun emit(name: String, data: WritableMap?) {
    val reactContext = context as? ReactContext ?: return
    UIManagerHelper.getEventDispatcher(reactContext)
      ?.dispatchEvent(ScreenEvent(UIManagerHelper.getSurfaceId(reactContext), id, name, data))
  }

  private class ScreenEvent(surfaceId: Int, viewId: Int, private val name: String, private val data: WritableMap?) :
    Event<ScreenEvent>(surfaceId, viewId) {
    override fun getEventName() = name

    override fun getEventData() = data ?: Arguments.createMap()
  }

  companion object {
    const val HOME = "home"
    const val EVENT_BACK = "topBack"
    const val EVENT_OPEN = "topOpen"
    const val EVENT_SETTINGS_CHANGE = "topSettingsChange"
  }
}
