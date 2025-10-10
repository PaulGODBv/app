package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "levels",
    foreignKeys = [
        ForeignKey(
            entity = CompetenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["competence_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LevelEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "competence_id")
    val competenceId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = true,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "progress")
    val progress: Float = 0f
)