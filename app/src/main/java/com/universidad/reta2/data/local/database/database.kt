package com.universidad.reta2.data.local.database

import androidx.room.Database
import com.universidad.reta2.data.local.entities.UserEntity
import com.universidad.reta2.data.local.entities.UserStatsEntity
import com.universidad.reta2.data.local.entities.CompetenceEntity
import com.universidad.reta2.data.local.entities.LevelEntity
import com.universidad.reta2.data.local.entities.QuestionEntity
import com.universidad.reta2.data.local.entities.QuestionOptionEntity
import com.universidad.reta2.data.local.entities.ProgressEntity
import com.universidad.reta2.data.local.entities.QuestionAttemptEntity
import com.universidad.reta2.data.local.entities.LevelProgressEntity
import androidx.room.RoomDatabase
import com.universidad.reta2.data.local.dao.UserDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.QuestionDao
import com.universidad.reta2.data.local.dao.CompetenceDao



@Database(
    entities = [
        UserEntity::class,
        UserStatsEntity::class,
        CompetenceEntity::class,
        LevelEntity::class,
        QuestionEntity::class,
        QuestionOptionEntity::class,
        ProgressEntity::class,
        QuestionAttemptEntity::class,
        LevelProgressEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun progressDao(): ProgressDao
    abstract fun questionDao(): QuestionDao
    abstract fun competenceDao(): CompetenceDao
}