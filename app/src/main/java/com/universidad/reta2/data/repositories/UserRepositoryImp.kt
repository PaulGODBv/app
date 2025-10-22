package com.universidad.reta2.data.repositories

import javax.inject.Inject
import com.universidad.reta2.data.local.dao.UserDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.mappers.UserMapper
import com.universidad.reta2.domain.models.User
import com.universidad.reta2.domain.repositories.UserRepository

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userStatsDao: UserStatsDao,
    private val mapper: UserMapper
) : UserRepository {

    override suspend fun getUserByUsernameOrEmail(identifier: String): User? {
        // Buscar por username primero
        val userByUsername = userDao.getUser(identifier)
        if (userByUsername != null) {
            return mapper.toDomain(userByUsername)
        }

        // Si no encuentra por username, buscar por email
        val userByEmail = userDao.getUserByEmail(identifier)
        return userByEmail?.let { mapper.toDomain(it) }
    }

    override suspend fun createUser(user: User): Boolean {
        return try {
            userDao.insertUser(mapper.toEntity(user))

            // Crear estadísticas iniciales para el nuevo usuario
            val initialStats = com.universidad.reta2.data.local.mappers.UserStatsMapper
                .createInitialStats(user.username)
            userStatsDao.createInitialStats(initialStats)

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUser(username)?.let { mapper.toDomain(it) }
    }

    override suspend fun getUserByEmail(email: String): User? {
        val userEntity = userDao.getUserByEmail(email)
        return userEntity?.let {
            // Obtener estadísticas del usuario
            val stats = userStatsDao.getUserStatsSync(it.username)
            mapper.toDomain(it, stats)
        }
    }

    override suspend fun userExists(username: String, email: String): Boolean {
        return userDao.getUser(username) != null || userDao.getUserByEmail(email) != null
    }

    override suspend fun updateUser(
        currentUsername: String,
        currentEmail: String,
        newUsername: String,
        newEmail: String,
        newPassword: String?
    ): Boolean {
        return try {
            // Buscar el usuario actual
            val user = userDao.getUserByUsernameAndEmail(currentUsername, currentEmail)
                ?: return false

            val updatedUser = user.copy(
                username = newUsername,
                email = newEmail,
                password = newPassword ?: user.password
            )

            userDao.updateUser(updatedUser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}