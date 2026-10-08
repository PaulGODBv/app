package com.universidad.reta2.data.repositories

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.universidad.reta2.data.local.dao.LevelDao
import com.universidad.reta2.data.local.dao.QuestionDao
import com.universidad.reta2.data.local.entities.QuestionEntity
import com.universidad.reta2.data.local.entities.QuestionOptionEntity
import com.universidad.reta2.data.remote.NetworkChecker
import com.universidad.reta2.data.remote.Reta2ApiService
import com.universidad.reta2.data.remote.dto.RemoteQuestionDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Baja el banco de preguntas del panel y lo vuelca en Room.
 *
 * La regla del sistema es que **la app juega siempre contra Room**; la red solo
 * actualiza Room. Asi una practica no depende de que haya cobertura, que es
 * justo lo que se pedia del modo offline.
 *
 * No borra nada. Una pregunta que el panel retira se marca inactiva: el
 * progreso del estudiante apunta a su id, y borrar la fila dejaria intentos
 * huerfanos y cuentas de progreso que no cuadran.
 */
@Singleton
class ContentSyncRepository @Inject constructor(
    private val apiService: Reta2ApiService,
    private val questionDao: QuestionDao,
    private val levelDao: LevelDao,
    private val networkChecker: NetworkChecker,
    @ApplicationContext private val context: Context
) {

    // Deja de ser privado porque `SplashViewModel` deriva de aqui la lista
    // de niveles a sincronizar, en vez de mantener la suya.
    companion object {
        /**
         * Traduccion entre las dos numeraciones de nivel que conviven.
         *
         * Django usa su clave primaria (1..13) y la app genera la suya con
         * `generateLevelId(competencia, nivel)` (101, 102, 201...). La tabla
         * `levels` de Room tiene las de la app, y `questions` tiene una clave
         * foranea contra ella: guardar una pregunta con el id de Django
         * revienta con FOREIGN KEY constraint failed, que es justo lo que
         * pasaba.
         *
         * La traduccion vive aqui, en el borde: dentro de la app los ids
         * siguen siendo los de siempre y nada mas se entera.
         */
        val NIVEL_DJANGO_A_APP = mapOf(
            1 to 101, 2 to 102, 3 to 103,
            4 to 201, 5 to 202, 6 to 203,
            7 to 301, 8 to 302, 9 to 303, 10 to 304,
            11 to 401, 12 to 402, 13 to 403,
            // Niveles de práctica (order = 0 en el panel). Terminan en 00, que
            // es lo que `LevelRules.esDePractica` mira.
            14 to 100, 15 to 200, 16 to 300, 17 to 400,
            // Comunicación Escrita, la quinta competencia.
            18 to 501,
        )
    }

    /**
     * Alcance propio, con la vida del proceso.
     *
     * La descarga se lanzaba desde el `viewModelScope` del splash, que muere
     * en cuanto se navega a Inicio: la bajada se cancelaba a mitad y solo
     * entraban los primeros niveles. Descargar el catalogo no es trabajo de
     * una pantalla, asi que no debe depender de que esa pantalla siga viva.
     */
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Lanza la sincronizacion y no espera: el que llama sigue su camino. */
    fun sincronizarEnSegundoPlano(idsDeNivel: List<Int>) {
        alcance.launch {
            val r = sincronizar(idsDeNivel)
            println(
                "📚 Contenido: ${r.nivelesSincronizados} niveles, " +
                    "${r.preguntasGuardadas} preguntas, ${r.nivelesConError} con error"
            )
        }
    }

    data class Resultado(
        val nivelesSincronizados: Int = 0,
        val preguntasGuardadas: Int = 0,
        val nivelesConError: Int = 0,
        val motivo: String? = null
    ) {
        val huboCambios: Boolean get() = preguntasGuardadas > 0
    }

    /**
     * Sincroniza los niveles indicados.
     *
     * Se recorre nivel a nivel y cada uno se guarda en su propia transaccion:
     * si el cuarto falla, los tres primeros ya estan en Room y sirven. Una
     * transaccion unica para los trece dejaria al estudiante sin nada por un
     * corte a mitad.
     */
    suspend fun sincronizar(idsDeNivel: List<Int>): Resultado {
        if (!networkChecker.isConnected()) {
            return Resultado(motivo = "Sin conexión")
        }

        var niveles = 0
        var preguntas = 0
        var errores = 0
        val urlsNuevas = mutableListOf<String>()

        for (levelId in idsDeNivel) {
            val idLocal = NIVEL_DJANGO_A_APP[levelId]
            if (idLocal == null) {
                // Un nivel que la app no conoce: se ignora en vez de romper la
                // sincronizacion entera.
                println("⚠️ Nivel $levelId del panel sin equivalente local, se omite")
                continue
            }
            try {
                val respuesta = apiService.getLevelContent(levelId)
                val cuerpo = respuesta.body()
                if (!respuesta.isSuccessful || cuerpo == null) {
                    errores++
                    continue
                }

                val activas = cuerpo.questions.filter { it.isActive }
                val retiradas = cuerpo.questions.filter { !it.isActive }.map { it.id }

                val entidades = activas.map { it.aEntidad(idLocal) }
                val opciones = activas.flatMap { it.aOpciones() }

                questionDao.reemplazarNivel(entidades, opciones, retiradas)

                // Lo unico que se toma del nivel: como se juega en practica.
                // El nombre, la descripcion y el bloqueo los gobierna la app, y
                // pisarlos desde el panel borraria el desbloqueo del estudiante.
                levelDao.actualizarFormatoDePractica(idLocal, cuerpo.formatoPractica)

                niveles++
                preguntas += entidades.size
                urlsNuevas += activas.mapNotNull { it.contextImageUrl }
            } catch (e: Exception) {
                println("⚠️ Sync de contenido, nivel $levelId: ${e.message}")
                errores++
            }
        }

        // Las imagenes se piden una vez terminado el volcado, no durante: asi
        // el catalogo queda utilizable cuanto antes y la descarga de imagenes
        // no retrasa el momento en que se puede practicar.
        precargarImagenes(urlsNuevas.distinct())

        return Resultado(niveles, preguntas, errores)
    }

    /**
     * Pide las imagenes para que Coil las deje en su cache de disco.
     *
     * Sin esto, la primera vez que un estudiante abre una pregunta con imagen
     * necesita cobertura justo en ese momento. Con esto, la imagen ya esta
     * descargada desde la sincronizacion.
     */
    private suspend fun precargarImagenes(urls: List<String>) {
        if (urls.isEmpty()) return
        val cargador = ImageLoader(context)
        for (url in urls) {
            try {
                cargador.execute(
                    ImageRequest.Builder(context)
                        .data(url)
                        // Solo interesa que quede en disco; decodificarla en
                        // memoria ahora seria gastar memoria sin motivo.
                        .build()
                )
            } catch (e: Exception) {
                println("⚠️ No se pudo precargar $url: ${e.message}")
            }
        }
    }
}

private fun RemoteQuestionDto.aEntidad(levelIdLocal: Int) = QuestionEntity(
    id = id,
    // Siempre el id local: el `level_id` que manda el panel es el suyo, y la
    // clave foranea de Room apunta a la tabla de niveles de la app.
    levelId = levelIdLocal,
    competenceId = competenceId,
    text = text,
    // El panel manda la POSICION de la correcta; dentro de la app la opcion se
    // identifica por su orden, que es lo que se guarda como id sintetico.
    correctOptionId = correctOptionOrder,
    explanation = explanation.orEmpty(),
    readingText = readingText.orEmpty(),
    // Django manda cadena vacia cuando no hay imagen, no null. Guardarla tal
    // cual hacia que la pantalla creyera que si la hay y pintara el hueco con
    // "Imagen no encontrada". Vacio es ausencia, y aqui se traduce a null.
    contextImageUrl = contextImageUrl?.takeIf { it.isNotBlank() },
    contextImageAlt = contextImageAlt?.takeIf { it.isNotBlank() },
    contextImage = contextImage?.takeIf { it.isNotBlank() },
    remoteUpdatedAt = updatedAt,
    isActive = isActive
)

private fun RemoteQuestionDto.aOpciones(): List<QuestionOptionEntity> =
    options.sortedBy { it.order }.mapIndexed { indice, opcion ->
        QuestionOptionEntity(
            questionId = id,
            optionText = opcion.text,
            // `originalOrder` es 0-based dentro de la app y `order` 1-based en
            // el panel; se normaliza aqui para que la comparacion con
            // `correctOptionId` siga funcionando igual que con el banco local.
            originalOrder = indice
        )
    }
