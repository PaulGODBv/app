package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = LevelEntity::class,
            parentColumns = ["id"],
            childColumns = ["level_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuestionEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "level_id")
    val levelId: Int,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "correct_option_id")
    val correctOptionId: Int, // ID de la opción correcta en question_options

    @ColumnInfo(name = "explanation")
    val explanation: String = ""
)