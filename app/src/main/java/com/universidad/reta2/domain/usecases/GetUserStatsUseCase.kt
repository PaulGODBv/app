package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.repositories.UserStatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val userStatsRepository: UserStatsRepository
) {
    operator fun invoke(): Flow<UserStats> {
        return userStatsRepository.getUserStats()
    }
}