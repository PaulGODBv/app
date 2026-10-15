package com.universidad.reta2.ui.screens.registration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.data.source.ProgramasUdes
import com.universidad.reta2.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    navController: NavController,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current


    // Auto-dismiss para mensajes
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            delay(4000)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage.isNotEmpty()) {
            delay(3000)
            // Navegar al login después de mostrar mensaje de éxito
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Registration.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Registro de Usuario",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = { Text("Nombre de usuario") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text),
            isError = uiState.errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // El código va ANTES que el correo, y no es un capricho de orden: el
        // correo se rellena solo a partir de él, y para entender por qué
        // aparece escrito hay que haber visto antes de dónde sale.
        OutlinedTextField(
            value = uiState.studentCode,
            onValueChange = { viewModel.onStudentCodeChange(it) },
            label = { Text("Código estudiantil") },
            supportingText = { Text("11 dígitos") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = uiState.errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // El correo institucional ES el código más el dominio, así que pedirlo
        // aparte era pedir dos veces el mismo dato. Sigue siendo editable:
        // bloquearlo dejaría tirado a quien tenga una dirección distinta.
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Correo electrónico") },
            supportingText = {
                Text(
                    if (uiState.correoEditadoAMano) "Debe terminar en @mail.udes.edu.co"
                    else "Se completa solo con tu código. Puedes cambiarlo."
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = uiState.errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // ----- Programa académico -----
        //
        // Sugiere mientras se escribe, en vez de desplegar una lista: son 49
        // programas y recorrerlos con el dedo es peor que teclear tres letras.
        // Escribiendo «softw» quedan dos.
        //
        // Antes esto era un campo libre, y por eso en el panel convivían
        // «Ingeniería de software» e «Ingeniería de Software» como si fueran
        // dos carreras distintas.
        val sugerencias = if (uiState.esOtroPrograma) emptyList()
                          else ProgramasUdes.sugerencias(uiState.studentProgram)
        val yaEsDelCatalogo = ProgramasUdes.exacto(uiState.studentProgram) != null

        OutlinedTextField(
            value = uiState.studentProgram,
            onValueChange = { viewModel.onStudentProgramChange(it) },
            label = {
                Text(if (uiState.esOtroPrograma) "Escribe tu programa" else "Programa académico")
            },
            supportingText = {
                Text(
                    when {
                        uiState.esOtroPrograma -> "Fuera del catálogo de Bucaramanga"
                        yaEsDelCatalogo -> "Programa de la UDES"
                        else -> "Escribe y elige de la lista"
                    }
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            isError = uiState.errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        // Las sugerencias van EN LÍNEA y no flotando sobre el campo: en un
        // teléfono un menú anclado tapa justo lo que se está escribiendo.
        if (sugerencias.isNotEmpty() && !yaEsDelCatalogo) {
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    sugerencias.forEach { programa ->
                        Text(
                            text = programa,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.elegirPrograma(programa) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // La salida para quien no esté en el catálogo —otra sede, un programa
        // recién abierto—. Sin esto, exigir la lista deja gente fuera.
        TextButton(
            onClick = {
                if (uiState.esOtroPrograma) viewModel.volverAlCatalogo()
                else viewModel.activarOtroPrograma()
            }
        ) {
            Text(
                if (uiState.esOtroPrograma) "Buscar en la lista de la UDES"
                else "Mi programa no está en la lista"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            isError = uiState.errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),

            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus()}
            ),
            isError = uiState.errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (uiState.successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = uiState.successMessage,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    when (viewModel.register()) {
                        is RegistrationResult.Success -> {
                            // La navegación se maneja automáticamente con el mensaje de éxito
                        }
                        else -> {
                            // Los errores ya se manejan en el ViewModel
                        }
                    }
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Registrar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {
            navController.navigate(Screen.Login.route)
        }) {
            Text("¿Ya tienes cuenta? Inicia sesión aquí")
        }
    }
}
