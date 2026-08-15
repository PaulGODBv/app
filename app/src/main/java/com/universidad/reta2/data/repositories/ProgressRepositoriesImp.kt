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
    @ApplicationContext private val context: Context
) : ProgressRepository {

    private fun getCurrentUserName(): String {
        return try {
            val username = SessionManager.getCurrentUsername(context)
            if (username.isNullOrEmpty()) {
                println("⚠️ No hay usuario logueado, usando usuario por defecto")
                "usuario_invitado"
            } else {
                println("✅ Usuario obtenido de SessionManager: $username")
                username
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo usuario de SessionManager: ${e.message}")
            "usuario_invitado" // Fallback seguro
        }
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
        val username = getCurrentUserName()
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
            ?: LevelStats(levelId = levelId, totalAttempts = 0, correctAttempts = 0, averageTime = 0.0)
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
            println("🎯 DIAGNÓSTICO INICIO - completeLevelAndUnlockNext")
            println("   📊 Score: $score/$totalQuestions")

            // 🔥 VERIFICAR PROGRESO MÍNIMO PARA COMPLETAR (80%)
            val progress = score.toFloat() / totalQuestions
            val minProgressRequired = 0.8f // 80% mínimo para completar

            println("   📈 Progreso: ${(progress * 100).toInt()}%")
            println("   🎯 Mínimo requerido: ${(minProgressRequired * 100).toInt()}%")

            if (progress < minProgressRequired) {
                println("❌ Progreso insuficiente para completar nivel")
                return false
            }

            // 🔥 VERIFICAR SI EL NIVEL ACTUAL EXISTE
            val levels = levelDao.getLevelsByCompetence(competenceId)
            println("   🔍 Niveles encontrados en competencia: ${levels.size}")
            levels.forEach { level ->
                println("      - Nivel ${level.id}: '${level.name}' (locked: ${level.isLocked}, completed: ${level.isCompleted})")
            }

            val currentLevelExists = levels.any { it.id == levelId }
            println("   ✅ Nivel actual existe: $currentLevelExists")

            if (!currentLevelExists) {
                println("❌ ERROR: El nivel $levelId no existe")
                return false
            }

            // 🔥 VERIFICAR PROGRESO ACTUAL ANTES DE ACTUALIZAR
            val progressBefore = progressDao.getLevelProgress(username, competenceId, levelId)
            println("   📈 Progreso antes: ${progressBefore?.isCompleted ?: "NO EXISTE"}")

            // 1. Guardar progreso del nivel actual
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
            println("✅ Progreso guardado para nivel $levelId")

            // 🔥 VERIFICAR QUE SE GUARDÓ
            val progressAfter = progressDao.getLevelProgress(username, competenceId, levelId)
            println("   📈 Progreso después: ${progressAfter?.isCompleted ?: "NO EXISTE"}")

            // 2. Actualizar LevelEntity
            val rowsUpdated = levelDao.updateLevelProgress(
                competenceId = competenceId,
                levelId = levelId,
                isCompleted = true,
                progress = 1.0f
            )
            println("✅ LevelEntity actualizado: $rowsUpdated filas afectadas")

            // 🔥 VERIFICAR QUE LevelEntity SE ACTUALIZÓ
            val updatedLevel = levelDao.getLevel(competenceId, levelId)
            println("   🔍 LevelEntity después: locked=${updatedLevel?.isLocked}, completed=${updatedLevel?.isCompleted}")

            // 3. Desbloquear siguiente nivel
            val nextLevelUnlocked = unlockNextLevel(competenceId, levelId)

            println("🎯 DIAGNÓSTICO FINAL - Nivel $levelId completado. Siguiente desbloqueado: $nextLevelUnlocked")
            nextLevelUnlocked

        } catch (e: Exception) {
            println("❌ ERROR CRÍTICO - completeLevelAndUnlockNext: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // Lógica para desbloquear siguiente nivel
    private suspend fun unlockNextLevel(competenceId: Int, currentLevelId: Int): Boolean {
        return try {
            println("🔓 DIAGNÓSTICO - unlockNextLevel")
            println("   📊 Competencia: $competenceId, Nivel actual: $currentLevelId")

            val levels = levelDao.getLevelsByCompetence(competenceId)
            println("   🔍 Todos los niveles:")
            levels.forEachIndexed { index, level ->
                println("      $index. Nivel ${level.id}: '${level.name}' (locked: ${level.isLocked})")
            }

            val currentLevelIndex = levels.indexOfFirst { it.id == currentLevelId }
            println("   📍 Índice del nivel actual: $currentLevelIndex")

            if (currentLevelIndex == -1) {
                println("❌ Nivel actual no encontrado")
                return false
            }

            if (currentLevelIndex < levels.size - 1) {
                val nextLevel = levels[currentLevelIndex + 1]
                println("   🔍 Siguiente nivel: ${nextLevel.id} - '${nextLevel.name}'")
                println("   🔓 Estado actual del siguiente nivel: locked=${nextLevel.isLocked}")

                if (nextLevel.isLocked) {
                    println("   🚀 Intentando desbloquear nivel ${nextLevel.id}...")

                    // Intentar desbloquear
                    levelDao.updateLevelLockStatus(competenceId, nextLevel.id, false)

                    // 🔥 VERIFICAR SI REALMENTE SE DESBLOQUEÓ
                    val verificationLevel = levelDao.getLevel(competenceId, nextLevel.id)
                    val wasUnlocked = verificationLevel?.isLocked == false

                    println("   ✅ Verificación después de desbloquear: locked=${verificationLevel?.isLocked}")
                    println("   🎯 Resultado desbloqueo: $wasUnlocked")

                    if (wasUnlocked) {
                        println("   🎉 ÉXITO - Nivel ${nextLevel.id} DESBLOQUEADO")
                    } else {
                        println("   ❌ FALLO - No se pudo desbloquear nivel ${nextLevel.id}")
                    }

                    return wasUnlocked
                } else {
                    println("   ℹ️ El nivel ${nextLevel.id} ya estaba desbloqueado")
                    return false
                }
            } else {
                println("   ℹ️ No hay siguiente nivel - $currentLevelId es el último")
                return false
            }

        } catch (e: Exception) {
            println("❌ ERROR - unlockNextLevel: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // Méodo para obtener niveles con estado actualizado
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

    // Méodo temporal
    suspend fun debugInitialState(competenceId: Int) {
        val username = getCurrentUserName()
        println("🔍 DEBUG ESTADO INICIAL - Competencia: $competenceId")

        val levels = levelDao.getLevelsByCompetence(competenceId)
        println("   Niveles en BD:")
        levels.forEach { level ->
            val progress = progressDao.getLevelProgress(username, competenceId, level.id)
            println("   - Nivel ${level.id}: locked=${level.isLocked}, progress=${progress?.isCompleted ?: "NO"}")
        }
    }

}