package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.User


interface SessionRepository {
    suspend fun getCurrentUser(): User?
    suspend fun clearSession()
    suspend fun saveSession(user: User)
}
