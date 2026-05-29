package com.rms.app.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Purple20,
    onPrimaryContainer = Purple80,
    secondary = Blue40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A2A5E),
    onSecondaryContainer = Blue80,
    tertiary = Success,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = Error,
    onError = Color.White,
    errorContainer = ErrorDark,
    onErrorContainer = ErrorLight
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEFF),
    onPrimaryContainer = Purple20,
    secondary = Blue40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBE1FF),
    onSecondaryContainer = Color(0xFF001A4E),
    tertiary = SuccessDark,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight,
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = ErrorDark
)

@Composable
fun RMSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RMSTypography,
        shapes = RMSShapes,
        content = content
    )
}
