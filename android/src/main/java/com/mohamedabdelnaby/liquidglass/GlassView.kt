package com.mohamedabdelnaby.liquidglass

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.engine.glass.LocalGlassSettings
import com.mohamedabdelnaby.liquidglass.engine.glass.glassEffect
import com.mohamedabdelnaby.liquidglass.host.ComposeHost

/** A React view group that draws a glass surface under its React children. */
internal class GlassView(context: Context) : ComposeHost(context) {

  private var props by mutableStateOf(GlassViewProps())

  init {
    val backdrop = createBackdrop()
    addComposeView {
      val current = props
      val (glass, settings) = remember(current) { current.toEngine() }
      CompositionLocalProvider(
        LocalGlassBackdrop provides backdrop,
        LocalGlassSettings provides settings,
      ) {
        Box(Modifier.fillMaxSize().glassEffect(glass, current.shape()))
      }
    }
  }

  fun update(change: GlassViewProps.() -> GlassViewProps) {
    props = props.change()
  }
}
