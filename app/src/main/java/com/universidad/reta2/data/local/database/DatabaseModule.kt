package com.universidad.reta2.data.local.database


import android.content.Context
import androidx.room.Room
import com.universidad.reta2.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "reta2_database"
        )
            // La app se sirve de este builder, no del de AppDatabase.getDatabase().
            // Si la migración se registra solo en uno de los dos, el otro cae en
            // la vía destructiva y borra el progreso sin avisar.
            .addMigrations(
                AppDatabase.MIGRACION_9_10,
                AppDatabase.MIGRACION_10_11,
                AppDatabase.MIGRACION_11_12,
                AppDatabase.MIGRACION_12_13,
                AppDatabase.MIGRACION_13_14,
                AppDatabase.MIGRACION_14_15,
                AppDatabase.MIGRACION_15_16
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideUserStatsDao(database: AppDatabase): UserStatsDao = database.userStatsDao()

    @Provides
    fun provideProgressDao(database: AppDatabase): ProgressDao = database.progressDao()

    @Provides
    fun provideCompetenceDao(database: AppDatabase): CompetenceDao = database.competenceDao()

    @Provides
    fun provideQuestionDao(database: AppDatabase): QuestionDao = database.questionDao()
    @Provides
    fun provideLevelDao(database: AppDatabase): LevelDao = database.levelDao()

}