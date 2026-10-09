package com.universidad.reta2.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.repositories.UserRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.utils.Musica
import com.universidad.reta2.utils.Sonidos
import com.universidad.reta2.utils.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val userStatsRepository: UserStatsRepository,
    private val passwordHasher: PasswordHasher,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    val themeMode = SessionManager.themeModeFlow

    val vibracion = SessionManager.vibracionFlow
    val efectos = SessionManager.efectosFlow
    val musica = SessionManager.musicaFlow

    fun setVibracion(activa: Boolean) = SessionManager.setVibracion(context, activa)

    fun setEfectos(activos: Boolean) = SessionManager.setEfectos(context, activos)

    val volumenMusica = SessionManager.volMusicaFlow
    val volumenEfectos = SessionManager.volEfectosFlow

    fun setVolumenMusica(v: Float) {
        SessionManager.setVolumenMusica(context, v)
        // Y en caliente, sobre lo que ya está sonando: ajustar a ciegas un
        // volumen es justo lo que un deslizador evita.
        Musica.ajustarVolumen(v)
    }

    fun setVolumenEfectos(v: Float) = SessionManager.setVolumenEfectos(context, v)

    /**
     * Suena una vez al soltar el deslizador de efectos, para oír dónde quedó.
     *
     * Al soltar y no mientras se arrastra: un efecto por cada paso del
     * deslizador sería una ametralladora.
     */
    fun probarEfecto() = Sonidos.toque(context)

    fun setMusica(activa: Boolean) {
        SessionManager.setMusica(context, activa)
        // Apagarla no basta con dejar de arrancarla: hay un MediaPlayer vivo
        // sonando ahora mismo, y hay que soltarlo. Encenderla no necesita nada
        // aquí: MainActivity observa el ajuste y la arranca si la pantalla de
        // ese momento es de menú.
        if (!activa) Musica.soltar()
    }

    private val _eventChannel= MutableSharedFlow<ProfileEvent>()
    val eventChannel = _eventChannel.asSharedFlow()

    sealed class ProfileEvent {
        object ThemeChanged : ProfileEvent()
    }

    init {
        val username = sessionManager.getCurrentUsername(context) ?: ""
        val email = sessionManager.getCurrentEmail(context) ?: ""
        _uiState.value = _uiState.value.copy(
            username = username, 
            email = email
        )
        viewModelScope.launch {
            try {
                userStatsRepository.getUserStats().collect { stats ->
                    _uiState.value = _uiState.value.copy(
                        totalQuestionsAnswered = stats.totalQuestionsAnswered,
                        currentStreak = stats.currentStreakDays,
                        maxStreak = stats.maxStreakDays
                    )
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            sessionManager.setThemeMode(context, mode)
            _eventChannel.emit(ProfileEvent.ThemeChanged)
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onCurrentPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(currentPassword = value)
    }

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    fun updateProfile() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.username.isEmpty()) {
                showError("El nombre de usuario no puede estar vacío")
                return@launch
            }
            if (state.email.isEmpty()) {
                showError("El correo electrónico no puede estar vacío")
                return@launch
            }

            val currentUsername = sessionManager.getCurrentUsername(context) ?: ""
            val currentEmail = sessionManager.getCurrentEmail(context) ?: ""

            // Solo se intenta cambiar la contraseña si la persona escribió algo
            // en alguno de los tres campos del bloque de contraseña.
            val wantsPasswordChange = state.currentPassword.isNotEmpty() ||
                    state.newPassword.isNotEmpty() ||
                    state.confirmPassword.isNotEmpty()

            var newPasswordHash: String? = null

            if (wantsPasswordChange) {
                when {
                    state.currentPassword.isEmpty() -> {
                        showError("Ingresa tu contraseña actual")
                        return@launch
                    }
                    state.newPassword.isEmpty() -> {
                        showError("La nueva contraseña no puede estar vacía")
                        return@launch
                    }
                    state.newPassword.length < 6 -> {
                        showError("La nueva contraseña debe tener al menos 6 caracteres")
                        return@launch
                    }
                    state.newPassword != state.confirmPassword -> {
                        showError("Las contraseñas no coinciden")
                        return@launch
                    }
                }

                val storedUser = userRepository.getUserByUsername(currentUsername)
                if (storedUser == null) {
                    showError("No se encontró el usuario de la sesión")
                    return@launch
                }

                // Paso clave: la contraseña actual se comprueba contra el hash guardado.
                if (!passwordHasher.verifyPassword(state.currentPassword, storedUser.passwordHash)) {
                    showError("Contraseña actual incorrecta")
                    return@launch
                }

                newPasswordHash = passwordHasher.hashPassword(state.newPassword)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = "",
                successMessage = ""
            )

            val success = userRepository.updateUser(
                currentUsername,
                currentEmail,
                state.username,
                state.email,
                newPasswordHash
            )

            if (success) {
                sessionManager.updateUserData(context, state.username, state.email)
                showSuccess(
                    if (newPasswordHash != null) "Perfil y contraseña actualizados exitosamente"
                    else "Perfil actualizado exitosamente"
                )
                _uiState.value = _uiState.value.copy(
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = ""
                )
            } else {
                showError("Error al actualizar perfil. Verifique que el usuario/email no esté en uso")
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun logout() {
        sessionManager.logout(context)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = "", successMessage = "")
    }

    private fun showError(msg: String) {
        _uiState.value = _uiState.value.copy(errorMessage = msg)
    }

    private fun showSuccess(msg: String) {
        _uiState.value = _uiState.value.copy(successMessage = msg)
    }
}

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val totalQuestionsAnswered: Int = 0,
    val currentStreak: Int = 0,
    /** Mejor racha alcanzada. Las insignias se miden contra esta, no contra la viva. */
    val maxStreak: Int = 0
)

