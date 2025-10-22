package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.User

interface UserRepository {
    suspend fun getUserByUsernameOrEmail(identifier: String): User?
    suspend fun createUser(user: User): Boolean
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun userExists(username: String, email: String): Boolean
    suspend fun updateUser(
        currentUsername: String,
        currentEmail: String,
        newUsername: String,
        newEmail: String,
        newPassword: String?
    ): Boolean
}


