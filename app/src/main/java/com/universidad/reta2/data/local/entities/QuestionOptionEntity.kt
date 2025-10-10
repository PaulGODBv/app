package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "question_options",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["question_id"])
    ]
)

data class QuestionOptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,

    @ColumnInfo(name="question_id")
    val questionId: Int,

    @ColumnInfo(name="option_text")
    val optionText: String,

    @ColumnInfo(name="original_order")
    val originalOrder: Int
)