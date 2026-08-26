package com.universidad.reta2.data.repositories

import android.content.Context
import com.universidad.reta2.domain.models.User
import com.universidad.reta2.domain.repositories.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionRepository {

    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    override suspend fun getCurrentUser(): User? {
        val username = prefs.getString("username", null)
        val email = prefs.getString("email", null)
        return if (username != null && email != null) {
            User(username = username, email = email, password = "") // password vacío
        } else null
    }

    override suspend fun clearSession() {
        prefs.edit().clear().commit() // Cambiado apply() por commit()
    }

    override suspend fun saveSession(user: User) {
        prefs.edit()
            .putString("username", user.username)
            .putString("email", user.email)
            .commit() // Cambiado apply() por commit()
    }
}
