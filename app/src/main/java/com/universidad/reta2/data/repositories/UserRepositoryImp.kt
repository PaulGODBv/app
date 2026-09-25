package com.universidad.reta2.data.repositories

import javax.inject.Inject
import com.universidad.reta2.data.local.dao.UserDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.mappers.UserMapper
import com.universidad.reta2.domain.models.User
import com.universidad.reta2.domain.repositories.UserRepository
import com.universidad.reta2.utils.PasswordHasher

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userStatsDao: UserStatsDao,
    private val mapper: UserMapper,
    private val passwordHasher: PasswordHasher
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
            // El hash debería llegar ya generado desde la capa de presentación.
            // Esta comprobación es la última barrera: si por cualquier motivo llegara
            // texto plano, se hashea aquí antes de tocar la base de datos.
            val safeUser = user.copy(passwordHash = ensureHashed(user.passwordHash))

            userDao.insertUser(mapper.toEntity(safeUser))

            // Crear estadísticas iniciales para el nuevo usuario
            val initialStats = com.universidad.reta2.data.local.mappers.UserStatsMapper
                .createInitialStats(safeUser.username)
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
        newPasswordHash: String?
    ): Boolean {
        return try {
            // Buscar el usuario actual
            val user = userDao.getUserByUsernameAndEmail(currentUsername, currentEmail)
                ?: return false

            val updatedUser = user.copy(
                username = newUsername,
                email = newEmail,
                // Si no se envía contraseña nueva se conserva el hash actual.
                passwordHash = newPasswordHash?.let { ensureHashed(it) } ?: user.passwordHash
            )

            userDao.updateUser(updatedUser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Devuelve el valor tal cual si ya es un hash; si no, lo hashea. */
    private fun ensureHashed(value: String): String =
        if (passwordHasher.isHashed(value)) value else passwordHasher.hashPassword(value)
}