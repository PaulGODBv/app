package com.universidad.reta2.di

import android.content.Context
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.UserDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.dao.QuestionDao
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.repositories.UserRepository
import com.universidad.reta2.data.repositories.UserRepositoryImpl
import com.universidad.reta2.data.repositories.ProgressRepositoryImpl
import com.universidad.reta2.data.repositories.SessionRepositoryImpl
import com.universidad.reta2.domain.repositories.SessionRepository
import com.universidad.reta2.domain.services.StatsInitializer
import com.universidad.reta2.data.repositories.UserStatsRepositoriesImp
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.data.local.dao.CompetenceDao
import com.universidad.reta2.data.local.dao.LevelDao
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
        levelDao: LevelDao,
        @ApplicationContext context: Context,
    ): ProgressRepository {
        return ProgressRepositoryImpl(
            progressDao = progressDao,
            userStatsDao = userStatsDao,
            levelDao = levelDao,
            context = context,
        )
    }


    @Provides
    @Singleton
    fun provideUserStatsRepository(
        userStatsDao: UserStatsDao,
        statsInitializer: StatsInitializer,
        @ApplicationContext context: Context
    ): UserStatsRepository {
        return UserStatsRepositoriesImp(userStatsDao, StatsInitializer(userStatsDao), context)
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
        competenceMapper: CompetenceMapper,
        levelDao: LevelDao,
        questionDao: QuestionDao,
        progressDao: ProgressDao,
        @ApplicationContext context: Context,
        questionRepository: QuestionRepository
    ): CompetenceRepository {
        return CompetenceRepositoryImpl(
            competenceDao = competenceDao,
            competenceMapper = competenceMapper,
            levelDao = levelDao,
            questionDao = questionDao,
            progressDao = progressDao,
            context = context,
            questionRepository = questionRepository
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