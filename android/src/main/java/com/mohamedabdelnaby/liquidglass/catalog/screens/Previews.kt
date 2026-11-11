package com.mohamedabdelnaby.liquidglass.catalog.screens

import androidx.compose.runtime.Composable
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.AppearanceMode
import com.mohamedabdelnaby.liquidglass.catalog.ui.theme.LiquidGlassTheme

@Composable
internal fun PreviewLight(content: @Composable () -> Unit) = LiquidGlassTheme(AppearanceMode.Light, content)

@Composable
internal fun PreviewDark(content: @Composable () -> Unit) = LiquidGlassTheme(AppearanceMode.Dark, content)
