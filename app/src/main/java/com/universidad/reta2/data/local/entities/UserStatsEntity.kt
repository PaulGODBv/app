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
    val dailyPracticeTime: Int = 0
)