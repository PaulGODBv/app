package com.universidad.reta2.di

import com.universidad.reta2.domain.services.QuestionRandomizer
import com.universidad.reta2.domain.usecases.GetRandomizedQuestionsUseCase
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.usecases.UpdateProgressUseCase
import com.universidad.reta2.domain.usecases.UpdateStreakUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetRandomizedQuestionsUseCase(): GetRandomizedQuestionsUseCase {
        return GetRandomizedQuestionsUseCase()
    }


    @Provides
    @Singleton
    fun provideUpdateProgressUseCase(
        progressRepository: com.universidad.reta2.domain.repositories.ProgressRepository,
        userStatsRepository: com.universidad.reta2.domain.repositories.UserStatsRepository
    ): UpdateProgressUseCase {
        return UpdateProgressUseCase(progressRepository, userStatsRepository)
    }


    @Provides
    @Singleton
    fun provideQuestionRandomizer(): QuestionRandomizer {
        return QuestionRandomizer
    }
}
