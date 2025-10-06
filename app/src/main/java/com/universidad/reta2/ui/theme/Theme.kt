package com.universidad.reta2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = primary,
    secondary = secondary,
    background = background,
    surface = surface,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceSecondary
)

@Composable
fun Reta2Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}