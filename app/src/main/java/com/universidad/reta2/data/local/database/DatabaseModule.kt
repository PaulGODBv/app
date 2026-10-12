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
            // El unico builder de la aplicacion. Las migraciones se declaran en
            // AppDatabase y se registran aqui, en un solo sitio.
            .addMigrations(
                AppDatabase.MIGRACION_9_10,
                AppDatabase.MIGRACION_10_11,
                AppDatabase.MIGRACION_11_12,
                AppDatabase.MIGRACION_12_13,
                AppDatabase.MIGRACION_13_14,
                AppDatabase.MIGRACION_14_15,
                AppDatabase.MIGRACION_15_16,
                AppDatabase.MIGRACION_16_17,
                AppDatabase.MIGRACION_17_18,
                AppDatabase.MIGRACION_18_19
            )
            // **Solo al BAJAR de version, nunca al subir.**
            //
            // Antes era `fallbackToDestructiveMigration()` a secas, que es
            // «si no se como migrar, borro y empiezo de cero». Eso no es un
            // respaldo: es perder la cuenta, el progreso y la racha de un
            // estudiante sin que nadie se entere. Ya paso una vez, en la v9,
            // cuando `password` paso a `password_hash` y todo el mundo tuvo
            // que registrarse otra vez.
            //
            // Ahora, si falta una migracion al subir de version, Room lanza
            // una excepcion. Es ruidoso a proposito: revienta en desarrollo,
            // que es donde se arregla, en vez de borrar en silencio en el
            // telefono de alguien. La base queda intacta.
            //
            // La bajada de version se mantiene destructiva porque no hay nada
            // mejor que hacer: una base escrita por una version mas nueva no
            // se puede leer hacia atras, y solo ocurre instalando a mano un
            // APK anterior, cosa que un estudiante no hace.
            .fallbackToDestructiveMigrationOnDowngrade()
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