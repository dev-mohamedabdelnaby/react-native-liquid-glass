package com.mohamedabdelnaby.liquidglass.catalog.glass

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.mohamedabdelnaby.liquidglass.R
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme

/**
 * A photo behind the glass, like react-native-liquid's playground: glass only reads as liquid
 * over detailed content it can bend. Dark mode dims it a little so labels stay legible.
 */
@Composable
fun GlassBackdrop(modifier: Modifier = Modifier, @DrawableRes photo: Int = R.drawable.backdrop_fjord) {
    // Material has no glass to show off: the plain page.
    if (isMaterial) return PlainBackdrop(modifier, MaterialTheme.colorScheme.surface)
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Image(painterResource(photo), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        if (isSystemInDarkTheme()) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
    }
}

@Preview(name = "Light")
@Composable
private fun GlassBackdropLightPreview() = LiquidGlassTheme(AppearanceMode.Light) { GlassBackdrop() }

@Preview(name = "Dark")
@Composable
private fun GlassBackdropDarkPreview() = LiquidGlassTheme(AppearanceMode.Dark) { GlassBackdrop() }
