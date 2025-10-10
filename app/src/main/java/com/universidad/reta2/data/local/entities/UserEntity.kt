package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "password")
    val password: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

