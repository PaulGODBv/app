package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.LevelStats
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {

    // Registrar intento de pregunta individual
    suspend fun recordQuestionAttempt(
        questionId: Int,
        isCorrect: Boolean,
        timeSpentSeconds: Int,
        levelId: Int = 1,
        // Por defecto evaluacion, que es lo unico que se registraba antes de la
        // v20: asi, quien no lo pase sigue escribiendo lo mismo que escribia.
        esPractica: Boolean = false
    )

    // Obtener progreso de un nivel específico
    suspend fun getLevelProgress(
        competenceId: Int,
        levelId: Int
    ): LevelProgress?

    suspend fun completeLevelAndUnlockNext(
        competenceId: Int,
        levelId: Int,
        score: Int,
        totalQuestions: Int,
        timeSpent: Int
    ): Boolean

    // Guardar progreso completo de un nivel
    suspend fun saveLevelProgress(progress: LevelProgress)

    // Obtener historial de progreso del usuario
    fun getUserProgress(): Flow<List<LevelProgress>>

    // Obtener preguntas correctas/incorrectas por nivel
    suspend fun getLevelStats(levelId: Int): LevelStats

    // Reiniciar progreso de un nivel
    suspend fun resetLevelProgress(competenceId: Int, levelId: Int)

    suspend fun getLevelsWithProgress(competenceId: Int): List<Level>
}

