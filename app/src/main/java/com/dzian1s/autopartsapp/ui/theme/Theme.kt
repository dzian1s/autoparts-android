package com.dzian1s.autopartsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = BrandYellow,
    onPrimary = Color(0xFF111111),

    secondary = BrandYellowDark,
    onSecondary = Color(0xFF111111),

    background = BgBlack,
    onBackground = TextOnDark,

    surface = SurfaceDark,
    onSurface = TextOnDark,

    surfaceVariant = SurfaceDark2,
    onSurfaceVariant = TextMuted,

    outline = OutlineDark,
    error = BrandError,
    onError = Color.White
)

@Composable
fun AutopartsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = Typography,
        content = content
    )
}