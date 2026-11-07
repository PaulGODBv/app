package com.universidad.reta2.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * «Demuéstrame que este teléfono es tuyo», con la huella o el PIN del sistema.
 *
 * POR QUÉ ESTO Y NO UN CORREO. La cuenta del estudiante vive **solo** en la
 * base Room de este aparato: ni el panel ni ningún servidor guardan su
 * contraseña. Un correo demostraría que controlas un buzón, que no tiene nada
 * que ver con los datos; y a quien pierda el teléfono no le serviría de nada,
 * porque la cuenta no existe en ningún otro sitio. Tener el aparato
 * desbloqueado **es** la prueba de ser su dueño, y aquí es el factor correcto,
 * no un sucedáneo.
 *
 * LO QUE ESTO NO PROTEGE, dicho claro: quien pueda desbloquear el teléfono
 * podrá reiniciar la contraseña de cualquier cuenta que haya en él. Es
 * aceptable porque quien tiene el móvil abierto ya tiene la base entera; la
 * contraseña de la app nunca defendió de eso, defiende del cambio casual
 * entre cuentas.
 *
 * SE ACEPTAN LOS DOS FACTORES —biometría y credencial del sistema— a
 * propósito. Exigir huella dejaría fuera a quien tenga el lector estropeado o
 * no lo use, y el PIN del teléfono prueba lo mismo.
 */
object VerificacionDelDispositivo {

    private const val FACTORES = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    /** En qué estado está el bloqueo de este teléfono. */
    enum class Disponibilidad {
        /** Se puede pedir. */
        LISTA,

        /** El teléfono no tiene ni huella ni PIN configurados. */
        SIN_BLOQUEO,

        /** El aparato no trae con qué, o está fuera de servicio. */
        NO_SOPORTADA,
    }

    fun disponibilidad(context: Context): Disponibilidad =
        when (BiometricManager.from(context).canAuthenticate(FACTORES)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Disponibilidad.LISTA

            // Sin bloqueo configurado no hay NADA contra qué verificar. Se
            // distingue de los demás casos porque es el único que el
            // estudiante puede resolver: basta con que se ponga un PIN.
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Disponibilidad.SIN_BLOQUEO

            else -> Disponibilidad.NO_SOPORTADA
        }

    /**
     * Pide la verificación y avisa del resultado.
     *
     * [alCancelar] y [alFallar] son cosas distintas y por eso van separadas:
     * cancelar es una decisión del estudiante —se arrepintió— y no merece un
     * mensaje de error; fallar es que el sistema dijo que no.
     */
    fun pedir(
        actividad: FragmentActivity,
        titulo: String,
        descripcion: String,
        alVerificar: () -> Unit,
        alCancelar: () -> Unit = {},
        alFallar: (String) -> Unit = {},
    ) {
        val prompt = BiometricPrompt(
            actividad,
            ContextCompat.getMainExecutor(actividad),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) = alVerificar()

                override fun onAuthenticationError(code: Int, mensaje: CharSequence) {
                    val cancelado = code == BiometricPrompt.ERROR_USER_CANCELED ||
                        code == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        code == BiometricPrompt.ERROR_CANCELED
                    if (cancelado) alCancelar() else alFallar(mensaje.toString())
                }

                // Una huella que no casa. No es un error: el sistema deja
                // reintentar solo, y cerrar aqui la pantalla seria quitarle
                // al estudiante el segundo intento.
                override fun onAuthenticationFailed() = Unit
            }
        )

        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(titulo)
                .setSubtitle(descripcion)
                // Sin boton de negativo: lo prohibe la API cuando se admite
                // la credencial del sistema, porque esa pantalla ya trae el
                // suyo para cancelar.
                .setAllowedAuthenticators(FACTORES)
                .build()
        )
    }
}
