package com.universidad.reta2.di

import android.content.Context
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.data.preferences.UserRepository
import com.universidad.reta2.data.repositories.ProgressRepositoryImpl
import com.universidad.reta2.data.repositories.UserStatsRepositoriesImp
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProgressRepository(
        progressDao: ProgressDao,
        userStatsDao: UserStatsDao
    ): ProgressRepository {
        return ProgressRepositoryImpl(progressDao, userStatsDao)
    }

    @Provides
    @Singleton
    fun provideUserStatsRepository(
        userStatsDao: UserStatsDao
    ): UserStatsRepository {
        return UserStatsRepositoriesImp(userStatsDao)
    }

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context): UserRepository {
        return UserRepository(context)
    }

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager
    }
}