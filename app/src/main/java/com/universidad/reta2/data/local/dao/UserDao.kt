package com.universidad.reta2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.universidad.reta2.data.local.entities.UserEntity


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")


































    suspend fun getUserByEmail(email: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username AND email = :email LIMIT 1")
    suspend fun getUserByUsernameAndEmail(username: String, email: String): UserEntity?

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)
}

