package com.universidad.reta2.di

import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.usecases.UpdateStreakUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StatsUseCaseModule {

    @Provides
    @Singleton
    fun provideGetUserStatsUseCase(
        userStatsRepository: UserStatsRepository
    ): GetUserStatsUseCase {
        return GetUserStatsUseCase(userStatsRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateStreakUseCase(
        userStatsRepository: UserStatsRepository
    ): UpdateStreakUseCase {
        return UpdateStreakUseCase(userStatsRepository)
    }
}
