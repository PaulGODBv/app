package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.dao.LevelDao
import com.universidad.reta2.data.local.mappers.ProgressMapper
import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.models.LevelStats
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.data.local.entities.QuestionAttemptEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.content.Context
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext


class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
    private val userStatsDao: UserStatsDao,
    private val levelDao: LevelDao,
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) : ProgressRepository {

    private fun getCurrentUserName(): String{
        return sessionManager.getCurrentUsername(context) ?: "invitado"
    }
    override suspend fun recordQuestionAttempt(
        questionId: Int,
        isCorrect: Boolean,
        timeSpentSeconds: Int,
        levelId: Int
    ) {
        val username = getCurrentUserName()

        val attempt = QuestionAttemptEntity(
            username = username,
            questionId = questionId,
            levelId = levelId,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpentSeconds
        )

        progressDao.insertQuestionAttempt(attempt)
    }

    override suspend fun getLevelProgress(
        competenceId: Int,
        levelId: Int
    ): LevelProgress? {
        val username = "usuario_actual"
        return progressDao.getLevelProgress(username, competenceId, levelId)?.let {
            ProgressMapper.toDomain(it)
        }
    }

    override suspend fun saveLevelProgress(progress: LevelProgress) {
        val username = getCurrentUserName()
        val entity = ProgressMapper.toEntity(progress, username)
        progressDao.saveLevelProgress(entity)
    }


    override fun getUserProgress(): Flow<List<LevelProgress>> {
        val username = getCurrentUserName()
        return progressDao.getUserProgress(username).map { entities ->
            entities.map { ProgressMapper.toDomain(it) }
        }
    }

    override suspend fun getLevelStats(levelId: Int): LevelStats {
        val username = getCurrentUserName()
        return progressDao.getLevelStats(username, levelId)
    }

    override suspend fun resetLevelProgress(competenceId: Int, levelId: Int) {
        val username = getCurrentUserName()
        progressDao.deleteLevelProgress(username, competenceId, levelId)
    }

    override suspend fun completeLevelAndUnlockNext(
        competenceId: Int,
        levelId: Int,
        score: Int,
        totalQuestions: Int,
        timeSpent: Int
    ): Boolean {
        return try {
            val username = getCurrentUserName()
            println("DEBUG - Completando nivel $levelId para usuario $username")

            // 1. Guardar progreso del nivel actual como completado
            val currentProgress = LevelProgress(
                competenceId = competenceId,
                levelId = levelId,
                questionsCompleted = totalQuestions,
                totalQuestions = totalQuestions,
                isCompleted = true,
                score = score,
                timeSpent = timeSpent,
                currentQuestionIndex = 0,
                answeredQuestions = emptyList(),
                correctAnswers = List(score) { it }
            )

            saveLevelProgress(currentProgress)

            // 2. Actualizar el nivel como completado en la tabla levels (usando Float)
            val rowsUpdated = levelDao.updateLevelProgress(
                competenceId = competenceId,
                levelId = levelId,
                isCompleted = true,
                progress = 1.0f // 100% completado - usando Float
            )
            println("DEBUG - Nivel actualizado en BD: $rowsUpdated filas afectadas")

            // 3. Desbloquear siguiente nivel automaticamente
            val nextLevelUnlocked = unlockNextLevel(competenceId, levelId)

            println("SUCCESS - Nivel $levelId completado. Siguiente nivel desbloqueado: $nextLevelUnlocked")
            nextLevelUnlocked

        } catch (e: Exception) {
            println("ERROR - Error completando nivel: ${e.message}")
            false
        }
    }

    // Lógica para desbloquear siguiente nivel
    private suspend fun unlockNextLevel(competenceId: Int, currentLevelId: Int): Boolean {
        return try {
            // Obtener todos los niveles de la competencia
            val levels = levelDao.getLevelsByCompetence(competenceId)
            println("DEBUG - Niveles encontrados en competencia $competenceId: ${levels.size}")

            // Encontrar el índice del nivel actual
            val currentLevelIndex = levels.indexOfFirst { it.id == currentLevelId }

            if (currentLevelIndex == -1) {
                println("ERROR - Nivel actual $currentLevelId no encontrado en competencia $competenceId")
                return false
            }

            // Verificar si hay siguiente nivel
            if (currentLevelIndex < levels.size - 1) {
                val nextLevel = levels[currentLevelIndex + 1]
                println("DEBUG - Siguiente nivel encontrado: ${nextLevel.id} - ${nextLevel.name} (locked: ${nextLevel.isLocked})")

                // Solo desbloquear si está bloqueado
                if (nextLevel.isLocked) {
                    try {
                        // Desbloquear el siguiente nivel en la base de datos
                        levelDao.updateLevelLockStatus(competenceId, nextLevel.id, false)

                        // Verificar si realmente se desbloqueó consultando la BD
                        val updatedLevel = levelDao.getLevel(competenceId, nextLevel.id)
                        val wasUnlocked = updatedLevel?.isLocked == false

                        if (wasUnlocked) {
                            println("SUCCESS - Nivel ${nextLevel.id} (${nextLevel.name}) desbloqueado automaticamente")
                            true
                        } else {
                            println("WARNING - No se pudo desbloquear el nivel ${nextLevel.id} en la BD")
                            false
                        }
                    } catch (e: Exception) {
                        println("ERROR - Error al actualizar nivel en BD: ${e.message}")
                        false
                    }
                } else {
                    println("INFO - Nivel ${nextLevel.id} ya estaba desbloqueado")
                    false
                }
            } else {
                println("INFO - No hay siguiente nivel - $currentLevelId es el ultimo nivel")
                false
            }

        } catch (e: Exception) {
            println("ERROR - Error desbloqueando siguiente nivel: ${e.message}")
            false
        }
    }

    //  Méodo para obtener niveles con estado actualizado
    override suspend fun getLevelsWithProgress(competenceId: Int): List<Level> {
        val levelEntities = levelDao.getLevelsByCompetence(competenceId)
        val username = getCurrentUserName()

        return levelEntities.map { levelEntity ->
            // Obtener progreso actual del nivel
            val progressEntity = progressDao.getLevelProgress(username, competenceId, levelEntity.id)

            com.universidad.reta2.domain.models.Level(
                id = levelEntity.id,
                name = levelEntity.name,
                description = levelEntity.description,
                questions = emptyList(),
                isLocked = levelEntity.isLocked,
                isCompleted = progressEntity?.isCompleted ?: false,
                progress = progressEntity?.let {
                    // Calcular progreso como Float
                    if (it.totalQuestions > 0) {
                        it.questionsCompleted.toFloat() / it.totalQuestions.toFloat()
                    } else {
                        0.0f
                    }
                } ?: 0.0f
            )
        }
    }
}
