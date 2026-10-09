package com.universidad.reta2.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.universidad.reta2.R
import com.universidad.reta2.data.preferences.SessionManager

/**
 * Efectos cortos: acertar, fallar, superar un nivel y confirmar un toque.
 *
 * **`SoundPool` y no `MediaPlayer`.** Los efectos tienen que sonar en el
 * instante del toque, y `MediaPlayer` prepara el recurso cada vez: el primer
 * clic llegaría tarde y se notaría. `SoundPool` los descomprime una vez a PCM
 * en memoria y luego dispararlos no cuesta nada. A cambio solo sirve para
 * clips cortos, que es exactamente lo que son estos —medio segundo— mientras
 * la música va por su lado en [Musica].
 *
 * **La carga es asíncrona.** `load()` devuelve un identificador antes de que el
 * sonido esté listo, y pedir que suene antes de tiempo no falla: no se oye
 * nada, que es peor porque parece un fallo de volumen. Por eso se cargan al
 * arrancar la aplicación desde `Reta2App`, no en el primer uso.
 *
 * Procedencia de los ficheros: paquetes de interfaz de Kenney, **CC0**, que es
 * renuncia al dominio público — sin atribución obligatoria y sin restricción
 * de uso. Queda dicho aquí porque es lo que hay que poder justificar en la
 * memoria.
 */
object Sonidos {

    /**
     * Cuatro a la vez es de sobra: lo máximo que puede coincidir es un acierto
     * encadenado con el sonido de nivel superado.
     */
    private const val CANALES = 4

    private var pool: SoundPool? = null

    /** Identificador que da `SoundPool` por cada fichero, o null si falló. */
    private var acierto: Int? = null
    private var fallo: Int? = null
    private var toque: Int? = null
    private var nivelSuperado: Int? = null
    private var nivelNoSuperado: Int? = null
    private var colocar: Int? = null

    /** Los que ya terminaron de descomprimirse y se pueden disparar. */
    private val cargados = mutableSetOf<Int>()

    /**
     * Se llama una vez, desde `Reta2App`. No hay `liberar()` a propósito: el
     * pool vive lo que vive el proceso, igual que los efectos, y soltarlo en
     * cada pantalla significaría volver a descomprimir al entrar.
     */
    fun init(context: Context) {
        if (pool != null) return

        val p = SoundPool.Builder()
            .setMaxStreams(CANALES)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        p.setOnLoadCompleteListener { _, idDelSonido, estado ->
            // estado 0 es éxito. Si un fichero falla, ese efecto simplemente
            // no suena; no se arrastra el resto.
            if (estado == 0) cargados.add(idDelSonido)
        }

        acierto = p.load(context, R.raw.confirmation_002, 1)
        fallo = p.load(context, R.raw.error_003, 1)
        toque = p.load(context, R.raw.select_004, 1)
        nivelSuperado = p.load(context, R.raw.nivel_superado, 1)
        nivelNoSuperado = p.load(context, R.raw.nivel_no_superado, 1)
        colocar = p.load(context, R.raw.switch_002, 1)
        pool = p
    }

    fun acierto(context: Context) = reproducir(context, acierto)

    fun fallo(context: Context) = reproducir(context, fallo)

    /** Remate de cierre cuando el nivel se da por superado. */
    fun nivelSuperado(context: Context) = reproducir(context, nivelSuperado)

    /**
     * Y el de cuando no llega al umbral. Existe como sonido propio y no como
     * un [fallo] más largo porque dice otra cosa: fallar es una respuesta,
     * no superar es el resultado de todas.
     */
    fun nivelNoSuperado(context: Context) = reproducir(context, nivelNoSuperado)

    fun toque(context: Context) = reproducir(context, toque)

    /**
     * Una palabra cae en un hueco, en el formato de arrastrar.
     *
     * Suena pero **no vibra**, al contrario que el resto. Un ejercicio tiene
     * ocho huecos y las palabras se recolocan varias veces antes de comprobar:
     * vibrar en cada soltada serían veinte sacudidas por ejercicio, y lo que
     * se repite tanto deja de informar y solo molesta. El sonido corto basta
     * para confirmar que la palabra se quedó donde se soltó.
     */
    fun colocar(context: Context) = reproducir(context, colocar)

    private fun reproducir(context: Context, idDelSonido: Int?) {
        if (!SessionManager.efectosActivos(context)) return
        if (!elTelefonoSuena(context)) return

        val p = pool ?: return
        val id = idDelSonido ?: return
        if (id !in cargados) return

        // El volumen se consulta en cada disparo y no se guarda: el
        // deslizador de Perfil tiene que notarse en el siguiente sonido, sin
        // reiniciar nada.
        val v = SessionManager.volumenEfectos(context)
        p.play(id, v, v, 1, 0, 1f)
    }

    /**
     * Si el teléfono está en silencio del todo, no suena.
     *
     * **Solo silencio, no vibración.** La primera versión exigía timbre normal
     * y eso dejaba la aplicación muda en el modo en que mucha gente vive todo
     * el día: con el timbre en vibración, que es como estaba el teléfono de
     * pruebas el 09/10/2026 — no se oía nada y parecía que el sonido estaba
     * roto.
     *
     * El criterio correcto es el de Android: el modo de timbre gobierna avisos
     * y llamadas, no el audio de medios. Un juego sigue sonando con el timbre
     * en vibración. Lo que sí se respeta, y es deliberado, es el **silencio**:
     * esto se abre en clase, y quien silencia el teléfono allí no espera que
     * una aplicación pite al fallar una pregunta.
     */
    private fun elTelefonoSuena(context: Context): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return true
        return audio.ringerMode != AudioManager.RINGER_MODE_SILENT
    }
}
