package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "competences")
data class CompetenceEntity(
    @PrimaryKey
    val id: Int,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "icon")
    val iconResId: Int,
    
    @ColumnInfo(name = "total_progress")
    val totalProgress: Float = 0f
)