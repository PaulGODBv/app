package com.universidad.reta2.ui.navigation

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/**
 * Cómo entra y sale cada pantalla.
 *
 * **El movimiento cuenta la jerarquía, y por eso no es el mismo en todas
 * partes.** Hay dos maneras de pasar de una pantalla a otra en esta
 * aplicación y no significan lo mismo:
 *
 *  - *Bajar* a algo: de la lista de competencias a una competencia, de ahí a
 *    un nivel. Eso se desliza desde la derecha, y el botón de atrás lo saca
 *    por donde entró. El gesto dice «esto está dentro de aquello» y hace que
 *    volver se sienta como volver.
 *  - *Cambiar de pestaña*: Inicio, Competencias, Progreso, Perfil. Entre
 *    hermanas no hay jerarquía que contar, así que deslizar mentiría —
 *    insinuaría un orden que no existe. Esas se funden.
 *
 * Es la distinción que Material llama eje compartido frente a fundido
 * cruzado, y se recoge aquí entera para que ninguna pantalla tenga que
 * acordarse de ella.
 *
 * **El desplazamiento es de un cuarto de pantalla, no de una entera.** Con el
 * ancho completo la pantalla que sale recorre 1 080 px en 300 ms y el efecto
 * es de latigazo; con un cuarto, acompañado del fundido, se lee la dirección
 * sin que nada cruce volando.
 */
object Transiciones {

    /** Lo bastante rápido para no estorbar, lo bastante lento para leerse. */
    private const val MILIS = 280

    private val curva = tween<Float>(MILIS, easing = FastOutSlowInEasing)

    /** La misma curva, para lo que se mide en pixeles y no en opacidad. */
    private val curvaDesplazamiento = tween<IntOffset>(MILIS, easing = FastOutSlowInEasing)

    /**
     * Si el sistema tiene las animaciones apagadas.
     *
     * Android deja quitarlas desde las opciones de desarrollador, y en
     * accesibilidad hay un «quitar animaciones» que hace lo mismo. Las
     * transiciones de Compose **no lo respetan solas**: hay que preguntarlo.
     *
     * Quien lo apaga suele tener un motivo —un teléfono justo de memoria, o
     * mareo con el movimiento— y es justo el perfil de buena parte de los
     * estudiantes que van a abrir esto. Con el ajuste a cero todo pasa a ser
     * un corte seco.
     *
     * Se lee una vez, al componer el grafo: cambiarlo exige salir de la
     * aplicación, así que no hace falta vigilarlo.
     */
    fun animacionesActivas(context: Context): Boolean = runCatching {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }.getOrDefault(true)

    // ---------- Bajar y subir en la jerarquía ----------

    fun entrarDesdeLaDerecha(
        activas: Boolean
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (!activas) EnterTransition.None
        else slideInHorizontally(curvaDesplazamiento) { ancho -> ancho / 4 } + fadeIn(curva)
    }

    fun salirHaciaLaIzquierda(
        activas: Boolean
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (!activas) ExitTransition.None
        else slideOutHorizontally(curvaDesplazamiento) { ancho -> -ancho / 4 } + fadeOut(curva)
    }

    fun volverDesdeLaIzquierda(
        activas: Boolean
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (!activas) EnterTransition.None
        else slideInHorizontally(curvaDesplazamiento) { ancho -> -ancho / 4 } + fadeIn(curva)
    }

    fun salirHaciaLaDerecha(
        activas: Boolean
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (!activas) ExitTransition.None
        else slideOutHorizontally(curvaDesplazamiento) { ancho -> ancho / 4 } + fadeOut(curva)
    }

    // ---------- Entre hermanas, y al arrancar ----------

    /**
     * Fundido con un crecimiento mínimo, del 94 % al 100 %.
     *
     * El cambio de tamaño es tan pequeño que no se ve como tal; lo que hace
     * es evitar que un fundido a secas parezca un parpadeo. Se usa entre las
     * pestañas de abajo y en el arranque, donde no hay un «dentro» ni un
     * «fuera» que indicar.
     */
    fun fundidoEntrada(
        activas: Boolean
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (!activas) EnterTransition.None
        else fadeIn(curva) + scaleIn(curva, initialScale = 0.94f)
    }

    fun fundidoSalida(
        activas: Boolean
    ): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (!activas) ExitTransition.None
        else fadeOut(curva) + scaleOut(curva, targetScale = 0.94f)
    }
}
