package com.mohamedabdelnaby.liquidglass

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

/** The `font*` props: the face for each weight, as React Native's `fontFamily` names it; `""` is not set. */
data class FontFaces(
  val regular: String = "",
  val medium: String = "",
  val semibold: String = "",
  val bold: String = "",
  val heavy: String = "",
)

/** The face [name] for [weight]; [loader] gets its typeface (React Native's font manager in the app). */
class NamedFont(
  val name: String,
  override val weight: FontWeight,
  private val loader: () -> Typeface?,
) : AndroidFont(FontLoadingStrategy.Blocking, Loader, FontVariation.Settings()) {
  override val style = FontStyle.Normal

  fun load(): Typeface? = loader()

  private object Loader : TypefaceLoader {
    override fun loadBlocking(context: Context, font: AndroidFont) = (font as NamedFont).load()

    override suspend fun awaitLoad(context: Context, font: AndroidFont) = (font as NamedFont).load()
  }
}

/**
 * The screens' font family: each weight's face, a missing weight takes the regular face (Compose
 * picks the nearest weight when there is no regular either). Null when no face is set: the system font.
 *
 * Loaded as a React Native `Text` would get it: one name for every weight is a family, loaded with
 * the weight (`fontFamily` + `fontWeight`); a weight map's faces are fonts of their own, loaded
 * at the normal weight (`fontFamily` alone, as an app's `AppText` does).
 */
fun fontFamilyOf(faces: FontFaces, load: (name: String, weight: FontWeight) -> Typeface?): FontFamily? {
  val fonts = with(faces) {
    val family = setOf(regular, medium, semibold, bold, heavy).size == 1
    listOf(FontWeight.W400 to regular, FontWeight.W500 to medium, FontWeight.W600 to semibold, FontWeight.W700 to bold, FontWeight.W800 to heavy)
      .mapNotNull { (weight, name) ->
        name.ifEmpty { regular }.takeIf { it.isNotEmpty() }
          ?.let { face -> NamedFont(face, weight) { load(face, if (family) weight else FontWeight.Normal) } }
      }
  }
  return if (fonts.isEmpty()) null else FontFamily(fonts)
}
