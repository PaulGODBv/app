package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "question_attempts",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "question_id")
    val questionId: Int,

    @ColumnInfo(name = "level_id")
    val levelId: Int,

    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean,

    @ColumnInfo(name = "time_spent_seconds")
    val timeSpentSeconds: Int,

    @ColumnInfo(name = "attempted_at")
    val attemptedAt: Long = System.currentTimeMillis()
)

