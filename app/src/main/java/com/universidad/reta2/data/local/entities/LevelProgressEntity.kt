package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "competence_id")
    val competenceId: Int,

    @ColumnInfo(name = "level_id")
    val levelId: Int,

    @ColumnInfo(name = "questions_completed")
    val questionsCompleted: Int,

    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "time_spent")
    val timeSpent: Int,

    @ColumnInfo(name = "current_question_index")
    val currentQuestionIndex: Int,

    @ColumnInfo(name = "answered_questions")
    val answeredQuestions: String, // JSON: [1, 3, 5, 7]

    @ColumnInfo(name = "correct_answers")
    val correctAnswers: String, // JSON: [1, 3, 7]

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)

