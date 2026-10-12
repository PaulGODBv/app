package com.universidad.reta2.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.universidad.reta2.ui.navigation.Transiciones
import kotlinx.coroutines.delay

/**
 * Entrada escalonada de una lista: cada tarjeta sube un poco y aparece, un
 * instante despues que la anterior.
 *
 * Lo que aporta no es adorno. La lista llega despues de leer Room, asi que
 * hasta ahora pasaba de pantalla vacia a pantalla llena **de golpe**, sin
 * nada que dijera que eso acababa de cargarse. La cascada da ese aviso y de
 * paso lleva la mirada de arriba abajo, que es el orden en que hay que leer.
 */

/** Cuanto espera cada tarjeta respecto a la anterior. */
private const val RETARDO_ENTRE_TARJETAS = 55L

/**
 * A partir de la septima el retardo deja de crecer.
 *
 * Una competencia puede tener cinco niveles y la lista de progreso bastantes
 * mas. Sin este tope, la ultima tarjeta de una lista de veinte esperaria mas
 * de un segundo, y una animacion que se hace esperar deja de ser un aviso
 * para convertirse en lentitud.
 */
private const val MAXIMO_ESCALONES = 6

/** Lo que sube cada tarjeta al entrar. Suficiente para leerse, no para saltar. */
private val DESPLAZAMIENTO = 14.dp

/**
 * Recuerda que tarjetas ya entraron.
 *
 * Hace falta porque `LazyColumn` **destruye lo que sale de pantalla y lo
 * vuelve a componer al volver**: sin esta memoria, bajar y subir por la lista
 * repetiria la animacion en cada pasada y la pantalla no se quedaria quieta
 * nunca. Vive en la pantalla, no en la tarjeta, que es lo unico que sobrevive
 * al reciclaje.
 */
@Stable
class Cascada(val activa: Boolean) {
    private val yaEntraron = mutableSetOf<Int>()
    fun yaEntro(indice: Int) = indice in yaEntraron
    fun anotar(indice: Int) { yaEntraron.add(indice) }
}

/**
 * Crea la memoria de la cascada, apagada si el sistema no quiere animaciones.
 */
@Composable
fun recordarCascada(): Cascada {
    val contexto = LocalContext.current
    return remember { Cascada(Transiciones.animacionesActivas(contexto)) }
}

/**
 * Envuelve una tarjeta de la lista para que entre escalonada.
 *
 * Si esa posicion ya entro antes —o si las animaciones estan apagadas— se
 * dibuja directamente en su sitio, sin animar nada.
 */
@Composable
fun EnCascada(
    indice: Int,
    cascada: Cascada,
    contenido: @Composable () -> Unit
) {
    val anima = cascada.activa && !cascada.yaEntro(indice)
    val progreso = remember(indice) { Animatable(if (anima) 0f else 1f) }
    val desplazamiento = with(LocalDensity.current) { DESPLAZAMIENTO.toPx() }

    LaunchedEffect(indice) {
        if (!anima) return@LaunchedEffect
        delay(indice.coerceAtMost(MAXIMO_ESCALONES) * RETARDO_ENTRE_TARJETAS)
        progreso.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
        cascada.anotar(indice)
    }

    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progreso.value
            translationY = (1f - progreso.value) * desplazamiento
        }
    ) {
        contenido()
    }
}
