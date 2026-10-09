package com.universidad.reta2.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.universidad.reta2.R
import com.universidad.reta2.data.preferences.SessionManager

/**
 * El bucle de fondo del menú.
 *
 * **Solo en el menú, nunca dentro de un nivel.** Es una decisión de diseño, no
 * un descuido: los pasajes de Lectura Crítica llegan a 2 100 caracteres, y una
 * música con melodía reconocible compite con la comprensión lectora justo
 * cuando es lo único que se está midiendo. El profesor pidió «música de menú y
 * de nivel» el 30/09/2026; de nivel se deja fuera por esto, y hay que poder
 * defenderlo así.
 *
 * **`MediaPlayer` y no `SoundPool`.** Al contrario que los efectos, esto es un
 * fichero largo —1:10— que se reproduce en bucle: descomprimirlo entero a
 * memoria, que es lo que hace `SoundPool`, costaría varios megabytes de RAM
 * para nada. `MediaPlayer` lo va descodificando y además sabe repetirse solo.
 *
 * Quién lo arranca y lo para es `MainActivity`, que es la que sabe en qué
 * pantalla se está.
 */
object Musica {

    private var reproductor: MediaPlayer? = null

    /**
     * Si en la pantalla actual le toca sonar.
     *
     * Hace falta recordarlo para distinguir las dos razones por las que la
     * musica puede estar callada: porque se esta dentro de un nivel, o porque
     * la aplicacion se fue a segundo plano. Sin esta marca, volver de segundo
     * plano arrancaria la musica encima de una pregunta.
     */
    private var tocaSonar = false

    /**
     * Dice si la pantalla actual es de menu. La llama `MainActivity`, que es
     * la que conoce la ruta.
     */
    fun enMenu(context: Context, esMenu: Boolean) {
        tocaSonar = esMenu
        if (esMenu) reanudar(context) else pausar()
    }

    /** La aplicacion se va a segundo plano: callar sin olvidar donde estaba. */
    fun alIrseAlFondo() = pausar()

    /** La aplicacion vuelve: sonar solo si la pantalla de ahora es de menu. */
    fun alVolver(context: Context) {
        if (tocaSonar) reanudar(context)
    }

    /**
     * Arranca el bucle, o lo reanuda si ya estaba creado.
     *
     * Es idempotente: llamarla estando ya sonando no hace nada. Importa porque
     * la llama un efecto de Compose que se vuelve a evaluar en cada cambio de
     * ruta.
     */
    private fun reanudar(context: Context) {
        if (!SessionManager.musicaActiva(context)) return
        if (!elTelefonoSuena(context)) return

        val existente = reproductor
        if (existente != null) {
            if (!existente.isPlaying) existente.start()
            return
        }

        runCatching {
            // Los atributos de audio van en `create` y NO despues: `create`
            // devuelve el reproductor ya preparado, y `setAudioAttributes`
            // sobre uno preparado lanza IllegalStateException. Se pasan aqui
            // para que suene por el canal de medios y no por el de avisos.
            val atributos = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val sesion = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                .generateAudioSessionId()
            val v = SessionManager.volumenMusica(context)
            MediaPlayer.create(context, R.raw.menu_music, atributos, sesion)?.apply {
                isLooping = true
                setVolume(v, v)
                start()
            }
        }.onSuccess { reproductor = it }
            .onFailure { println("⚠️ No se pudo arrancar la música del menú: ${it.message}") }
    }

    /**
     * Cambia el volumen de lo que ya está sonando.
     *
     * Hace falta porque el deslizador de Perfil se arrastra **con la música
     * puesta**: sin esto habría que salir y volver para oír el cambio, y
     * ajustar a ciegas un volumen es justo lo que un deslizador evita.
     */
    fun ajustarVolumen(volumen: Float) {
        val v = volumen.coerceIn(0f, 1f)
        runCatching { reproductor?.setVolume(v, v) }
    }

    /** Pausa y conserva la posición: al volver al menú sigue donde estaba. */
    private fun pausar() {
        runCatching { reproductor?.takeIf { it.isPlaying }?.pause() }
    }

    /**
     * Suelta el reproductor. Se llama al apagar la música desde Perfil, para
     * no dejar un `MediaPlayer` vivo que ya nadie va a usar, y al cerrar la
     * aplicación.
     */
    fun soltar() {
        runCatching { reproductor?.release() }
        reproductor = null
    }

    /** Mismo criterio que en [Sonidos]: calla solo con el teléfono en silencio. */
    private fun elTelefonoSuena(context: Context): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return true
        return audio.ringerMode != AudioManager.RINGER_MODE_SILENT
    }
}
