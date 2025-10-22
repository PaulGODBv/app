package com.universidad.reta2.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val username = sessionManager.getCurrentUsername(context) ?: ""
        val email = sessionManager.getCurrentEmail(context) ?: ""
        _uiState.value = _uiState.value.copy(username = username, email = email)
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

            _uiState.value = state.copy(isLoading = true, errorMessage = "", successMessage = "")

            val currentUsername = sessionManager.getCurrentUsername(context) ?: ""
            val currentEmail = sessionManager.getCurrentEmail(context) ?: ""

            val success = userRepository.updateUser(
                currentUsername,
                currentEmail,
                state.username,
                state.email,
                if (state.newPassword.isNotEmpty()) state.newPassword else null
            )

            if (success) {
                sessionManager.updateUserData(context, state.username, state.email)
                showSuccess("Perfil actualizado exitosamente")
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
    val successMessage: String = ""
)

