package com.universidad.reta2.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TemaClaro = lightColorScheme(
    primary = Primary100,
    primaryContainer = Primary200,
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,

    secondary = Accent100,
    secondaryContainer = Accent200,
    onSecondary = Text100,
    onSecondaryContainer = Text100,

    tertiary = Primary300,
    tertiaryContainer = Bg200,
    onTertiary = Text100,
    onTertiaryContainer = Text100,

    background = Bg100,
    onBackground = Text100,

    surface = Bg100,
    surfaceVariant = Bg200,
    onSurface = Text100,
    onSurfaceVariant = Text200,

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    outline = Bg300,
    outlineVariant = Bg200,

    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = Text100,
    inverseOnSurface = Bg100,
    inversePrimary = Primary200
)

private val TemaOscuro = darkColorScheme(
    primary = Primary200,
    primaryContainer = Primary100,
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,

    secondary = Accent200,
    secondaryContainer = Accent100,
    onSecondary = Text100,
    onSecondaryContainer = Text100,

    tertiary = Primary300,
    tertiaryContainer = Primary100,
    onTertiary = Text100,
    onTertiaryContainer = Color.White,

    background = Color(0xFF121212),
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCCCCCC),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    outline = Color(0xFF666666),
    outlineVariant = Color(0xFF444444),

    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = Color.White,
    inverseOnSurface = Text100,
    inversePrimary = Primary100
)

@Composable
fun Reta2Theme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) TemaOscuro else TemaClaro
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
