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
        timeSpent: Int
    ) {
        // 1. Registrar la respuesta en el progreso
        progressRepository.recordQuestionAttempt(
            questionId = questionId,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpent
        )

        // 2. Actualizar estadísticas del usuario
        val currentStats = userStatsRepository.getUserStats().first()
        val updatedStats = currentStats.copy(
            totalQuestionsAnswered = currentStats.totalQuestionsAnswered + 1,
            totalPracticeTimeSeconds = currentStats.totalPracticeTimeSeconds + timeSpent,
            dailyPracticeTime = currentStats.dailyPracticeTime + timeSpent
        )

        userStatsRepository.updateUserStats(updatedStats)

        // 3. Actualizar racha si es necesario
        if (isCorrect) {
            userStatsRepository.incrementStreak()
        } else {
            userStatsRepository.resetStreak()
        }
    }
}