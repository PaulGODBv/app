package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val username: String,

    @ColumnInfo(name = "total_questions_answered")
    val totalQuestionsAnswered: Int = 0,

    @ColumnInfo(name = "total_practice_time_seconds")
    val totalPracticeTimeSeconds: Int = 0,

    @ColumnInfo(name = "current_streak_days")
    val currentStreakDays: Int = 0,

    @ColumnInfo(name = "last_practice_date")
    val lastPracticeDate: String = "",

    @ColumnInfo(name = "daily_practice_time")
    val dailyPracticeTime: Int = 0,

    // Marcas históricas. Los logros se miden contra estas y no contra los
    // contadores vivos, porque la racha y el tiempo del día se reinician por
    // diseño: sin esto, perder la racha bloqueaba de nuevo una insignia ya
    // conseguida. Solo suben, nunca bajan.
    @ColumnInfo(name = "max_streak_days")
    val maxStreakDays: Int = 0,

    @ColumnInfo(name = "max_daily_practice_time")
    val maxDailyPracticeTime: Int = 0
)