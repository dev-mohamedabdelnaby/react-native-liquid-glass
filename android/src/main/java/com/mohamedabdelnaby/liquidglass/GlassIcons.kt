package com.mohamedabdelnaby.liquidglass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import com.caverock.androidsvg.SVG
import com.facebook.react.common.assets.ReactFontManager
import com.mohamedabdelnaby.liquidglass.catalog.glass.nativeIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/** A custom icon from the `icons` prop. */
sealed interface GlassIcon {
  data class Glyph(val glyph: String, val fontFamily: String) : GlassIcon
  data class Svg(val svg: String) : GlassIcon
  data class Image(val uri: String) : GlassIcon
}

/** The `icons` prop: JSON of SF Symbol name → `{glyph, fontFamily}` | `{svg}` | `{uri}`. Bad JSON or entries are left out. */
fun glassIconsOf(json: String): Map<String, GlassIcon> {
  val all = runCatching { JSONObject(json) }.getOrNull() ?: return emptyMap()
  return all.keys().asSequence().mapNotNull { name ->
    val o = all.optJSONObject(name) ?: return@mapNotNull null
    // Strings only: Android's org.json coerces other values, the JVM's throws; both skip them here.
    fun str(key: String) = o.opt(key) as? String
    val glyph = str("glyph")
    val fontFamily = str("fontFamily")
    val svg = str("svg")
    val uri = str("uri")
    val icon = when {
      glyph != null && fontFamily != null -> GlassIcon.Glyph(glyph, fontFamily)
      svg != null -> GlassIcon.Svg(svg)
      uri != null -> GlassIcon.Image(uri)
      else -> null
    }
    icon?.let { name to it }
  }.toMap()
}

/** The screen's custom icons, by SF Symbol name. Set by the host. */
val LocalGlassIcons = staticCompositionLocalOf<Map<String, GlassIcon>> { emptyMap() }

/**
 * The icon the iOS catalog names [sfName] in the same place: the app's custom icon when it has
 * one, else [fallback] (in Android's form while the style is Material). An icon that has not
 * loaded yet, or fails to, shows [fallback]. A custom icon mirrors in RTL when [fallback] does.
 */
@Composable
fun glassIcon(sfName: String, fallback: ImageVector): Painter {
  val nativeVector = nativeIcon(fallback)
  val native = rememberVectorPainter(nativeVector)
  val icon = LocalGlassIcons.current[sfName] ?: return native
  val context = LocalContext.current
  // Drawn once per icon, off the main thread (parsing an SVG, decoding an image), then shared.
  var drawing by remember(icon) { mutableStateOf(drawings[icon]) }
  if (drawing == null) LaunchedEffect(icon) { drawing = withContext(Dispatchers.IO) { iconDrawing(context, icon) } }
  return drawing?.let { remember(it, nativeVector.autoMirror) { GlassIconPainter(it, nativeVector.autoMirror) } } ?: native
}

/** Where a source of [srcWidth] × [srcHeight] goes in a [width] × [height] icon: fitted, centred, inside Material's 2 dp of 24 padding. */
fun iconRect(srcWidth: Float, srcHeight: Float, width: Float, height: Float): ComposeRect {
  val inset = minOf(width, height) * 2f / 24f
  val scale = minOf((width - 2 * inset) / srcWidth, (height - 2 * inset) / srcHeight)
  val w = srcWidth * scale
  val h = srcHeight * scale
  return ComposeRect((width - w) / 2, (height - h) / 2, (width + w) / 2, (height + h) / 2)
}

/** Draws an icon into a [Canvas] of the given size. */
fun interface IconDrawing {
  fun draw(canvas: Canvas, width: Float, height: Float)
}

// shortcut: drawings (parsed SVGs, downsampled bitmaps) stay for the process; add an LRU if apps pass many icons.
private val drawings = ConcurrentHashMap<GlassIcon, IconDrawing>()

/** Images are decoded no bigger than this many dp, a few times an icon's 24: enough for any icon size the catalog draws. */
private const val MAX_IMAGE_DP = 96

/** Null when the icon cannot be drawn (unknown font, bad SVG, an image that fails to load). Blocking: call off the main thread. */
private fun iconDrawing(context: Context, icon: GlassIcon): IconDrawing? = drawings[icon] ?: runCatching {
  when (icon) {
    is GlassIcon.Glyph -> glyphDrawing(icon, ReactFontManager.getInstance().getTypeface(icon.fontFamily, Typeface.NORMAL, context.assets))
    is GlassIcon.Svg -> SVG.getFromString(icon.svg).let { svg ->
      // Without a view box the document's own size is the drawing's; without either it cannot be sized.
      if (svg.documentViewBox == null && svg.documentWidth > 0 && svg.documentHeight > 0) {
        svg.setDocumentViewBox(0f, 0f, svg.documentWidth, svg.documentHeight)
      }
      val box = svg.documentViewBox ?: return@runCatching null
      IconDrawing { canvas, w, h -> svg.renderToCanvas(canvas, iconRect(box.width(), box.height(), w, h).toAndroidRectF()) }
    }
    is GlassIcon.Image -> loadImage(context, icon.uri, (MAX_IMAGE_DP * context.resources.displayMetrics.density).toInt())
      ?.let { bitmap ->
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        IconDrawing { canvas, w, h -> canvas.drawBitmap(bitmap, null, iconRect(bitmap.width.toFloat(), bitmap.height.toFloat(), w, h).toAndroidRectF(), paint) }
      }
  }
}.getOrNull()?.also { drawings[icon] = it }

/** The glyph's ink, fitted (not its advance box, which icon fonts pad unevenly). Null when the font has no ink for it. */
private fun glyphDrawing(icon: GlassIcon.Glyph, typeface: Typeface): IconDrawing? {
  val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.typeface = typeface; textSize = 100f }
  // A font the app did not add falls back to the system font, which has no such glyph.
  if (!paint.hasGlyph(icon.glyph)) return null
  val ink = Rect().also { paint.getTextBounds(icon.glyph, 0, icon.glyph.length, it) }
  if (ink.isEmpty) return null
  return IconDrawing { canvas, w, h ->
    val r = iconRect(ink.width().toFloat(), ink.height().toFloat(), w, h)
    val scale = r.width / ink.width()
    canvas.save()
    canvas.translate(r.left - ink.left * scale, r.top - ink.top * scale)
    canvas.scale(scale, scale)
    canvas.drawText(icon.glyph, 0f, 0f, paint)
    canvas.restore()
  }
}

/**
 * An image uri as React Native resolves it: a URL (Metro, remote), a file, a content uri, or a
 * release build's drawable name, decoded at most about [maxPx] on its longer side.
 */
private fun loadImage(context: Context, uri: String, maxPx: Int): Bitmap? {
  val bytes = when (Uri.parse(uri).scheme) {
    "http", "https" -> (URL(uri).openConnection() as HttpURLConnection).run {
      // A host that stalls must not hold the icon (and an IO thread) forever: it stays native.
      connectTimeout = LOAD_TIMEOUT_MS
      readTimeout = LOAD_TIMEOUT_MS
      try { inputStream.use { it.readBytes() } } finally { disconnect() }
    }
    "file" -> File(Uri.parse(uri).path!!).readBytes()
    null -> context.resources.getIdentifier(uri, "drawable", context.packageName).takeIf { it != 0 }
      ?.let { id -> context.resources.openRawResource(id).use { it.readBytes() } }
    else -> context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
  } ?: return null
  val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
  val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxPx) }
  return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}

private const val LOAD_TIMEOUT_MS = 10_000

/** The largest power of two that keeps a [width] × [height] image at least [maxPx] on its longer side. */
internal fun sampleSize(width: Int, height: Int, maxPx: Int): Int {
  var sample = 1
  while (maxOf(width, height) / (sample * 2) >= maxPx) sample *= 2
  return sample
}

/**
 * Draws a custom icon as a template: the tint Icon() passes (its colour filter) colours every
 * opaque pixel. [autoMirror]: flipped in RTL, as the auto-mirrored icon it replaces.
 */
private class GlassIconPainter(private val drawing: IconDrawing, private val autoMirror: Boolean) : Painter() {
  private var filter: ColorFilter? = null

  // Unspecified: Icon() gives it 24 dp, as a Material icon, unless the call site sizes it.
  override val intrinsicSize = Size.Unspecified

  override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
    filter = colorFilter
    return true
  }

  override fun DrawScope.onDraw() {
    val canvas = drawContext.canvas.nativeCanvas
    canvas.saveLayer(0f, 0f, size.width, size.height, Paint().apply { colorFilter = filter?.asAndroidColorFilter() })
    if (autoMirror && layoutDirection == LayoutDirection.Rtl) canvas.scale(-1f, 1f, size.width / 2, size.height / 2)
    drawing.draw(canvas, size.width, size.height)
    canvas.restore()
  }
}
