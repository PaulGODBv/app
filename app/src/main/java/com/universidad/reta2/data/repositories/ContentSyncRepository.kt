package com.universidad.reta2.data.repositories

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.universidad.reta2.data.local.dao.LevelDao
import com.universidad.reta2.data.local.dao.QuestionDao
import com.universidad.reta2.data.local.entities.LevelEntity
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
 * No borra nada. Una pregunta que el panel retira se marca inactiva y un nivel
 * que retira se marca no publicado: el progreso del estudiante apunta a sus
 * ids, y borrar la fila dejaria intentos huerfanos y cuentas de progreso que no
 * cuadran.
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
        /**
         * Todos los niveles que hay que bajarse, por su id del panel.
         *
         * Vive aqui y no en cada pantalla que la necesita: la pedian el splash
         * y ahora tambien Inicio, y dos copias de la misma lista es como se
         * cuela un nivel que una de las dos no pide. Se deriva del mapa de
         * traduccion, asi que anadir un nivel sigue siendo tocar un solo sitio.
         */
        val TODOS_LOS_NIVELES: List<Int> get() = NIVEL_DJANGO_A_APP.keys.sorted()
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

    /**
     * Si ya se bajo el catalogo con exito en esta ejecucion de la aplicacion.
     *
     * No se guarda en disco a proposito: al abrir la app otra vez conviene
     * volver a mirar que publico el panel. Lo que evita es repetir la bajada
     * dentro de la misma sesion, ahora que se pide desde dos sitios.
     *
     * **Solo se marca al tener exito.** Un intento que fallo por falta de red
     * debe poder reintentarse; si se marcara igualmente, el estudiante se
     * quedaria con el catalogo viejo hasta cerrar y abrir la aplicacion.
     */
    private val yaSincronizado = java.util.concurrent.atomic.AtomicBoolean(false)

    /** Evita que dos llamadas casi simultaneas lancen dos bajadas. */
    private val enMarcha = java.util.concurrent.atomic.AtomicBoolean(false)

    /**
     * Lanza la sincronizacion y no espera: el que llama sigue su camino.
     *
     * **Se pide desde el splash y desde Inicio, y eso no es redundancia.**
     * El splash solo la lanzaba cuando YA habia sesion al arrancar, asi que un
     * estudiante que acababa de registrarse no pasaba por ahi: jugaba toda su
     * primera sesion con el catalogo escrito en el codigo. Se vio en una prueba
     * del piloto — «Feelings» abria el cuestionario de evaluacion en vez de la
     * pantalla de unir parejas, porque el formato de practica sembrado es
     * "opcion" y el del panel es "unir"—, y se arreglaba solo al abrir la app
     * por segunda vez, que es lo que lo hacia dificil de reproducir.
     *
     * Pedirlo desde Inicio cubre todas las puertas de entrada —registro, inicio
     * de sesion y vuelta de un usuario conocido— sin tener que acordarse de
     * anadirlo en cada una.
     */
    fun sincronizarEnSegundoPlano(idsDeNivel: List<Int>) {
        if (yaSincronizado.get()) return
        if (!enMarcha.compareAndSet(false, true)) return

        alcance.launch {
            try {
                val r = sincronizar(idsDeNivel)
                println(
                    "📚 Contenido: ${r.nivelesSincronizados} niveles, " +
                        "${r.preguntasGuardadas} preguntas, ${r.nivelesConError} con error"
                )
                // Sin motivo y con algun nivel al dia: se dio por bueno.
                if (r.motivo == null && r.nivelesSincronizados > 0) {
                    yaSincronizado.set(true)
                }
            } finally {
                enMarcha.set(false)
            }
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
     * Descubre el catalogo del panel y crea en Room los niveles que falten.
     *
     * Es el paso que cierra TODO-1. Antes la lista de niveles era una
     * constante del codigo, asi que **dar de alta un nivel en el panel no
     * llegaba a ningun telefono** hasta publicar una version nueva de la app,
     * con su migracion de Room. Ahora la lista se pregunta.
     *
     * Devuelve la traduccion id-del-panel → id-local, que es lo que necesita
     * [sincronizar] para guardar las preguntas contra la clave foranea
     * correcta. Si el catalogo no se puede pedir, devuelve null y el que llama
     * sigue con lo que ya sabia: quedarse sin catalogo no debe dejar al
     * estudiante sin sincronizar lo de siempre.
     *
     * **Que se escribe y que no.** Del panel vienen el nombre, la descripcion y
     * el formato de practica. El bloqueo, el progreso y si esta completado NO
     * se tocan: eso se lo gano el estudiante jugando, y refrescarlo desde el
     * catalogo le borraria lo que tenia desbloqueado. Por eso un nivel que ya
     * existe se actualiza con [LevelDao.refrescarDesdeElPanel] y no
     * reinsertandolo entero.
     */
    suspend fun sincronizarCatalogo(): Map<Int, Int>? {
        val respuesta = runCatching { apiService.getCatalogo() }.getOrNull()
        val competencias = respuesta?.body()
        if (respuesta == null || !respuesta.isSuccessful || competencias == null) {
            println("⚠️ No se pudo leer el catalogo; se sigue con los niveles conocidos")
            return null
        }

        val mapa = mutableMapOf<Int, Int>()
        var creados = 0

        for (competencia in competencias) {
            for (nivel in competencia.levels) {
                // 1. Si ya lo conocemos por su id del panel, ese manda.
                val porDjango = levelDao.getLevelPorDjangoId(nivel.id)
                if (porDjango != null) {
                    mapa[nivel.id] = porDjango.id
                    levelDao.refrescarDesdeElPanel(
                        porDjango.id, nivel.name, nivel.description, nivel.formatoPractica
                    )
                    continue
                }

                // 2. Si no, puede ser uno sembrado desde `CompetencyData` que
                //    aun no tiene django_id. Se le propone el id que le toca
                //    por la formula y, si existe, se adopta.
                val idLocal = idLocalDeNivel(competencia.id, nivel.order)
                val existente = levelDao.getLevelPorId(idLocal)
                if (existente != null) {
                    levelDao.adoptarDjangoId(idLocal, nivel.id)
                    levelDao.refrescarDesdeElPanel(
                        idLocal, nivel.name, nivel.description, nivel.formatoPractica
                    )
                    mapa[nivel.id] = idLocal
                    continue
                }

                // 3. Nivel nuevo. Se crea.
                runCatching {
                    levelDao.insertLevel(
                        LevelEntity(
                            id = idLocal,
                            competenceId = competencia.id,
                            djangoId = nivel.id,
                            name = nivel.name,
                            description = nivel.description,
                            // Nace bloqueado si el panel lo dice. Un nivel nuevo
                            // en medio de una escalera ya recorrida no debe
                            // regalarse ni cerrar el paso: el desbloqueo lo
                            // gobierna el progreso, como siempre.
                            isLocked = nivel.bloqueadoPorDefecto,
                            formatoPractica = nivel.formatoPractica
                        )
                    )
                }.onSuccess {
                    creados++
                    mapa[nivel.id] = idLocal
                    println("✅ Nivel nuevo del panel: ${nivel.name} (Django ${nivel.id} → $idLocal)")
                }.onFailure {
                    // Lo mas probable: su competencia no existe en Room. Las
                    // cinco del Icfes estan sembradas, asi que esto solo pasa
                    // si alguien crea una sexta en el panel — y eso si necesita
                    // version nueva de la app, por el icono y el color.
                    println("⚠️ No se pudo crear el nivel ${nivel.id}: ${it.message}")
                }
            }
        }

        // Y la direccion contraria: lo que el panel ya no publica deja de
        // ofrecerse. No se borra —el progreso del estudiante apunta al id del
        // nivel— solo se baja de la lista, asi que si vuelve, vuelve entero.
        //
        // El `isNotEmpty` no es cosmetico: si un dia la peticion contestara 200
        // con una lista vacia, sin esta guarda la app se quedaria sin un solo
        // nivel visible. Una respuesta vacia se trata como no haber preguntado.
        if (mapa.isNotEmpty()) {
            val idsDelPanel = mapa.keys.toList()
            levelDao.publicarLosQueEstan(idsDelPanel)
            levelDao.despublicarLosQueFaltan(idsDelPanel)
        }

        if (creados > 0) println("📘 Catalogo: $creados nivel(es) nuevo(s)")
        return mapa
    }

    /**
     * El id local que le corresponde a un nivel del panel.
     *
     * Es la misma formula que `generateLevelId`: competencia por cien mas el
     * numero de nivel. Un `order` de cero da un id terminado en 00, que es lo
     * que `LevelRules.esDePractica` reconoce como calentamiento.
     *
     * **Solo se usa al crear o al adoptar.** Una vez el nivel tiene su
     * `django_id`, la correspondencia sale de ahi y reordenar en el panel deja
     * de afectarle.
     */
    private fun idLocalDeNivel(competenceId: Int, order: Int) = competenceId * 100 + order

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

        // El catalogo manda: dice que niveles hay y cual es el id local de cada
        // uno. Si no se puede leer, se cae a la tabla de siempre y a la lista
        // que venga por parametro, que es como funcionaba antes.
        val descubierto = sincronizarCatalogo()
        val traduccion = descubierto ?: NIVEL_DJANGO_A_APP
        val aSincronizar = if (descubierto != null) {
            descubierto.keys.sorted()
        } else {
            idsDeNivel
        }

        var niveles = 0
        var preguntas = 0
        var errores = 0
        val urlsNuevas = mutableListOf<String>()

        for (levelId in aSincronizar) {
            val idLocal = traduccion[levelId]
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
    contextImageEsFigura = contextImageEsFigura,
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
