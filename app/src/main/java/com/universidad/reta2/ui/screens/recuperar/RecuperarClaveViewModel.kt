package com.universidad.reta2.ui.screens.recuperar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.repositories.UserRepository
import com.universidad.reta2.utils.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** En qué punto del recorrido está la pantalla. */
enum class PasoDeRecuperacion {
    /** Qué cuenta se va a recuperar. */
    IDENTIFICAR,

    /** Verificada la identidad; ya se puede escribir la nueva. */
    NUEVA_CLAVE,

    /** Guardada. */
    LISTO,
}

data class RecuperarClaveUiState(
    val paso: PasoDeRecuperacion = PasoDeRecuperacion.IDENTIFICAR,
    val identificador: String = "",
    val nuevaClave: String = "",
    val confirmacion: String = "",
    val verClaves: Boolean = false,
    val cargando: Boolean = false,
    val error: String = "",
    /** El usuario de la cuenta encontrada, para enseñar a quién se recupera. */
    val usuarioEncontrado: String = "",
    val correoEncontrado: String = "",
)

@HiltViewModel
class RecuperarClaveViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onIdentificadorChange(valor: String) {
        _uiState.value = _uiState.value.copy(identificador = valor, error = "")
    }

    fun onNuevaClaveChange(valor: String) {
        _uiState.value = _uiState.value.copy(nuevaClave = valor, error = "")
    }

    fun onConfirmacionChange(valor: String) {
        _uiState.value = _uiState.value.copy(confirmacion = valor, error = "")
    }

    fun alternarVerClaves() {
        _uiState.value = _uiState.value.copy(verClaves = !_uiState.value.verClaves)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = "")
    }

    /**
     * Busca la cuenta y avisa de si existe.
     *
     * **Pedir el usuario o el correo no es una comprobación de seguridad**:
     * los dos se conocen y se adivinan. Es un selector, porque en un mismo
     * teléfono puede haber varias cuentas y hay que saber cuál se reinicia.
     * Lo que verifica de verdad es el paso siguiente, el del sistema.
     *
     * Y por eso **sí se dice si la cuenta existe**. En un servicio en la nube
     * se calla, para no revelar quién está registrado; aquí no hay nada que
     * revelar —la base es de este teléfono y quien la consulta ya lo tiene en
     * la mano— y callarlo solo dejaría al estudiante mirando una pantalla que
     * no avanza sin decirle por qué.
     */
    fun buscarCuenta(alEncontrar: () -> Unit) {
        val identificador = _uiState.value.identificador.trim()
        if (identificador.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Escribe tu usuario o tu correo.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = "")
            val usuario = userRepository.getUserByUsernameOrEmail(identificador)
            if (usuario == null) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No hay ninguna cuenta con ese usuario ni ese correo en este teléfono.",
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                cargando = false,
                usuarioEncontrado = usuario.username,
                correoEncontrado = usuario.email,
            )
            alEncontrar()
        }
    }

    /** El sistema dijo que sí. Se abre el paso de escribir la nueva. */
    fun identidadVerificada() {
        _uiState.value = _uiState.value.copy(paso = PasoDeRecuperacion.NUEVA_CLAVE, error = "")
    }

    fun verificacionFallida(mensaje: String) {
        _uiState.value = _uiState.value.copy(error = mensaje)
    }

    fun guardarNuevaClave() {
        val estado = _uiState.value
        val problema = problemaCon(estado.nuevaClave, estado.confirmacion)
        if (problema != null) {
            _uiState.value = estado.copy(error = problema)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = "")
            val guardado = userRepository.updateUser(
                currentUsername = estado.usuarioEncontrado,
                currentEmail = estado.correoEncontrado,
                newUsername = estado.usuarioEncontrado,
                newEmail = estado.correoEncontrado,
                newPasswordHash = passwordHasher.hashPassword(estado.nuevaClave),
            )
            _uiState.value = if (guardado) {
                _uiState.value.copy(cargando = false, paso = PasoDeRecuperacion.LISTO)
            } else {
                _uiState.value.copy(
                    cargando = false,
                    error = "No se pudo guardar la contraseña nueva. Inténtalo otra vez.",
                )
            }
        }
    }

    /**
     * Las mismas reglas que el registro, y por eso están aquí y no repartidas
     * por la pantalla: si un día cambian, cambian en un sitio.
     */
    private fun problemaCon(nueva: String, confirmacion: String): String? = when {
        nueva.length < MINIMO_DE_CLAVE ->
            "La contraseña necesita al menos $MINIMO_DE_CLAVE caracteres."
        nueva != confirmacion -> "Las dos contraseñas no coinciden."
        else -> null
    }

    private companion object {
        const val MINIMO_DE_CLAVE = 6
    }
}
