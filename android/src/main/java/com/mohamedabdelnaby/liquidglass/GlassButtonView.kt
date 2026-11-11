package com.mohamedabdelnaby.liquidglass

import android.content.Context
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.mohamedabdelnaby.liquidglass.engine.glass.GlassButton
import com.mohamedabdelnaby.liquidglass.engine.glass.GlassCircle
import com.mohamedabdelnaby.liquidglass.engine.glass.GlassControlSize
import com.mohamedabdelnaby.liquidglass.engine.glass.GlassProminentButton
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.engine.materialIcon
import com.mohamedabdelnaby.liquidglass.host.ComposeHost

/** The props of the native glass button; [glass] holds the shared glass props. */
internal data class GlassButtonProps(
  val glass: GlassViewProps = GlassViewProps(),
  val title: String? = null,
  val androidIcon: String? = null,
  val prominent: Boolean = false,
  val size: String = "regular",
  val disabled: Boolean = false,
  val label: String? = null,
) {
  // Icon only: the engine's circle, which drops the label padding that would squeeze the icon.
  fun shape(): RoundedCornerShape = if (title.isNullOrEmpty()) GlassCircle else glass.shape()
}

/** A React view with no React children: the engine's glass button fills its bounds. */
internal class GlassButtonView(context: Context) : ComposeHost(context) {

  private var props by mutableStateOf(GlassButtonProps())

  init {
    val backdrop = createBackdrop()
    addComposeView(accessible = true) {
      val current = props
      val (glass, settings) = remember(current.glass) { current.glass.toEngine() }
      val size = when (current.size) {
        "small" -> GlassControlSize.Small
        "large" -> GlassControlSize.Large
        else -> GlassControlSize.Regular
      }
      val label = current.label
      val modifier = Modifier.fillMaxSize().let { m ->
        if (label.isNullOrEmpty()) m else m.semantics { contentDescription = label }
      }
      val content: @Composable RowScope.() -> Unit = {
        materialIcon(current.androidIcon.orEmpty())?.let { Icon(it, null, Modifier.size(20.dp)) }
        current.title?.takeIf { it.isNotEmpty() }?.let { Text(it) }
      }
      CompositionLocalProvider(
        LocalGlassBackdrop provides backdrop,
        LocalGlassSettings provides settings,
      ) {
        if (current.prominent) {
          GlassProminentButton(
            onClick = ::emitPress,
            modifier = modifier,
            tint = glass.tint ?: MaterialTheme.colorScheme.primary,
            shape = current.shape(),
            size = size,
            enabled = !current.disabled,
            content = content,
          )
        } else {
          GlassButton(
            onClick = ::emitPress,
            modifier = modifier,
            glass = glass,
            shape = current.shape(),
            size = size,
            enabled = !current.disabled,
            content = content,
          )
        }
      }
    }
  }

  fun update(change: GlassButtonProps.() -> GlassButtonProps) {
    props = props.change()
  }

  private fun emitPress() {
    val reactContext = context as? ReactContext ?: return
    UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
      ?.dispatchEvent(PressEvent(UIManagerHelper.getSurfaceId(reactContext), id))
  }

  private class PressEvent(surfaceId: Int, viewId: Int) : Event<PressEvent>(surfaceId, viewId) {
    override fun getEventName() = EVENT_NAME

    override fun getEventData() = Arguments.createMap()
  }

  companion object {
    const val EVENT_NAME = "topButtonPress"
  }
}
