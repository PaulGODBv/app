package com.universidad.reta2.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TemaClaro = lightColorScheme(
    primary = Primary100,
    primaryContainer = Primary300,
    onPrimary = Color.White,
    onPrimaryContainer = Text100,

    secondary = Accent100,
    secondaryContainer = Accent200,
    onSecondary = Text100,
    onSecondaryContainer = Text100,

    tertiary = Primary200,
    tertiaryContainer = Bg200,
    onTertiary = Color.White,
    onTertiaryContainer = Text100,

    background = Bg100,
    onBackground = Text100,

    surface = Bg100,
    surfaceVariant = Bg200,
    onSurface = Text100,
    onSurfaceVariant = Text200,

    error = Error100,
    onError = Color.White,
    errorContainer = Success200.copy(alpha = 0.1f),
    onErrorContainer = Success100,

    outline = Bg300,
    outlineVariant = Bg200,

    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = Text100,
    inverseOnSurface = Bg100,
    inversePrimary = Primary200
)

private val TemaOscuro = darkColorScheme(
    primary = DarkPrimary100,
    primaryContainer = DarkPrimary300,
    onPrimary = DarkBg100,
    onPrimaryContainer = DarkText100,

    secondary = DarkAccent100,
    secondaryContainer = DarkAccent200,
    onSecondary = DarkBg100,
    onSecondaryContainer = DarkText100,

    tertiary = DarkPrimary200,
    tertiaryContainer = DarkBg200,
    onTertiary = DarkBg100,
    onTertiaryContainer = DarkText100,

    background = DarkBg100,
    onBackground = DarkText100,

    surface = DarkBg200, // Cambiado de DarkBg100 para dar elevación a tarjetas
    surfaceVariant = DarkBg300,
    onSurface = DarkText100,
    onSurfaceVariant = DarkText200,

    error = DarkError100,
    onError = Color.Black,
    errorContainer = DarkSuccess200.copy(alpha = 0.2f),
    onErrorContainer = DarkSuccess100,

    outline = DarkBg300,
    outlineVariant = DarkBg200,

    scrim = Color.Black.copy(alpha = 0.5f),
    inverseSurface = DarkText100,
    inverseOnSurface = DarkBg100,
    inversePrimary = DarkPrimary200
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
