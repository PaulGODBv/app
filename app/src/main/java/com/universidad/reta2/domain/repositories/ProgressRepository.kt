package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.models.UserProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {

    // Registrar intento de pregunta individual
    suspend fun recordQuestionAttempt(
        questionId: Int,
        isCorrect: Boolean,
        timeSpentSeconds: Int
    )

    // Obtener progreso de un nivel específico
    suspend fun getLevelProgress(
        competenceId: Int,
        levelId: Int
    ): LevelProgress?

    // Guardar progreso completo de un nivel
    suspend fun saveLevelProgress(progress: LevelProgress)

    // Obtener historial de progreso del usuario
    fun getUserProgress(): Flow<List<UserProgress>>

    // Obtener preguntas correctas/incorrectas por nivel
    suspend fun getLevelStats(levelId: Int): LevelStats

    // Reiniciar progreso de un nivel
    suspend fun resetLevelProgress(competenceId: Int, levelId: Int)
}

