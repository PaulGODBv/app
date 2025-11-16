package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.dao.CompetenceDao
import com.universidad.reta2.data.local.dao.LevelDao
import com.universidad.reta2.data.local.mappers.CompetenceMapper
import com.universidad.reta2.data.local.entities.LevelEntity
import com.universidad.reta2.data.local.entities.CompetenceEntity
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.R
import javax.inject.Inject

class CompetenceRepositoryImpl @Inject constructor(
    private val competenceDao: CompetenceDao,
    private val levelDao: LevelDao,
    private val competenceMapper: CompetenceMapper
) : CompetenceRepository {

    override suspend fun getAllCompetences(): List<Competence> {
        return try {
            // Obtener las entidades de la base de datos
            val competenceEntities = competenceDao.getAllCompetences()

            // Si no hay datos en la BD, usar los datos hardcodeados
            if (competenceEntities.isEmpty()) {
                val hardcodedCompetences = getHardcodedCompetences()
                // 🔥 GUARDAR LOS DATOS HARCODEADOS EN LA BD
                saveCompetencesToDatabase(hardcodedCompetences)
                hardcodedCompetences
            } else {
                // Convertir entidades a modelos de dominio
                competenceEntities.map { entity ->
                    val levels = getLevelsFromDatabase(entity.id)
                    competenceMapper.toDomain(entity, levels)
                }
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
                competenceMapper.toDomain(entity, levels)
            } else {
                // Buscar en datos hardcodeados
                getHardcodedCompetences().find { it.id == id }?.also { competence ->
                    // 🔥 GUARDAR EN BD SI SE ENCUENTRA EN HARCODEADOS
                    saveCompetenceToDatabase(competence)
                }
            }
        } catch (e: Exception) {
            println("❌ Error en getCompetenceById: ${e.message}")
            getHardcodedCompetences().find { it.id == id }
        }
    }

    // 🔥 NUEVO MÉTDO: Obtener niveles desde la base de datos
    private suspend fun getLevelsFromDatabase(competenceId: Int): List<Level> {
        return try {
            val levelEntities = levelDao.getLevelsByCompetence(competenceId)
            println("🔍 getLevelsFromDatabase - competencia $competenceId: ${levelEntities.size} niveles")

            if (levelEntities.isEmpty()) {
                // Si no hay niveles en BD, crearlos
                println("⚠️ No hay niveles en BD para competencia $competenceId, creándolos...")
                createAndSaveLevelsForCompetence(competenceId)
            } else {
                // Convertir entidades a modelos de dominio
                levelEntities.map { entity ->
                    Level(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        questions = emptyList(),
                        isLocked = entity.isLocked,
                        isCompleted = entity.isCompleted,
                        progress = entity.progress
                    )
                }
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo niveles de BD: ${e.message}")
            createLevelsForCompetence(competenceId, "Competencia $competenceId")
        }
    }

    // 🔥 NUEVO MÉTDO: Crear y guardar niveles en BD
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
     * Datos hardcodeados basados en tu CompetencyData
     */
    private fun getHardcodedCompetences(): List<Competence> {
        return listOf(
            Competence(
                id = 1,
                name = "Lectura Crítica",
                description = "Desarrolla habilidades para analizar, interpretar y evaluar textos de manera crítica",
                iconResId = R.drawable.ic_lectura_critica,
                levels = listOf(
                    Level(
                        id = generateLevelId(1, 1), // 🔥 ID único: 101
                        name = "🟢 Nivel 1 – Comprensión literal",
                        description = "Identifica información explícita en textos",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(1, 2), // 🔥 ID único: 102
                        name = "🟡 Nivel 2 – Interpretación e inferencia",
                        description = "Identifica la organización y estructura de textos",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(1, 3), // 🔥 ID único: 103
                        name = "🔴 Nivel 3 – Análisis crítico y evaluación",
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
                        name = "🟢 Nivel 1 – Conocimiento Constitucional",
                        description = "Conoce los derechos, deberes y principios fundamentales de la Constitución",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(4, 2), // 🔥 ID único: 402
                        name = "🟡 Nivel 2 – Análisis de Perspectivas",
                        description = "Reconoce diferentes perspectivas y comprende la multidimensionalidad de los problemas",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = generateLevelId(4, 3), // 🔥 ID único: 403
                        name = "🔴 Nivel 3 – Análisis Crítico",
                        description = "Analiza y evalúa la pertinencia y solidez de argumentos y discursos",
                        questions = emptyList(),
                        isLocked = true,
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