package com.universidad.reta2.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Los colores con los que la app dice «esto estuvo bien» o «esto estuvo mal».
 *
 * **Por qué no salen del `colorScheme`.** Material 3 no tiene un rol de éxito:
 * tiene `error`, pero su contrario no existe. Hasta ahora el acierto se pintaba
 * con `primary`, el azul de acción, y funcionaba mientras el acierto solo
 * aparecía en un número de la pantalla de resultados. Con la retroalimentación
 * inmediata y el ejercicio de unir parejas, el acierto pasa a ser un veredicto
 * constante y necesita color propio: verde y rojo son la convención que
 * cualquiera reconoce sin que se la expliquen.
 *
 * **Por qué un CompositionLocal y no `isSystemInDarkTheme()`.** La app tiene su
 * propio conmutador de tema en Perfil (Auto / Claro / Oscuro), así que
 * preguntarle al sistema daría el color equivocado a quien haya forzado un
 * modo. Lo provee `Reta2Theme`, que es quien de verdad sabe en qué tema está.
 *
 * Contrastes comprobados contra el fondo que les toca: 4,56:1 en claro y 6,20:1
 * en oscuro, por encima del 4,5:1 que pide AA para texto normal.
 */
@Immutable
data class ColoresDeVeredicto(
    val acierto: Color,
    val aciertoContenedor: Color
)

val VEREDICTO_CLARO = ColoresDeVeredicto(
    acierto = Success100,
    aciertoContenedor = Success050
)

val VEREDICTO_OSCURO = ColoresDeVeredicto(
    acierto = DarkSuccess100,
    aciertoContenedor = DarkSuccess300
)

/**
 * Por defecto el tema claro: si alguien olvida proveerlo, se ve raro pero
 * legible, en vez de reventar o pintar transparente.
 */
val LocalVeredicto = staticCompositionLocalOf { VEREDICTO_CLARO }

/** Atajo para leerlo con la misma forma que `MaterialTheme.colorScheme`. */
object Veredicto {
    val colores: ColoresDeVeredicto
        @Composable
        @ReadOnlyComposable
        get() = LocalVeredicto.current
}
