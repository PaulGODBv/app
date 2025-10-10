package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.repositories.UserStatsRepository
import javax.inject.Inject

class UpdateStreakUseCase @Inject constructor(
    private val userStatsRepository: UserStatsRepository
) {
    suspend operator fun invoke(isCorrect: Boolean) {
        if (isCorrect) {
            userStatsRepository.incrementStreak()
        } else {
            userStatsRepository.resetStreak()
        }
    }
}
