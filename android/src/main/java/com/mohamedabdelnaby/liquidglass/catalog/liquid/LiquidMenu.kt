// Ported from react-native-liquid's LiquidMenuView (MenuPanel, MenuRow, Separator; the React Native
// view host and its MenuOverlay dropped, the Compose bodies kept). Items are caller content here.
package com.mohamedabdelnaby.liquidglass.catalog.liquid

import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.mohamedabdelnaby.liquidglass.catalog.glass.LocalGlassBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.backdrops.emptyBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.drawBackdrop
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.blur
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.lens
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.effects.vibrancy
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.highlight.Highlight
import com.mohamedabdelnaby.liquidglass.catalog.liquid.backdrop.shadow.Shadow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The menu's glass, grown out of [trigger] (in this panel's coordinates) like a drop while [open],
 * and shrunk back into it once [open] turns false, then [onClosed]. Fill a full-screen window.
 * [onProgress] gets the drop's size each frame, for [menuSourceAlpha].
 */
@Composable
internal fun LiquidMenuPanel(
  trigger: Rect,
  open: Boolean,
  onDismissRequest: () -> Unit,
  onProgress: (Float) -> Unit,
  onClosed: () -> Unit,
  modifier: Modifier = Modifier,
  // As iOS: a button's menu grows toward the screen's middle (up from a button in the lower half).
  // A context menu sits below its preview instead.
  towardCenter: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  val dark = isSystemInDarkTheme()
  val backdrop = LocalGlassBackdrop.current?.takeIf { isGlassEffectSupported } ?: emptyBackdrop()
  val density = LocalDensity.current
  val scope = rememberCoroutineScope()
  // Two springs with different stiffness make the glass morph like a droplet.
  val openX = remember { mutableFloatStateOf(0f) }
  val openY = remember { mutableFloatStateOf(0f) }
  var morph by remember { mutableStateOf<Job?>(null) }

  // Stepped per drawn frame (at most a 30 fps frame at a time), so the stall of opening the
  // overlay cannot make the drop jump ahead: it always grows out of the button.
  fun morphTo(target: Float, x: LiquidSpring, y: LiquidSpring, then: (() -> Unit)? = null) {
    morph?.cancel()
    morph = scope.launch {
      x.value = openX.floatValue
      y.value = openY.floatValue
      x.target = target
      y.target = target
      var last = 0L
      while (true) {
        val now = withFrameNanos { it }
        val dt = if (last == 0L) 1f / 60f else ((now - last) / 1e9f).coerceIn(1f / 240f, 1f / 30f)
        last = now
        val moving = x.step(dt) or y.step(dt)
        openX.floatValue = x.value
        openY.floatValue = y.value
        // How far the drop is from the button's size, in either direction.
        val size = maxOf(x.value, y.value)
        onProgress(size)
        // Closing ends once the drop has faded into the button, not in the spring's slow tail.
        if (!moving || (target == 0f && size < DROP_GONE)) break
      }
      then?.invoke()
    }
  }
  var contentHeight by remember { mutableFloatStateOf(0f) }
  var closing by remember { mutableStateOf(false) }
  // Where the content sits inside the morphing panel; set during layout.
  var contentOffsetX = 0f
  var contentOffsetY = 0f
  // The drop's current size, for its corners.
  var dropWidth = 0f
  var dropHeight = 0f

  // Starts growing on the first frame, like iOS; the height aims at the menu's once measured.
  // Closes when the caller drops `open` (an item picked, or a dismiss request it accepted).
  LaunchedEffect(open) {
    closing = !open
    if (open) {
      morphTo(1f, LiquidSpring(0.72f, 340f, 0.001f), LiquidSpring(0.8f, 240f, 0.001f))
    } else {
      morphTo(0f, LiquidSpring(1f, 600f, 0.001f), LiquidSpring(1f, 520f, 0.001f), onClosed)
    }
  }

  Box(Modifier.fillMaxSize()) {
    // The lib's tap-outside-to-close, as its own node so a screen reader can dismiss the menu too.
    Box(
      Modifier
        .fillMaxSize()
        .pointerInput(Unit) { detectTapGestures { if (!closing) onDismissRequest() } }
        .semantics {
          contentDescription = "Dismiss menu"
          onClick { if (!closing) onDismissRequest(); true }
          traversalIndex = 1f
        },
    )

    val menuWidthPx = with(density) { MENU_WIDTH.dp.toPx() }
    val margin = with(density) { 12.dp.toPx() }
    val panelRadius = with(density) { 26.dp.toPx() }

    Box(
      Modifier
        .layout { measurable, constraints ->
          val screenWidth = constraints.maxWidth.toFloat()
          val screenHeight = constraints.maxHeight.toFloat()

          // Final rect: aligned to the trigger's nearest side, below it when there is room.
          val alignRight = trigger.center.x > screenWidth / 2
          val finalLeft = (if (alignRight) trigger.right - menuWidthPx else trigger.left)
            .coerceIn(margin, (screenWidth - menuWidthPx - margin).coerceAtLeast(margin))
          val finalHeight = (if (contentHeight > 0f) contentHeight else trigger.height)
            .coerceAtMost(screenHeight - margin * 2)
          val growUp = towardCenter && trigger.center.y > screenHeight / 2
          val finalTop = if (!growUp && trigger.top + finalHeight + margin <= screenHeight) trigger.top
          else (trigger.bottom - finalHeight).coerceAtLeast(margin)

          val x = openX.floatValue
          val y = openY.floatValue
          val left = lerp(trigger.left, finalLeft, x)
          val width = lerp(trigger.width, menuWidthPx, x)
          val top = lerp(trigger.top, finalTop, y)
          val height = lerp(trigger.height, finalHeight.coerceAtLeast(1f), y)

          // The content stays at its final position while the glass grows around it.
          contentOffsetX = finalLeft - left
          contentOffsetY = finalTop - top
          dropWidth = width
          dropHeight = height
          val placeable = measurable.measure(
            Constraints.fixed(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1)),
          )
          layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(left.roundToInt(), top.roundToInt())
          }
        }
        .drawBackdrop(
          backdrop = backdrop,
          shape = {
            // As on iOS, it stays a round drop until late, then settles into the panel's corners.
            val progress = minOf(openX.floatValue, openY.floatValue).coerceIn(0f, 1f)
            RoundedCornerShape(lerp(minOf(dropWidth, dropHeight) / 2f, panelRadius, progress * progress))
          },
          effects = {
            vibrancy()
            // A clear drop at first, so the button shows through it; the blur of the menu
            // builds up as it grows.
            val progress = minOf(openX.floatValue, openY.floatValue).coerceIn(0f, 1f)
            blur(16f.dp.toPx() * progress)
            lens(16f.dp.toPx(), 24f.dp.toPx())
          },
          highlight = { Highlight.Default },
          shadow = {
            val progress = minOf(openX.floatValue, openY.floatValue).coerceIn(0f, 1f)
            Shadow(radius = 24.dp, color = Color.Black.copy(alpha = (if (dark) 0.35f else 0.15f) * progress))
          },
          onDrawSurface = {
            // The drop starts in the button's light glass and turns into the menu's material.
            val progress = minOf(openX.floatValue, openY.floatValue).coerceIn(0f, 1f)
            val drop = if (dark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.32f)
            drawRect(androidx.compose.ui.graphics.lerp(drop, glassSurfaceColor(dark), progress))
          },
        )
        .pointerInput(Unit) { detectTapGestures { } },
    ) {
      Column(
        Modifier
          .layout { measurable, constraints ->
            // Measure at the final width with unbounded height to know the menu's size.
            val width = menuWidthPx.roundToInt()
            val placeable = measurable.measure(Constraints(width, width, 0, Constraints.Infinity))
            contentHeight = placeable.height.toFloat()
            layout(constraints.maxWidth, constraints.maxHeight) {
              placeable.place(contentOffsetX.roundToInt(), contentOffsetY.roundToInt())
            }
          }
          .graphicsLayer {
            // As on iOS, the items show early inside the growing drop, blurred, then sharpen.
            val shown = ((minOf(openX.floatValue, openY.floatValue) - 0.1f) / 0.6f).coerceIn(0f, 1f)
            alpha = shown
            val radius = 12.dp.toPx() * (1f - shown)
            renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && radius > 0.5f) {
              BlurEffect(radius, radius)
            } else {
              null
            }
          }
          .animateContentSize(spring(0.85f, Spring.StiffnessMediumLow))
          .then(modifier)
          .padding(vertical = 8.dp),
        content = content,
      )
    }

    // The lib's `if (closing) return` in close(): rows call the caller directly here, so a second
    // tap while the drop shrinks back must not reach them.
    if (closing) Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } })
  }
}

/**
 * The trigger's alpha for a drop of [size]. As on iOS the button's glass becomes the menu: the drop
 * starts on the button in its shape, so the button hides the moment the drop leaves its size, and
 * shows again once the drop has shrunk back into it.
 */
internal fun menuSourceAlpha(size: Float): Float = if (size > DROP_GONE) 0f else 1f

/** MenuPanel's title, used for section headers. */
@Composable
internal fun LiquidMenuTitle(text: String) {
  BasicText(
    text,
    Modifier.padding(horizontal = 20.dp, vertical = 8.dp).semantics { heading() },
    style = TextStyle(color = secondaryColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
  )
}

@Composable
internal fun LiquidMenuSeparator() {
  Box(
    Modifier
      .padding(vertical = 4.dp, horizontal = 16.dp)
      .fillMaxWidth()
      .height(0.5.dp)
      .background(separatorColor),
  )
}

/** The lib's row, with the app's checkmark leading and symbol trailing (iOS 26). */
@Composable
internal fun LiquidMenuRow(
  text: String,
  onClick: () -> Unit,
  icon: ImageVector?,
  destructive: Boolean,
  checked: Boolean,
) {
  val interaction = remember { MutableInteractionSource() }
  val pressed by interaction.collectIsPressedAsState()
  val color = if (destructive) Color(0xFFFF3B30) else labelColor

  Row(
    Modifier
      .fillMaxWidth()
      .clickable(
        interactionSource = interaction,
        indication = null,
        role = Role.Button,
        onClick = onClick,
      )
      .semantics { if (checked) selected = true }
      .graphicsLayer {
        val scale = if (pressed) 0.97f else 1f
        scaleX = scale
        scaleY = scale
      }
      .padding(horizontal = 20.dp, vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    if (checked) {
      BasicText("✓", Modifier.clearAndSetSemantics { }, style = TextStyle(color = color, fontSize = 17.sp))
    }
    BasicText(text, Modifier.weight(1f), style = TextStyle(color = color, fontSize = 17.sp))
    icon?.let { Icon(it, null, Modifier.size(20.dp), tint = color) }
  }
}

// MenuPanel's colors, read by each row now that the rows are caller content.
private val labelColor: Color @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black
private val secondaryColor: Color @Composable get() = if (isSystemInDarkTheme()) Color(0x99EBEBF5) else Color(0x993C3C43)
private val separatorColor: Color @Composable get() =
  if (isSystemInDarkTheme()) Color.White.copy(0.12f) else Color.Black.copy(0.08f)

private val isGlassEffectSupported: Boolean
  get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private fun glassSurfaceColor(dark: Boolean): Color = when {
  !isGlassEffectSupported && dark -> Color(0xF21C1C1E)
  !isGlassEffectSupported -> Color(0xF2F2F2F7)
  dark -> Color(0xFF1C1C1E).copy(alpha = 0.45f)
  else -> Color.White.copy(alpha = 0.45f)
}

/** The lib's default `menuWidth`, in dp. */
private const val MENU_WIDTH = 250f

/** Size (the drop's larger side, as a fraction of the way to the menu's) below which it is back on the button. */
private const val DROP_GONE = 0.02f
