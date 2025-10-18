package com.universidad.reta2.di

import com.universidad.reta2.domain.services.QuestionRandomizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideQuestionRandomizer(): QuestionRandomizer {
        return QuestionRandomizer
    }
}