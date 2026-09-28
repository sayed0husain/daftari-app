package com.daftari.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = NavyPrimary,
    secondary = PencilOrange,
    background = BeigeBackgroundLight,
    surface = Color.White,
    onPrimary = Color.White
)

private val DarkColors = darkColorScheme(
    primary = SkyLight,
    secondary = PencilOrange,
    background = SurfaceDark,
    surface = SurfaceDarkAlt,
    onPrimary = NavyPrimary
)

/**
 * [themeMode]: -1 = follow system, 0 = force light, 1 = force dark.
 */
@Composable
fun DaftariTheme(themeMode: Int, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemInDarkTheme()
    }
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
