package com.universidad.reta2.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private const val PREFS_NAME = "user_session"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_STUDENT_PROGRAM = "student_program"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_THEME_MODE = "theme_mode" // 0: Auto, 1: Light, 2: Dark
    private const val KEY_VIBRACION = "vibracion_activa"
    private const val KEY_EFECTOS = "efectos_activos"
    private const val KEY_MUSICA = "musica_activa"
    private const val KEY_VOL_MUSICA = "volumen_musica"
    private const val KEY_VOL_EFECTOS = "volumen_efectos"

    /** Valores de partida de los deslizadores, afinados a oido. */
    const val VOL_MUSICA_POR_DEFECTO = 0.25f
    const val VOL_EFECTOS_POR_DEFECTO = 0.7f
    private const val PREFIX_AVATAR = "avatar_"

    private val _themeModeFlow = MutableStateFlow(0)
    val themeModeFlow = _themeModeFlow.asStateFlow()

    private val _vibracionFlow = MutableStateFlow(true)
    val vibracionFlow = _vibracionFlow.asStateFlow()

    private val _efectosFlow = MutableStateFlow(true)
    val efectosFlow = _efectosFlow.asStateFlow()

    private val _musicaFlow = MutableStateFlow(false)
    val musicaFlow = _musicaFlow.asStateFlow()

    private val _volMusicaFlow = MutableStateFlow(VOL_MUSICA_POR_DEFECTO)
    val volMusicaFlow = _volMusicaFlow.asStateFlow()

    private val _volEfectosFlow = MutableStateFlow(VOL_EFECTOS_POR_DEFECTO)
    val volEfectosFlow = _volEfectosFlow.asStateFlow()

    fun init(context: Context) {
        _themeModeFlow.value = getThemeMode(context)
        _vibracionFlow.value = vibracionActiva(context)
        _efectosFlow.value = efectosActivos(context)
        _musicaFlow.value = musicaActiva(context)
        _volMusicaFlow.value = volumenMusica(context)
        _volEfectosFlow.value = volumenEfectos(context)
    }

    fun saveUserSession(context: Context, username: String, email: String, studentProgram: String = "") {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_STUDENT_PROGRAM, studentProgram)
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

    /**
     * Si el telefono vibra al acertar, fallar y superar un nivel.
     *
     * Activa por defecto: es la respuesta que pidio el profesor y la que hace
     * que el acierto se note sin mirar. Quien no la quiera la apaga en Perfil,
     * y la decision se recuerda.
     */
    fun vibracionActiva(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_VIBRACION, true)
    }

    fun setVibracion(context: Context, activa: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VIBRACION, activa).commit()
        _vibracionFlow.value = activa
    }

    /**
     * Si suenan los efectos de acertar, fallar y confirmar.
     *
     * Activos por defecto: son respuesta a una accion del estudiante, duran
     * medio segundo y es lo que se pidio en la reunion.
     */
    fun efectosActivos(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_EFECTOS, true)
    }

    fun setEfectos(context: Context, activos: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_EFECTOS, activos).commit()
        _efectosFlow.value = activos
    }

    /**
     * Si suena el bucle de fondo del menu.
     *
     * **Apagada por defecto, al contrario que los efectos.** La musica no
     * responde a nada que haya hecho el estudiante: empieza sola, y una
     * aplicacion de estudio que arranca sonando sin avisar es intrusiva. Quien
     * la quiera la enciende una vez y se recuerda.
     */
    fun musicaActiva(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MUSICA, false)
    }

    fun setMusica(context: Context, activa: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MUSICA, activa).commit()
        _musicaFlow.value = activa
    }

    /**
     * Volumen de la musica, de 0 a 1, por encima del volumen del sistema.
     *
     * Se guarda aparte del interruptor a proposito: apagar y volver a
     * encender tiene que devolver el volumen que habia, no uno por defecto.
     * Es la diferencia entre un interruptor y un deslizador a cero.
     */
    fun volumenMusica(context: Context): Float {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_VOL_MUSICA, VOL_MUSICA_POR_DEFECTO)
    }

    fun setVolumenMusica(context: Context, volumen: Float) {
        val v = volumen.coerceIn(0f, 1f)
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_VOL_MUSICA, v).commit()
        _volMusicaFlow.value = v
    }

    /** Volumen de los efectos, de 0 a 1. */
    fun volumenEfectos(context: Context): Float {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_VOL_EFECTOS, VOL_EFECTOS_POR_DEFECTO)
    }

    fun setVolumenEfectos(context: Context, volumen: Float) {
        val v = volumen.coerceIn(0f, 1f)
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_VOL_EFECTOS, v).commit()
        _volEfectosFlow.value = v
    }

    fun getCurrentUsername(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getCurrentEmail(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getCurrentStudentProgram(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_STUDENT_PROGRAM, "")
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
            remove(KEY_STUDENT_PROGRAM)
            putBoolean(KEY_IS_LOGGED_IN, false)
            commit() // Cambiado apply() por commit() para persistencia inmediata
        }
    }

    fun updateUserData(context: Context, username: String, email: String, studentProgram: String = "") {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_STUDENT_PROGRAM, studentProgram)
            commit() // Cambiado apply() por commit() para persistencia inmediata
        }
    }
}