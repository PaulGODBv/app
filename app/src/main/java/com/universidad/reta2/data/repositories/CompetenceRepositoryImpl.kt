package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.CatalogoCache
import android.content.Context
import com.universidad.reta2.data.local.dao.CompetenceDao
import com.universidad.reta2.data.local.dao.LevelDao
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.QuestionDao
import com.universidad.reta2.data.local.mappers.CompetenceMapper
import com.universidad.reta2.data.local.entities.LevelEntity
import com.universidad.reta2.data.local.entities.CompetenceEntity
import com.universidad.reta2.domain.LevelRules
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.QuestionRepository
import com.universidad.reta2.R
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CompetenceRepositoryImpl @Inject constructor(
    private val competenceDao: CompetenceDao,
    private val levelDao: LevelDao,
    private val competenceMapper: CompetenceMapper,
    private val progressDao: ProgressDao,
    private val questionDao: QuestionDao,
    private val catalogoCache: CatalogoCache,
    @ApplicationContext private val context: Context,
    private val questionRepository: QuestionRepository
) : CompetenceRepository {

    private fun getCurrentUserName(): String {
        return try {
            val username = SessionManager.getCurrentUsername(context)
            if (username.isNullOrEmpty()) {
                println("⚠️ CompetenceRepo: No hay usuario logueado, usando 'usuario_invitado'")
                "usuario_invitado"
            } else {
                username
            }
        } catch (e: Exception) {
            println("❌ CompetenceRepo: Error obteniendo usuario: ${e.message}")
            "usuario_invitado" // Fallback seguro
        }
    }
    override suspend fun getAllCompetences(): List<Competence> {
        return try {
            // Componer el catalogo con el progreso cuesta unos 200 ms por las
            // consultas de intentos nivel a nivel. Los ViewModel de las
            // pestanas se recrean al volver a ellas, asi que sin esta cache el
            // calculo se repetia en cada cambio y obligaba a ensenar el
            // esqueleto de carga una y otra vez. La capa de progreso la invalida
            // en cuanto escribe algo.
            catalogoCache.obtener(getCurrentUserName())?.let { return it }

            val competenceEntities = competenceDao.getAllCompetences()

            if (competenceEntities.isEmpty()) {
                val hardcodedCompetences = getHardcodedCompetences()
                saveCompetencesToDatabase(hardcodedCompetences)
                hardcodedCompetences // Devuelve 0% de progreso la primera vez, se recalculará al volver a cargar
            } else {
                // 🔥 --- LÓGICA DE CÁLCULO DE PROGRESO MODIFICADA ---
                val username = getCurrentUserName()

                // Usamos map (en lugar de un bucle for) para transformar la lista
                competenceEntities.map { entity ->
                    val levels = getLevelsFromDatabase(entity.id)

                    // 1. Mapear la entidad a dominio (progreso aún es 0f)
                    val competence = competenceMapper.toDomain(entity, levels)

                    // 2. Calcular el progreso real
                    // Sin los de práctica: el calentamiento no es evaluación, y
                    // sumarlo movería el porcentaje de la competencia con
                    // ejercicios que a propósito no desbloquean nada.
                    val levelIds = levels.map { it.id }
                        .filterNot { LevelRules.esDePractica(it) }
                    var calculatedProgress = 0f

                    if (levelIds.isNotEmpty()) {

                        // 1. DENOMINADOR: las preguntas que hoy tiene el banco.
                        val competenceId = entity.id
                        val idsDelBanco = mutableListOf<Int>()

                        // Bucle tradicional porque getQuestions... es 'suspend'
                        for (levelId in levelIds) {
                            idsDelBanco += questionRepository
                                .getQuestionsByCompetenceAndLevel(competenceId, levelId)
                                .map { it.id }
                        }
                        val totalQuestions = idsDelBanco.size

                        // 2. NUMERADOR: aciertos, pero solo de esas preguntas.
                        //
                        // Se le pasan los ids del banco a proposito. Contar
                        // cualquier intento acertado incluia los que apuntan a
                        // preguntas que ya no existen —los ids cambiaron al
                        // traer el banco del panel— y el porcentaje se iba por
                        // encima del 100 %: un nivel de 8 marcaba 137 %.
                        val correctQuestions = if (idsDelBanco.isEmpty()) 0
                        else progressDao.contarAciertosDeEstasPreguntas(
                            username, levelIds, idsDelBanco
                        )

                        if (totalQuestions > 0) {
                            // El techo es una red de seguridad: con numerador y
                            // denominador midiendo lo mismo no deberia hacer
                            // falta, pero una barra al 137 % es peor que una
                            // barra llena.
                            calculatedProgress =
                                (correctQuestions.toFloat() / totalQuestions.toFloat())
                                    .coerceIn(0f, 1f)
                        }

                        println("📊 Progreso para ${competence.name}: $correctQuestions / $totalQuestions = $calculatedProgress")
                    }

                    competence.copy(totalProgress = calculatedProgress)
                }.also { catalogoCache.guardar(username, it) }
            }
        } catch (e: Exception) {
            println("❌ Error en getAllCompetences: ${e.message}")
            getHardcodedCompetences()
        }
    }

    override suspend fun getCompetenceById(id: Int): Competence? {
        return try {
            val entity = competenceDao.getCompetenceById(id)
            if (entity != null) {

                val levels = getLevelsFromDatabase(entity.id)
                val username = getCurrentUserName()

                // 1. Mapear
                val competence = competenceMapper.toDomain(entity, levels)

                // 2. Calcular
                // Mismo criterio que en getAllCompetences: la práctica no puntúa.
                val levelIds = levels.map { it.id }
                    .filterNot { LevelRules.esDePractica(it) }
                var calculatedProgress = 0f
                if (levelIds.isNotEmpty()) {


                    val idsDelBanco = mutableListOf<Int>()
                    for (levelId in levelIds) {
                        idsDelBanco += questionRepository
                            .getQuestionsByCompetenceAndLevel(entity.id, levelId)
                            .map { it.id }
                    }
                    val totalQuestions = idsDelBanco.size

                    // Acotado al banco actual: ver contarAciertosDeEstasPreguntas.
                    val correctQuestions = if (idsDelBanco.isEmpty()) 0
                    else progressDao.contarAciertosDeEstasPreguntas(username, levelIds, idsDelBanco)

                    if (totalQuestions > 0) {
                        calculatedProgress =
                            (correctQuestions.toFloat() / totalQuestions.toFloat())
                                .coerceIn(0f, 1f)
                    }
                }

                competence.copy(totalProgress = calculatedProgress)
            } else {
                getHardcodedCompetences().find { it.id == id }?.also { competence ->
                    saveCompetenceToDatabase(competence)
                }
            }
        } catch (e: Exception) {
            println("❌ Error en getCompetenceById: ${e.message}")
            getHardcodedCompetences().find { it.id == id }
        }
    }

    private suspend fun getLevelsFromDatabase(competenceId: Int): List<Level> {
        return try {
            val levelEntities = levelDao.getLevelsByCompetence(competenceId)
            println("🔍 getLevelsFromDatabase - competencia $competenceId: ${levelEntities.size} niveles")

            if (levelEntities.isEmpty()) {
                // Si no hay niveles en BD, crearlos
                println("⚠️ No hay niveles en BD para competencia $competenceId, creándolos...")
                createAndSaveLevelsForCompetence(competenceId)
            } else {
                // 🔥 --- LÓGICA DE CÁLCULO DE PROGRESO POR NIVEL ---
                val username = getCurrentUserName()

                // Mapea las entidades a Dominio, calculando su progreso real
                // Usamos map suspendido (con bucle for) porque los DAOs son suspend
                val levelsWithRealProgress = mutableListOf<Level>()

                for (entity in levelEntities) {
                    val levelId = entity.id

                    // 1. DENOMINADOR (preguntas que hoy tiene el nivel)
                    val idsDelNivel = questionRepository
                        .getQuestionsByCompetenceAndLevel(competenceId, levelId)
                        .map { it.id }
                    val totalQuestionsInLevel = idsDelNivel.size

                    // 2. NUMERADOR (aciertos, solo de esas preguntas)
                    //
                    // Aqui se vio el 137 %: el nivel tiene 8 preguntas y habia
                    // 11 intentos acertados, la mayoria apuntando a ids que
                    // dejaron de existir al traer el banco del panel.
                    val correctQuestionsInLevel = if (idsDelNivel.isEmpty()) 0
                    else progressDao.contarAciertosDeEstasPreguntas(
                        username, listOf(levelId), idsDelNivel
                    )

                    // 3. CALCULAR
                    val calculatedProgress = if (totalQuestionsInLevel > 0) {
                        (correctQuestionsInLevel.toFloat() / totalQuestionsInLevel.toFloat())
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    // 4. CREAR EL MODELO DE DOMINIO CON DATOS REALES
                    levelsWithRealProgress.add(
                        Level(
                            id = entity.id,
                            name = entity.name,
                            description = entity.description,
                            questions = emptyList(),
                            isLocked = entity.isLocked,
                            isCompleted = (calculatedProgress == 1f), // Se considera completo si es 100%
                            progress = calculatedProgress, // Usamos el progreso real
                            // Sin esto el nivel llega con el formato por
                            // defecto y la practica nunca enruta al tablero de
                            // parejas, aunque el panel lo haya dicho y Room lo
                            // tenga guardado.
                            formatoPractica = entity.formatoPractica,
                            ultimaPractica = entity.ultimaPractica
                        )
                    )
                }
                levelsWithRealProgress // Devolver la lista con progreso real
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo niveles de BD: ${e.message}")
            createLevelsForCompetence(competenceId, "Competencia $competenceId")
        }
    }

    private suspend fun createAndSaveLevelsForCompetence(competenceId: Int): List<Level> {
        return try {
            // Obtener nombre de la competencia para nombres de niveles
            val competence = competenceDao.getCompetenceById(competenceId)
            val competenceName = competence?.name ?: "Competencia $competenceId"

            println("🔧 Creando niveles para competencia $competenceId: $competenceName")

            // Crear niveles
            val levels = createLevelsForCompetence(competenceId, competenceName)

            // Guardar niveles en BD
            levels.forEach { level ->
                val levelEntity = LevelEntity(
                    id = level.id,
                    competenceId = competenceId,
                    name = level.name,
                    description = level.description,
                    isLocked = level.isLocked,
                    isCompleted = level.isCompleted,
                    progress = level.progress
                )
                levelDao.insertLevel(levelEntity)
                println("   ✅ Nivel ${level.id} guardado: ${level.name}")
            }

            println("✅ ${levels.size} niveles creados y guardados para competencia $competenceId")
            levels
        } catch (e: Exception) {
            println("❌ Error guardando niveles en BD: ${e.message}")
            createLevelsForCompetence(competenceId, "Competencia $competenceId")
        }
    }

    //  NUEVO MÉTDO: Guardar competencias en BD
    private suspend fun saveCompetencesToDatabase(competences: List<Competence>) {
        try {
            competences.forEach { competence ->
                saveCompetenceToDatabase(competence)
            }
            println("✅ ${competences.size} competencias guardadas en BD")
        } catch (e: Exception) {
            println("❌ Error guardando competencias en BD: ${e.message}")
        }
    }

    //  NUEVO MÉTDO: Guardar una competencia en BD
    private suspend fun saveCompetenceToDatabase(competence: Competence) {
        try {
            // Guardar competencia usando insertCompetence
            val competenceEntity = CompetenceEntity(
                id = competence.id,
                name = competence.name,
                description = competence.description,
                iconResId = competence.iconResId
            )
            competenceDao.insertCompetence(competenceEntity) // 🔥 USAR insertCompetence
            println("✅ Competencia ${competence.id} guardada: ${competence.name}")

            // Guardar niveles
            competence.levels.forEach { level ->
                val levelEntity = LevelEntity(
                    id = level.id,
                    competenceId = competence.id,
                    name = level.name,
                    description = level.description,
                    isLocked = level.isLocked,
                    isCompleted = level.isCompleted,
                    progress = level.progress
                )
                levelDao.insertLevel(levelEntity)
                println("   ✅ Nivel ${level.id} guardado: ${level.name}")
            }

            println("✅ Competencia ${competence.id} completada con ${competence.levels.size} niveles")
        } catch (e: Exception) {
            println("❌ Error guardando competencia ${competence.id} en BD: ${e.message}")
        }
    }

    override suspend fun getCompetencesByCategory(category: String): List<Competence> {
        // Como no tenemos categorías en los datos hardcodeados, devolvemos todas
        return getAllCompetences()
    }

    override suspend fun getFeaturedCompetences(): List<Competence> {
        return try {
            val allCompetences = getAllCompetences()
            // Podemos definir alguna lógica para destacados
            allCompetences.take(2) // Por ejemplo, las primeras 2
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchCompetences(query: String): List<Competence> {
        return try {
            val allCompetences = getAllCompetences()
            allCompetences.filter { competence ->
                competence.name.contains(query, ignoreCase = true) ||
                        competence.description.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getOverallProgress(): Float {
        return try {
            val competences = getAllCompetences()
            if (competences.isEmpty()) 0f
            else competences.map { it.totalProgress }.average().toFloat()
        } catch (e: Exception) {
            0f
        }
    }

    override suspend fun marcarNivelPracticado(levelId: Int) {
        runCatching {
            levelDao.marcarPracticado(levelId, System.currentTimeMillis())
            // Sin esto la marca queda escrita pero Inicio no la ve: el
            // catalogo se sirve de cache y solo se invalidaba al escribir
            // progreso, cosa que la practica no hace a proposito.
            catalogoCache.invalidar()
        }.onFailure { println("⚠️ No se pudo marcar el nivel $levelId: ${it.message}") }
    }

    override suspend fun updateCompetence(competence: Competence): Boolean {
        return try {
            val entity = competenceMapper.toEntity(competence)
            competenceDao.updateCompetence(entity) > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Crea los 3 niveles para una competencia (si no existen en la BD)
     */
    private fun createLevelsForCompetence(competenceId: Int, competenceName: String): List<Level> {
        return listOf(
            Level(
                id = generateLevelId(competenceId, 1), //  ID único
                name = "$competenceName - Básico",
                description = "Nivel básico de $competenceName",
                questions = emptyList(),
                isLocked = false,
                isCompleted = false,
                progress = 0f
            ),
            Level(
                id = generateLevelId(competenceId, 2), //  ID único
                name = "$competenceName - Intermedio",
                description = "Nivel intermedio de $competenceName",
                questions = emptyList(),
                isLocked = true,
                isCompleted = false,
                progress = 0f
            ),
            Level(
                id = generateLevelId(competenceId, 3), //  ID único
                name = "$competenceName - Avanzado",
                description = "Nivel avanzado de $competenceName",
                questions = emptyList(),
                isLocked = true,
                isCompleted = false,
                progress = 0f
            )
        )
    }

    /**
     * El catálogo de arranque, con el nivel de práctica delante de cada
     * competencia.
     *
     * Se añade aquí en vez de dentro de los cuatro literales para que haya un
     * único sitio que decida cómo es un nivel de práctica. Esto solo lo ve
     * quien instala la app limpia; a quien ya la tiene se los pone
     * `MIGRACION_11_12`.
     */
    private fun getHardcodedCompetences(): List<Competence> =
        catalogoDeArranque().map { competencia ->
            competencia.copy(levels = listOf(nivelDePractica(competencia.id)) + competencia.levels)
        }

    private fun nivelDePractica(competenceId: Int) = Level(
        // competencia * 100: termina en 00, que es lo que marca la práctica.
        id = generateLevelId(competenceId, 0),
        name = "Calentamiento",
        description = "Ítems cortos para coger ritmo. No cuenta para desbloquear: " +
            "al responder te dice si acertaste y por qué.",
        questions = emptyList(),
        // Practicar es la puerta de entrada, no un premio: nunca bloqueado.
        isLocked = false,
        isCompleted = false,
        progress = 0f
    )

    /**
     * Datos hardcodeados basados en tu CompetencyData
     */
    private fun catalogoDeArranque(): List<Competence> {
        return listOf(
            Competence(
                id = 1,
                name = "Lectura Crítica",
                description = "Desarrolla habilidades para analizar, interpretar y evaluar textos de manera crítica",
                iconResId = R.drawable.ic_lectura_critica,
                levels = listOf(
                    Level(
                        id = generateLevelId(1, 1), // 🔥 ID único: 101
                        name = "Nivel 1 – Comprensión literal",
                        description = "Identifica información explícita en textos",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(1, 2), // 🔥 ID único: 102
                        name = "Nivel 2 – Interpretación e inferencia",
                        description = "Identifica la organización y estructura de textos",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(1, 3), // 🔥 ID único: 103
                        name = "Nivel 3 – Análisis crítico y evaluación",
                        description = "Evalúa la calidad y credibilidad de textos",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    )
                ),
                totalProgress = 0f
            ),
            Competence(
                id = 2,
                name = "Razonamiento Cuantitativo",
                description = "Capacidad para comprender, analizar y resolver problemas que involucran información cuantitativa",
                iconResId = R.drawable.ic_razonamiento_critico,
                levels = listOf(
                    Level(
                        id = generateLevelId(2, 1), // 🔥 ID único: 201
                        name = "Interpretación",
                        description = "Comprende y transforma la información cuantitativa y esquemática presentada en distintos formatos",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(2, 2), // 🔥 ID único: 202
                        name = "Argumentación",
                        description = "Valida procedimientos y estrategias matemáticas utilizadas para dar solución a problemas",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(2, 3), // 🔥 ID único: 203
                        name = "Formulación y ejecución",
                        description = "Plantea e implementa estrategias que lleven a soluciones adecuadas en problemas cuantitativos",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    )
                ),
                totalProgress = 0f
            ),
            Competence(
                id = 3,
                name = "Inglés",
                description = "Desarrolla habilidades en comprensión, gramática y vocabulario en inglés",
                iconResId = R.drawable.ic_ingles,
                levels = listOf(
                    Level(
                        id = generateLevelId(3, 1), // 🔥 ID único: 301
                        name = "Feelings",
                        description = "Identifica emociones y sentimientos en inglés",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(3, 2), // 🔥 ID único: 302
                        name = "Complete the Conversations",
                        description = "Completa conversaciones cotidianas en inglés",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(3, 3), // 🔥 ID único: 303
                        name = "Complete the text",
                        description = "Completa textos con la palabra correcta",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(3, 4), // 🔥 ID único: 304
                        name = "Reading Comprehension",
                        description = "Comprension lectora avanzada en ingles",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    )
                ),
                totalProgress = 0f
            ),
            Competence(
                id = 4,
                name = "Competencias Ciudadanas",
                description = "Desarrolla habilidades para la participación ciudadana responsable",
                iconResId = R.drawable.ic_competencia_ciudadana,
                levels = listOf(
                    Level(
                        id = generateLevelId(4, 1), // 🔥 ID único: 401
                        name = "Nivel 1 – Conocimiento Constitucional",
                        description = "Conoce los derechos, deberes y principios fundamentales de la Constitución",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(4, 2), // 🔥 ID único: 402
                        name = "Nivel 2 – Análisis de Perspectivas",
                        description = "Reconoce diferentes perspectivas y comprende la multidimensionalidad de los problemas",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(4, 3), // 🔥 ID único: 403
                        name = "Nivel 3 – Análisis Crítico",
                        description = "Analiza y evalúa la pertinencia y solidez de argumentos y discursos",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    )
                ),
                totalProgress = 0f
            ),
            Competence(
                id = 5,
                name = "Comunicación Escrita",
                description = "Cohesión, precisión léxica y corrección gramatical: los ejes con los que el Icfes califica el texto escrito.",
                iconResId = R.drawable.ic_comunicacion_escrita,
                levels = listOf(
                    Level(
                        id = generateLevelId(5, 1), // ID único: 501
                        name = "Nivel 1 – Cohesión y corrección",
                        description = "Conectores, adverbios y artículos dentro de una frase con sentido.",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    )
                ),
                totalProgress = 0f
            )
        )
    }

    //  FUNCIÓN PARA GENERAR IDs ÚNICOS
    private fun generateLevelId(competenceId: Int, levelNumber: Int): Int {
        return competenceId * 100 + levelNumber
        // Ejemplos:
        // Competencia 1: 101, 102, 103
        // Competencia 2: 201, 202, 203
        // Competencia 3: 301, 302, 303, 304
        // Competencia 4: 401, 402, 403
    }
}