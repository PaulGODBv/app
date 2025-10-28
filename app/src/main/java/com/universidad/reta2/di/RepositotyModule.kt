package com.universidad.reta2.di

import android.content.Context
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.UserDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.repositories.UserRepository
import com.universidad.reta2.data.repositories.UserRepositoryImpl
import com.universidad.reta2.data.repositories.ProgressRepositoryImpl
import com.universidad.reta2.data.repositories.SessionRepositoryImpl
import com.universidad.reta2.domain.repositories.SessionRepository
import com.universidad.reta2.data.repositories.UserStatsRepositoriesImp
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.data.local.dao.CompetenceDao
import com.universidad.reta2.data.local.mappers.CompetenceMapper
import com.universidad.reta2.data.local.mappers.UserMapper
import com.universidad.reta2.data.repositories.CompetenceRepositoryImpl
import com.universidad.reta2.data.repositories.QuestionRepositoryImpl
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.QuestionRepository
import com.universidad.reta2.domain.usecases.GetQuestionsUseCase
import dagger.Module
import dagger.Provides
import dagger.Binds
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
        userStatsDao: UserStatsDao,
        @ApplicationContext context: Context,
        sessionManager: SessionManager
    ): ProgressRepository {
        return ProgressRepositoryImpl(progressDao, userStatsDao, context, sessionManager)
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
    fun provideUserRepository(
        userDao: UserDao,
        userStatsDao: UserStatsDao,
        mapper: UserMapper
    ): UserRepository {

        return UserRepositoryImpl(
            userDao = userDao,
            userStatsDao = userStatsDao,
            mapper = mapper
        )
    }

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager
    }

    @Provides
    @Singleton
    fun provideCompetenceRepository(
        competenceDao: CompetenceDao,
        competenceMapper: CompetenceMapper
    ): CompetenceRepository {
        return CompetenceRepositoryImpl(
            competenceDao = competenceDao,
            competenceMapper = competenceMapper
        )
    }

    @Provides
    @Singleton
    fun provideCompetenceMapper(): CompetenceMapper {
        return CompetenceMapper
    }

    @Provides
    @Singleton
    fun provideUserMapper(): UserMapper {
        return UserMapper
    }

    @Provides
    @Singleton
    fun provideQuestionRepository(): QuestionRepository {
        return QuestionRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideGetQuestionsUseCase(questionRepository: QuestionRepository): GetQuestionsUseCase {
        return GetQuestionsUseCase(questionRepository)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class SessionModule {
        @Binds
        @Singleton
        abstract fun bindSessionRepository(
            impl: SessionRepositoryImpl
        ): SessionRepository
    }
}