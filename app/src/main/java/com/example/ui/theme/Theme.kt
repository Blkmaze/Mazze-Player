package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MaZzePrimary,
    onPrimary = Color.White,
    primaryContainer = MaZzePrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = MaZzeSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D4A),
    onSecondaryContainer = MaZzeSecondary,
    tertiary = MaZzeAccentAmber,
    onTertiary = Color.Black,
    background = MaZzeDarkBackground,
    onBackground = TextPrimary,
    surface = MaZzeSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = MaZzeSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = MaZzeSurfaceBorder,
    error = MaZzeError,
    onError = Color.White
)

@Composable
fun MaZzeTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = MaZzeDarkBackground.toArgb()
                window.navigationBarColor = MaZzeDarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
