package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val userStatsRepository: UserStatsRepository
) {

    suspend operator fun invoke(
        questionId: Int,
        isCorrect: Boolean,
        timeSpent: Int,
        levelId: Int,
        competenceId: Int? = null,
        isLevelCompleted: Boolean = false,
        levelScore: Int = 0,
        totalQuestions: Int = 0
    ) {
        try {
            // 1. Registrar la respuesta individual
            progressRepository.recordQuestionAttempt(
                questionId = questionId,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpent,
                levelId = levelId
            )

            // 2. Actualizar estadísticas del usuario
            val currentStats = userStatsRepository.getUserStats().first()
            val updatedStats = currentStats.copy(
                totalQuestionsAnswered = currentStats.totalQuestionsAnswered + 1,
                totalPracticeTimeSeconds = currentStats.totalPracticeTimeSeconds + timeSpent,
                dailyPracticeTime = currentStats.dailyPracticeTime + timeSpent
            )
            userStatsRepository.updateUserStats(updatedStats)

            // 3. Actualizar racha
            if (isCorrect) {
                userStatsRepository.incrementStreak()
            } else {
                userStatsRepository.resetStreak()
            }

            // Desbloquear siguiente nivel si se completó
            if (isLevelCompleted && competenceId != null) {
                val nextLevelUnlocked = progressRepository.completeLevelAndUnlockNext(
                    competenceId = competenceId,
                    levelId = levelId,
                    score = levelScore,
                    totalQuestions = totalQuestions,
                    timeSpent = timeSpent
                )

                if (nextLevelUnlocked) {
                    println("SUCCESS - Siguiente nivel desbloqueado automaticamente")
                } else {
                    println("INFO - No habia siguiente nivel para desbloquear")
                }
            }

            println("SUCCESS - UpdateProgressUseCase ejecutado correctamente")

        } catch (e: Exception) {
            println("ERROR - Error en UpdateProgressUseCase: ${e.message}")
        }
    }
}
