package com.mohamedabdelnaby.liquidglass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mohamedabdelnaby.liquidglass.engine.glass.CapsuleShape
import com.mohamedabdelnaby.liquidglass.engine.glass.Glass
import com.mohamedabdelnaby.liquidglass.engine.glass.GlassSettings

/** The props of the native glass view. JS resolves every default and precedence, so values here are final. */
internal data class GlassViewProps(
  val glassStyle: String = "regular",
  val tintColor: Int? = null,
  val interactive: Boolean = false,
  val opaque: Boolean = false,
  val liquid: Boolean = true,
  val blur: Float = 3f,
  val white: Float = 0.25f,
  val black: Float = 0.05f,
  val edgeLight: Float = 0.15f,
  val refractionHeight: Float = 14f,
  val refractionAmount: Float = 24f,
  val shapeKind: String = "capsule",
  val cornerRadius: Float = 0f,
) {
  /**
   * The values go on the glass itself (ownVariant keeps its style), and the same ones form the
   * settings the engine merges them with, so nothing of the app-wide defaults leaks in.
   */
  fun toEngine(): Pair<Glass, GlassSettings> {
    val tint = tintColor?.let { Color(it) }
    val glass = Glass(
      variant = if (glassStyle == "clear") Glass.Variant.Clear else Glass.Variant.Regular,
      tint = tint,
      interactive = interactive,
      blur = blur.dp,
      refractionHeight = refractionHeight.dp,
      refractionAmount = refractionAmount.dp,
      white = white,
      black = black,
      edgeLight = edgeLight,
      ownVariant = true,
      opaque = opaque,
      liquid = liquid,
    )
    val settings = GlassSettings(
      clear = false,
      blur = blur.dp,
      white = white,
      black = black,
      edgeLight = edgeLight,
      refractionHeight = refractionHeight.dp,
      refractionAmount = refractionAmount.dp,
      tint = tint,
      opaque = opaque,
      liquid = liquid,
    )
    return glass to settings
  }

  // "circle" is a capsule too: the engine's capsule is a 50% rounded rect.
  fun shape(): RoundedCornerShape =
    if (shapeKind == "rect") RoundedCornerShape(cornerRadius.dp) else CapsuleShape
}
