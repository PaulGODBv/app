package com.universidad.reta2.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.universidad.reta2.data.preferences.SessionManager

/**
 * Vibración como respuesta a acertar, fallar y terminar un nivel.
 *
 * Es la mitad del punto 2 de la reunión del 30/09/2026 —«vibración del
 * teléfono»— y la única que no necesita ningún recurso: los patrones son
 * números, no ficheros, así que no hay licencias que documentar ni peso que
 * añadir al APK.
 *
 * **Patrones propios y no `createPredefined`.** Los efectos predefinidos
 * (`EFFECT_TICK`, `EFFECT_CLICK`, `EFFECT_HEAVY_CLICK`) solo existen desde la
 * API 29, y el mínimo del proyecto es la 24. Además cada fabricante los
 * interpreta a su manera, así que acertar y fallar podrían acabar sintiéndose
 * igual en algunos teléfonos. Con `createWaveform` el patrón es el mismo en
 * todas partes, y es el que distingue: acertar son dos toques cortos que
 * suben, fallar es uno solo más firme y más largo.
 *
 * **Tampoco se usa `LocalHapticFeedback` de Compose**, que sería lo idiomático:
 * solo ofrece `LongPress` y `TextHandleMove`, dos tipos para tres mensajes
 * distintos. No da para diferenciar un acierto de un fallo.
 *
 * Se llama desde los ViewModel y no desde las pantallas, aunque la respuesta
 * táctil sea cosa de la capa de presentación. El veredicto se calcula en el
 * ViewModel, en los tres formatos, así que ponerlo ahí es una llamada por
 * veredicto en lugar de montar eventos hasta la pantalla y recogerlos en tres
 * sitios. Es deuda consciente: si algún día hace falta que la vibración
 * dependa de si la pantalla está visible, habrá que mover esto a eventos.
 */
object Vibracion {

    /**
     * Acertar: dos toques cortos y ascendentes. Ligeros a propósito — esto
     * ocurre cada pocos segundos durante una sesión, y lo que se repite tanto
     * tiene que notarse sin llegar a molestar.
     */
    private val ACIERTO = Patron(
        tiempos = longArrayOf(0, 18, 60, 30),
        amplitudes = intArrayOf(0, 110, 0, 145),
    )

    /**
     * Fallar: uno solo, más firme y más largo. No es un castigo, es una señal
     * distinguible sin mirar: el contraste con el acierto está en la forma
     * —uno contra dos— y no en la intensidad, que en los teléfonos sin control
     * de amplitud es lo único que se pierde.
     */
    private val FALLO = Patron(
        tiempos = longArrayOf(0, 95),
        amplitudes = intArrayOf(0, 190),
    )

    /**
     * Nivel superado: tres toques que crecen. Es el único momento en el que se
     * permite durar, porque pasa una vez por nivel.
     */
    private val NIVEL_SUPERADO = Patron(
        tiempos = longArrayOf(0, 22, 70, 32, 70, 60),
        amplitudes = intArrayOf(0, 100, 0, 155, 0, 215),
    )

    /**
     * Nivel no superado: dos toques que **bajan**, al revés que los de
     * superado. Más largos que el fallo de una sola respuesta, porque es un
     * veredicto de cierre y no de una pregunta; quien lo note sabrá sin mirar
     * que no llegó al umbral.
     */
    private val NIVEL_NO_SUPERADO = Patron(
        tiempos = longArrayOf(0, 48, 60, 85),
        amplitudes = intArrayOf(0, 180, 0, 100),
    )

    /** Un toque mínimo, para confirmar que algo se ha registrado. */
    private val TOQUE = Patron(
        tiempos = longArrayOf(0, 14),
        amplitudes = intArrayOf(0, 95),
    )

    fun acierto(context: Context) = vibrar(context, ACIERTO)

    fun fallo(context: Context) = vibrar(context, FALLO)

    fun nivelSuperado(context: Context) = vibrar(context, NIVEL_SUPERADO)

    fun nivelNoSuperado(context: Context) = vibrar(context, NIVEL_NO_SUPERADO)

    fun toque(context: Context) = vibrar(context, TOQUE)

    /**
     * Un patrón es la pareja de tiempos y amplitudes que pide
     * `createWaveform`. Los tiempos alternan espera y vibración empezando por
     * espera, y de ahí el 0 inicial de todos los patrones de arriba.
     */
    private class Patron(val tiempos: LongArray, val amplitudes: IntArray)

    private fun vibrar(context: Context, patron: Patron) {
        // El ajuste del estudiante manda. Se consulta en cada vibración y no
        // se guarda en una variable, porque se puede apagar desde Perfil
        // mientras hay una sesión abierta.
        if (!SessionManager.vibracionActiva(context)) return

        val vibrador = vibrador(context) ?: return
        if (!vibrador.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Sin control de amplitud, pasar amplitudes no sirve de nada: el
            // teléfono vibra a su única intensidad. Se usa entonces la forma
            // de solo tiempos, que expresa lo mismo sin pedir lo que no hay.
            val efecto = if (vibrador.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(patron.tiempos, patron.amplitudes, SIN_REPETIR)
            } else {
                VibrationEffect.createWaveform(patron.tiempos, SIN_REPETIR)
            }
            // USAGE_ASSISTANCE_SONIFICATION la marca como respuesta de
            // interfaz. Importa porque algunos teléfonos silencian en «No
            // molestar» las vibraciones que no vienen etiquetadas.
            vibrador.vibrate(efecto, ATRIBUTOS_DE_INTERFAZ)
        } else {
            @Suppress("DEPRECATION")
            vibrador.vibrate(patron.tiempos, SIN_REPETIR)
        }
    }

    private fun vibrador(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    /** `-1` en `createWaveform` y en `vibrate` significa «no repetir». */
    private const val SIN_REPETIR = -1

    private val ATRIBUTOS_DE_INTERFAZ: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
}
