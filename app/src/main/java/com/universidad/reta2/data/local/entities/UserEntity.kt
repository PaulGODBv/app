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
    
    /** Hash de la contraseña generado por PasswordHasher: nunca texto plano. */
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "student_code")
    val studentCode: String = "",

    @ColumnInfo(name = "student_program")
    val studentProgram: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

