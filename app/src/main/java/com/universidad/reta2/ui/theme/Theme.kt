package com.universidad.reta2.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    errorContainer = Error200.copy(alpha = 0.1f),
    onErrorContainer = Error100,

    outline = Bg300,
    outlineVariant = Bg200,

    scrim = Color.Black.copy(alpha = 0.32f),
    inverseSurface = Text100,
    inverseOnSurface = Bg100,
    inversePrimary = Primary200
)

private val TemaOscuro = darkColorScheme(
    // Azul: acciones y estado.
    primary = DarkPrimary100,
    onPrimary = Color(0xFF05203A),
    primaryContainer = DarkPrimary300,
    onPrimaryContainer = Color(0xFFCFE3F8),

    // Ámbar: logro. Lo consumen el ranking, la tarjeta de desbloqueo de
    // resultados y los tramos del contrarreloj.
    secondary = DarkAccent100,
    onSecondary = Color(0xFF2A1A06),
    secondaryContainer = DarkAccent200,
    onSecondaryContainer = Color(0xFFF5DCBB),

    // Azul pizarra: el tramo intermedio (60–69 %), que no es ni logro ni fallo.
    tertiary = DarkTertiary100,
    onTertiary = DarkBg100,
    tertiaryContainer = DarkTertiary200,
    onTertiaryContainer = Color(0xFFD6E0EA),

    background = DarkBg100,
    onBackground = DarkText100,

    surface = DarkBg200,
    onSurface = DarkText100,
    surfaceVariant = DarkBg300,
    onSurfaceVariant = DarkText200,

    error = DarkError100,
    onError = Color(0xFF2A0A0A),
    errorContainer = DarkError200.copy(alpha = 0.2f),
    onErrorContainer = DarkError100,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    scrim = Color.Black.copy(alpha = 0.6f),
    inverseSurface = DarkText100,
    inverseOnSurface = DarkBg100,
    inversePrimary = DarkPrimary200
)

@Composable
fun Reta2Theme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) TemaOscuro else TemaClaro
    // El verde del acierto no cabe en el colorScheme —Material 3 no tiene rol
    // de exito— y se provee aparte. Aqui, que es donde de verdad se sabe en
    // que tema estamos: preguntarselo al sistema daria el color equivocado a
    // quien haya forzado un modo desde Perfil.
    val veredicto = if (darkTheme) VEREDICTO_OSCURO else VEREDICTO_CLARO

    CompositionLocalProvider(LocalVeredicto provides veredicto) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography(),
            content = content
        )
    }
}
