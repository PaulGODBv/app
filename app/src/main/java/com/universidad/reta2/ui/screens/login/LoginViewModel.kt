package com.universidad.reta2.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            errorMessage = ""
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = ""
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }

    suspend fun login(context: Context): LoginResult {
        val state = _uiState.value

        return when {
            state.username.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "El usuario o correo no puede estar vacío"
                )
                LoginResult.EmptyUsername
            }
            state.password.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "La contraseña no puede estar vacía"
                )
                LoginResult.EmptyPassword
            }
            else -> {
                try {
                    val user = userRepository.getUserByUsernameOrEmail(state.username)

                    if (user != null && user.password == state.password) {
                        sessionManager.saveUserSession(
                            context=context,
                            username = user.username,
                            email = user.email
                        )
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Login exitoso"
                        )
                        LoginResult.Success
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Usuario/Correo o contraseña incorrectos"
                        )
                        LoginResult.InvalidCredentials
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al verificar credenciales: ${e.message}"
                    )
                    LoginResult.Error(e.message ?: "Error desconocido")
                }
            }
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val errorMessage: String = "",
    val successMessage: String = "",
    val isLoading: Boolean = false
)

sealed class LoginResult {
    object Success : LoginResult()
    object EmptyUsername : LoginResult()
    object EmptyPassword : LoginResult()
    object InvalidCredentials : LoginResult()
    data class Error(val message: String) : LoginResult()
}