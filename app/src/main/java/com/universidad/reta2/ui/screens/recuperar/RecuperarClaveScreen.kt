package com.universidad.reta2.ui.screens.recuperar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.utils.VerificacionDelDispositivo

/**
 * Recuperar la contraseña demostrando que el teléfono es tuyo.
 *
 * Tres pasos y ninguno de adorno: qué cuenta, quién eres, y la nueva. El del
 * medio lo resuelve el sistema —huella o PIN— y la razón está en
 * [VerificacionDelDispositivo]: la cuenta vive solo en este aparato, así que
 * tenerlo desbloqueado es la prueba de ser su dueño.
 */
@Composable
fun RecuperarClaveScreen(
    navController: NavController,
    viewModel: RecuperarClaveViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // `BiometricPrompt` necesita la actividad, no un `Context` cualquiera.
    // Si no la hay —una vista previa, por ejemplo— no se ofrece el paso en
    // vez de reventar al pulsar.
    val actividad = context as? FragmentActivity

    /* El estado del bloqueo se consulta al entrar y **cada vez que se vuelve a
     * la app**, no en cada recomposición —habría una por cada letra tecleada—
     * ni una sola vez para siempre.
     *
     * Lo de volver no es un refinamiento: el aviso de más abajo dice «ponle un
     * bloqueo en los ajustes del teléfono y vuelve aquí», y eso es exactamente
     * lo que el estudiante hace. Con un `remember` a secas, volvía y seguía
     * leyendo que no tenía PIN, con el botón gris y sin forma de avanzar.
     * Se vio en el emulador poniendo el PIN con la pantalla abierta. */
    val lifecycleOwner = LocalLifecycleOwner.current
    var disponibilidad by remember {
        mutableStateOf(VerificacionDelDispositivo.disponibilidad(context))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                disponibilidad = VerificacionDelDispositivo.disponibilidad(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Recuperar contraseña",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
        )

        when (estado.paso) {
            PasoDeRecuperacion.IDENTIFICAR -> PasoIdentificar(
                estado = estado,
                disponibilidad = disponibilidad,
                hayActividad = actividad != null,
                onIdentificadorChange = viewModel::onIdentificadorChange,
                onContinuar = {
                    viewModel.buscarCuenta {
                        val quien = viewModel.uiState.value.usuarioEncontrado
                        VerificacionDelDispositivo.pedir(
                            actividad = actividad ?: return@buscarCuenta,
                            titulo = "Confirma que eres tú",
                            descripcion = "Usa tu huella o el PIN del teléfono " +
                                "para recuperar la contraseña de $quien.",
                            alVerificar = viewModel::identidadVerificada,
                            alFallar = viewModel::verificacionFallida,
                        )
                    }
                },
            )

            PasoDeRecuperacion.NUEVA_CLAVE -> PasoNuevaClave(
                estado = estado,
                onNuevaClaveChange = viewModel::onNuevaClaveChange,
                onConfirmacionChange = viewModel::onConfirmacionChange,
                onAlternarVer = viewModel::alternarVerClaves,
                onGuardar = viewModel::guardarNuevaClave,
            )

            PasoDeRecuperacion.LISTO -> PasoListo(
                usuario = estado.usuarioEncontrado,
                onEntrar = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        if (estado.error.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
            ) {
                Text(
                    text = estado.error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (estado.paso != PasoDeRecuperacion.LISTO) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}

@Composable
private fun PasoIdentificar(
    estado: RecuperarClaveUiState,
    disponibilidad: VerificacionDelDispositivo.Disponibilidad,
    hayActividad: Boolean,
    onIdentificadorChange: (String) -> Unit,
    onContinuar: () -> Unit,
) {
    val sePuede =
        disponibilidad == VerificacionDelDispositivo.Disponibilidad.LISTA && hayActividad

    Text(
        text = "Dinos qué cuenta quieres recuperar y confirma con la huella o el " +
            "PIN de este teléfono.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedTextField(
        value = estado.identificador,
        onValueChange = onIdentificadorChange,
        label = { Text("Usuario o correo") },
        singleLine = true,
        enabled = sePuede && !estado.cargando,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier.fillMaxWidth(),
    )

    // El aviso va DEBAJO del campo y no en su lugar: así se entiende que el
    // recorrido existe y qué falta para poder usarlo, en vez de encontrarse
    // una pantalla sin explicación.
    when {
        !hayActividad -> AvisoDeBloqueo(
            "No se puede verificar la identidad en esta pantalla."
        )

        disponibilidad == VerificacionDelDispositivo.Disponibilidad.SIN_BLOQUEO ->
            AvisoDeBloqueo(
                "Este teléfono no tiene huella ni PIN configurados, y son la " +
                    "única forma de comprobar que la cuenta es tuya. Ponle un " +
                    "bloqueo en los ajustes del teléfono y vuelve aquí."
            )

        disponibilidad == VerificacionDelDispositivo.Disponibilidad.NO_SOPORTADA ->
            AvisoDeBloqueo(
                "Este teléfono no permite confirmar tu identidad, así que desde " +
                    "aquí no se puede recuperar la contraseña."
            )
    }

    Button(
        onClick = onContinuar,
        enabled = sePuede && !estado.cargando && estado.identificador.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (estado.cargando) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Confirmar que soy yo")
        }
    }
}

@Composable
private fun AvisoDeBloqueo(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PasoNuevaClave(
    estado: RecuperarClaveUiState,
    onNuevaClaveChange: (String) -> Unit,
    onConfirmacionChange: (String) -> Unit,
    onAlternarVer: () -> Unit,
    onGuardar: () -> Unit,
) {
    val transformacion =
        if (estado.verClaves) VisualTransformation.None else PasswordVisualTransformation()

    Text(
        text = "Identidad confirmada. Escribe la contraseña nueva de " +
            "${estado.usuarioEncontrado}.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedTextField(
        value = estado.nuevaClave,
        onValueChange = onNuevaClaveChange,
        label = { Text("Nueva contraseña") },
        singleLine = true,
        visualTransformation = transformacion,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            // Un solo ojo para los dos campos: son la misma contraseña escrita
            // dos veces, y comparar lo que se tecleo exige verlos a la vez.
            IconButton(onClick = onAlternarVer) {
                Icon(
                    imageVector = if (estado.verClaves) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = if (estado.verClaves) {
                        "Ocultar las contraseñas"
                    } else {
                        "Ver las contraseñas"
                    },
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )

    OutlinedTextField(
        value = estado.confirmacion,
        onValueChange = onConfirmacionChange,
        label = { Text("Confirmar nueva contraseña") },
        singleLine = true,
        visualTransformation = transformacion,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier.fillMaxWidth(),
    )

    Button(
        onClick = onGuardar,
        enabled = !estado.cargando,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (estado.cargando) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Text("Guardar contraseña")
        }
    }
}

@Composable
private fun PasoListo(usuario: String, onEntrar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Contraseña cambiada",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Ya puedes entrar como $usuario con la contraseña nueva.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = onEntrar, modifier = Modifier.fillMaxWidth()) {
            Text("Ir a iniciar sesión")
        }
    }
}
