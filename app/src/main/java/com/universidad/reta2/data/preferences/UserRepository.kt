package com.universidad.reta2.data.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class User(
    val username: String,
    val email: String,
    val password: String
)

class UserRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
    private val USERS_KEY = "registered_users"

    fun saveUser(user: User): Boolean {
        try {
            val usersJson = prefs.getString(USERS_KEY, "[]") ?: "[]"
            val usersArray = JSONArray(usersJson)

            // Verificar si el usuario ya existe
            for (i in 0 until usersArray.length()) {
                val userObj = usersArray.getJSONObject(i)
                if (userObj.getString("username") == user.username ||
                    userObj.getString("email") == user.email) {
                    return false // Usuario ya existe
                }
            }

            // Agregar nuevo usuario
            val newUserObj = JSONObject().apply {
                put("username", user.username)
                put("email", user.email)
                put("password", user.password)
            }
            usersArray.put(newUserObj)

            prefs.edit().putString(USERS_KEY, usersArray.toString()).apply()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getUserByUsernameOrEmail(username: String, email: String): User? {
        try {
            val usersJson = prefs.getString(USERS_KEY, "[]") ?: "[]"
            val usersArray = JSONArray(usersJson)

            for (i in 0 until usersArray.length()) {
                val userObj = usersArray.getJSONObject(i)
                if (userObj.getString("username") == username ||
                    userObj.getString("email") == email) {
                    return User(
                        username = userObj.getString("username"),
                        email = userObj.getString("email"),
                        password = userObj.getString("password")
                    )
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    fun getAllUsers(): List<User> {
        try {
            val usersJson = prefs.getString(USERS_KEY, "[]") ?: "[]"
            val usersArray = JSONArray(usersJson)
            val users = mutableListOf<User>()

            for (i in 0 until usersArray.length()) {
                val userObj = usersArray.getJSONObject(i)
                users.add(
                    User(
                        username = userObj.getString("username"),
                        email = userObj.getString("email"),
                        password = userObj.getString("password")
                    )
                )
            }
            return users
        } catch (e: Exception) {
            return emptyList()
        }
    }

    fun updateUser(oldUsername: String, oldEmail: String, newUsername: String, newEmail: String, newPassword: String? = null): Boolean {
        try {
            val usersJson = prefs.getString(USERS_KEY, "[]") ?: "[]"
            val usersArray = JSONArray(usersJson)

            // Buscar el usuario a actualizar
            for (i in 0 until usersArray.length()) {
                val userObj = usersArray.getJSONObject(i)
                if (userObj.getString("username") == oldUsername ||
                    userObj.getString("email") == oldEmail) {

                    // Verificar que el nuevo username/email no esté en uso por otro usuario
                    for (j in 0 until usersArray.length()) {
                        if (i != j) {
                            val otherUserObj = usersArray.getJSONObject(j)
                            if (otherUserObj.getString("username") == newUsername ||
                                otherUserObj.getString("email") == newEmail) {
                                return false // Username o email ya está en uso
                            }
                        }
                    }

                    // Actualizar los datos del usuario
                    userObj.put("username", newUsername)
                    userObj.put("email", newEmail)
                    if (newPassword != null) {
                        userObj.put("password", newPassword)
                    }

                    prefs.edit().putString(USERS_KEY, usersArray.toString()).apply()
                    return true
                }
            }
            return false // Usuario no encontrado
        } catch (e: Exception) {
            return false
        }
    }

    fun verifyUserCredentials(username: String, email: String, password: String): User? {
        try {
            val usersJson = prefs.getString(USERS_KEY, "[]") ?: "[]"
            val usersArray = JSONArray(usersJson)

            for (i in 0 until usersArray.length()) {
                val userObj = usersArray.getJSONObject(i)
                if ((userObj.getString("username") == username ||
                            userObj.getString("email") == email) &&
                    userObj.getString("password") == password) {
                    return User(
                        username = userObj.getString("username"),
                        email = userObj.getString("email"),
                        password = userObj.getString("password")
                    )
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    fun verifyCurrentPassword(username: String, email: String, currentPassword: String): Boolean {
        try {
            val usersJson = prefs.getString(USERS_KEY, "[]") ?: "[]"
            val usersArray = JSONArray(usersJson)

            for (i in 0 until usersArray.length()) {
                val userObj = usersArray.getJSONObject(i)
                if ((userObj.getString("username") == username ||
                            userObj.getString("email") == email) &&
                    userObj.getString("password") == currentPassword) {
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
}