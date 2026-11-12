package com.vish.currencyconverter.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2A5576),
    onPrimary = Color.White,
    background = Color(0xFFE8EEF4),
    onBackground = Color(0xFF17212B),
    surface = Color(0xFFF7FAFC),
    onSurface = Color(0xFF17212B),
    surfaceVariant = Color(0xFFE4EBF2),
    onSurfaceVariant = Color(0xFF536372),
    outline = Color(0xFFC5D0DA),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFA9CCE8),
    onPrimary = Color(0xFF102838),
    background = Color(0xFF0E141B),
    onBackground = Color(0xFFF4F8FB),
    surface = Color(0xFF182430),
    onSurface = Color(0xFFE7EEF4),
    surfaceVariant = Color(0xFF243646),
    onSurfaceVariant = Color(0xFFB4C3D1),
    outline = Color(0xFF3C5164),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(colorScheme = scheme) {
        // MatTheme does not set LocalContentColor, so plain Text stays black.
        // Surface supplies onBackground, which is what makes the title and result readable at night.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = scheme.background,
            contentColor = scheme.onBackground,
            content = content,
        )
    }
}