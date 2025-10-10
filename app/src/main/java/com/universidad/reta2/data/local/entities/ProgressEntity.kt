package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "user_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["username"],
            childColumns = ["username"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "question_id")
    val questionId: String,

    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean,

    @ColumnInfo(name = "time_spent_seconds")
    val timeSpentSeconds: Int,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long = System.currentTimeMillis()
)