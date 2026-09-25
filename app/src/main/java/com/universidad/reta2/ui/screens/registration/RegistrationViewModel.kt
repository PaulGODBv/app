package com.universidad.reta2.ui.screens.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.User
import com.universidad.reta2.domain.repositories.UserRepository
import com.universidad.reta2.utils.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            errorMessage = ""
        )
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = ""
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = ""
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            errorMessage = ""
        )
    }

    fun onStudentCodeChange(studentCode: String) {
        // Solo permitir dígitos y máximo 11 caracteres
        if (studentCode.length <= 11 && studentCode.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                studentCode = studentCode,
                errorMessage = ""
            )
        }
    }

    fun onStudentProgramChange(studentProgram: String) {
        _uiState.value = _uiState.value.copy(
            studentProgram = studentProgram,
            errorMessage = ""
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }

    suspend fun register(): RegistrationResult {
        val state = _uiState.value

        return when {
            state.username.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "El nombre de usuario no puede estar vacío"
                )
                RegistrationResult.EmptyUsername
            }
            state.email.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "El correo electrónico no puede estar vacío"
                )
                RegistrationResult.EmptyEmail
            }
            !isValidEmail(state.email) -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Por favor ingrese un correo electrónico válido"
                )
                RegistrationResult.InvalidEmail
            }
            !isInstitutionalEmail(state.email) -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Regístrate con tu correo institucional " +
                            "($INSTITUTIONAL_EMAIL_DOMAIN)"
                )
                RegistrationResult.NonInstitutionalEmail
            }
            state.password.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "La contraseña no puede estar vacía"
                )
                RegistrationResult.EmptyPassword
            }
            state.password.length < 6 -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                )
                RegistrationResult.WeakPassword
            }
            state.password != state.confirmPassword -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Las contraseñas no coinciden"
                )
                RegistrationResult.PasswordsNotMatch
            }
            state.studentCode.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "El código estudiantil no puede estar vacío"
                )
                RegistrationResult.EmptyStudentCode
            }
            state.studentCode.length != 11 -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "El código estudiantil debe tener 11 dígitos"
                )
                RegistrationResult.InvalidStudentCode
            }
            state.studentProgram.isEmpty() -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "El programa académico no puede estar vacío"
                )
                RegistrationResult.EmptyStudentProgram
            }
            else -> {
                try {
                    // Verificar si el usuario ya existe
                    val userExists = userRepository.userExists(state.username, state.email)

                    if (userExists) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "El usuario o correo electrónico ya está registrado"
                        )
                        RegistrationResult.UserExists
                    } else {
                        val user = User(
                            username = state.username,
                            email = state.email,
                            // Se hashea aquí: a la capa de datos nunca llega texto plano.
                            passwordHash = passwordHasher.hashPassword(state.password),
                            studentCode = state.studentCode,
                            studentProgram = state.studentProgram
                        )

                        val success = userRepository.createUser(user)

                        if (success) {
                            _uiState.value = _uiState.value.copy(
                                successMessage = "Usuario registrado exitosamente"
                            )
                            RegistrationResult.Success
                        } else {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Error al registrar usuario"
                            )
                            RegistrationResult.RegistrationError
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al registrar usuario: ${e.message}"
                    )
                    RegistrationResult.Error(e.message ?: "Error desconocido")
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Comprueba que el correo pertenezca al dominio institucional.
     *
     * Se valida aquí, antes de crear la cuenta, para dar respuesta inmediata al usuario.
     * El servidor repite la comprobación en su serializer: esta validación es de
     * experiencia de uso, la del backend es la de seguridad.
     */
    private fun isInstitutionalEmail(email: String): Boolean {
        return email.trim().lowercase().endsWith(INSTITUTIONAL_EMAIL_DOMAIN)
    }

    companion object {
        const val INSTITUTIONAL_EMAIL_DOMAIN = "@mail.udes.edu.co"
    }
}

data class RegistrationUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val studentCode: String = "",
    val studentProgram: String = "", // Nuevo
    val errorMessage: String = "",
    val successMessage: String = "",
    val isLoading: Boolean = false
)

sealed class RegistrationResult {
    object Success : RegistrationResult()
    object EmptyUsername : RegistrationResult()
    object EmptyEmail : RegistrationResult()
    object InvalidEmail : RegistrationResult()
    object NonInstitutionalEmail : RegistrationResult()
    object EmptyPassword : RegistrationResult()
    object WeakPassword : RegistrationResult()
    object PasswordsNotMatch : RegistrationResult()
    object UserExists : RegistrationResult()
    object RegistrationError : RegistrationResult()
    object EmptyStudentCode : RegistrationResult()
    object InvalidStudentCode : RegistrationResult()
    object EmptyStudentProgram : RegistrationResult() // Nuevo
    data class Error(val message: String) : RegistrationResult()
}
