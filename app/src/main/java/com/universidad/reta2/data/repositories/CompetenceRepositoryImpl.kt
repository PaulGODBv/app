package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.dao.CompetenceDao
import com.universidad.reta2.data.local.mappers.CompetenceMapper
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.R
import javax.inject.Inject

class CompetenceRepositoryImpl @Inject constructor(
    private val competenceDao: CompetenceDao,
    private val competenceMapper: CompetenceMapper
) : CompetenceRepository {

    override suspend fun getAllCompetences(): List<Competence> {
        return try {
            // Obtener las entidades de la base de datos
            val competenceEntities = competenceDao.getAllCompetences()

            // Si no hay datos en la BD, usar los datos hardcodeados
            if (competenceEntities.isEmpty()) {
                getHardcodedCompetences()
            } else {
                // Convertir entidades a modelos de dominio
                competenceEntities.map { entity ->
                    val levels = createLevelsForCompetence(entity.id, entity.name)
                    competenceMapper.toDomain(entity, levels)
                }
            }
        } catch (e: Exception) {
            // En caso de error, devolver datos hardcodeados
            getHardcodedCompetences()
        }
    }

    override suspend fun getCompetenceById(id: Int): Competence? {
        return try {
            val entity = competenceDao.getCompetenceById(id)
            if (entity != null) {
                val levels = createLevelsForCompetence(entity.id, entity.name)
                competenceMapper.toDomain(entity, levels)
            } else {
                // Buscar en datos hardcodeados
                getHardcodedCompetences().find { it.id == id }
            }
        } catch (e: Exception) {
            getHardcodedCompetences().find { it.id == id }
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
                id = 1,
                name = "$competenceName - Básico",
                description = "Nivel básico de $competenceName",
                questions = emptyList(),
                isLocked = false,
                isCompleted = false,
                progress = 0f
            ),
            Level(
                id = 2,
                name = "$competenceName - Intermedio",
                description = "Nivel intermedio de $competenceName",
                questions = emptyList(),
                isLocked = true,
                isCompleted = false,
                progress = 0f
            ),
            Level(
                id = 3,
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
                        id = 1,
                        name = "🟢 Nivel 1 – Comprensión literal",
                        description = "Identifica información explícita en textos",
                        questions = emptyList(), // Las preguntas reales están en CompetencyData
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 2,
                        name = "🟡 Nivel 2 – Interpretación e inferencia",
                        description = "Identifica la organización y estructura de textos",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 3,
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
                        id = 1,
                        name = "Interpretación",
                        description = "Comprende y transforma la información cuantitativa y esquemática presentada en distintos formatos",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 2,
                        name = "Argumentación",
                        description = "Valida procedimientos y estrategias matemáticas utilizadas para dar solución a problemas",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 3,
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
                        id = 1,
                        name = "Feelings",
                        description = "Identifica emociones y sentimientos en inglés",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 2,
                        name = "Complete the Conversations",
                        description = "Completa conversaciones cotidianas en inglés",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 3,
                        name = "Complete the text",
                        description = "Completa textos con la palabra correcta",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 4,
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
                        id = 1,
                        name = "🟢 Nivel 1 – Conocimiento Constitucional",
                        description = "Conoce los derechos, deberes y principios fundamentales de la Constitución",
                        questions = emptyList(),
                        isLocked = false,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 2,
                        name = "🟡 Nivel 2 – Análisis de Perspectivas",
                        description = "Reconoce diferentes perspectivas y comprende la multidimensionalidad de los problemas",
                        questions = emptyList(),
                        isLocked = true,
                        isCompleted = false,
                        progress = 0f
                    ),
                    Level(
                        id = 3,
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
}