package com.universidad.reta2.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private const val PREFS_NAME = "user_session"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_THEME_MODE = "theme_mode" // 0: Auto, 1: Light, 2: Dark
    private const val PREFIX_AVATAR = "avatar_"

    private val _themeModeFlow = MutableStateFlow(0)
    val themeModeFlow = _themeModeFlow.asStateFlow()

    fun init(context: Context) {
        _themeModeFlow.value = getThemeMode(context)
    }

    fun saveUserSession(context: Context, username: String, email: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            commit() // Cambiado apply() por commit() para persistencia inmediata
        }
    }

    fun saveUserAvatar(context: Context, username: String, uri: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREFIX_AVATAR + username, uri).commit()
    }

    fun getUserAvatar(context: Context, username: String): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREFIX_AVATAR + username, null)
    }

    fun setThemeMode(context: Context, mode: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, mode).commit()
        _themeModeFlow.value = mode
    }

    fun getThemeMode(context: Context): Int {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, 0) // Default: Auto
    }

    fun getCurrentUsername(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getCurrentEmail(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EMAIL, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove(KEY_USERNAME)
            remove(KEY_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
            commit() // Cambiado apply() por commit() para persistencia inmediata
        }
    }

    fun updateUserData(context: Context, username: String, email: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            commit() // Cambiado apply() por commit() para persistencia inmediata
        }
    }
}