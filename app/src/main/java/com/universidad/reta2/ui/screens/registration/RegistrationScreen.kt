package com.universidad.reta2.ui.screens.registration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.data.source.ProgramasUdes
import com.universidad.reta2.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * El registro, en tres pasos.
 *
 * Eran seis campos de golpe. Seis campos en la pantalla de un telefono se leen
 * como un tramite, y lo que cansa de un formulario largo no es rellenarlo: es
 * no saber donde acaba. Partido en tres, cada pantalla hace una pregunta y la
 * barra de arriba dice cuanto queda.
 *
 * El reparto no es arbitrario. Van juntos los campos que se piensan juntos:
 * quien eres (nombre y codigo), lo academico (correo y programa) y la clave.
 * Ademas el codigo tiene que ir ANTES que el correo, porque el correo sale de
 * el, y para entender de donde sale hay que haberlo visto primero.
 */
@Composable
fun RegistrationScreen(
    navController: NavController,
    viewModel: RegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // El error se borra solo; el exito ya no, porque ahora tiene su pantalla.
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            delay(4000)
            viewModel.clearMessages()
        }
    }

    if (uiState.registroCompletado) {
        PantallaDeCuentaCreada(
            onContinuar = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Registration.route) { inclusive = true }
                }
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Crear tu cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ----- Cuanto falta -----
        //
        // La barra y el rotulo dicen lo mismo de dos formas: una se ve de un
        // vistazo y el otro se lee. En un formulario de varios pasos, saber
        // cuantos quedan es la mitad de la paciencia.
        LinearProgressIndicator(
            progress = { uiState.paso / RegistrationViewModel.PASOS.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Paso ${uiState.paso} de ${RegistrationViewModel.PASOS} · " +
                    tituloDelPaso(uiState.paso),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (uiState.paso) {
            1 -> PasoQuienEres(uiState, viewModel)
            2 -> PasoLoAcademico(uiState, viewModel)
            else -> PasoLaClave(uiState, viewModel, focusManager::clearFocus)
        }

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

        Spacer(modifier = Modifier.height(24.dp))

        // ----- Avanzar -----
        Button(
            onClick = {
                if (uiState.paso < RegistrationViewModel.PASOS) {
                    viewModel.irAlPasoSiguiente()
                } else {
                    coroutineScope.launch { viewModel.register() }
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
                Text(
                    if (uiState.paso < RegistrationViewModel.PASOS) "Continuar"
                    else "Crear cuenta"
                )
            }
        }

        // Volver atras solo existe cuando hay atras adonde volver.
        if (uiState.paso > 1) {
            TextButton(
                onClick = { viewModel.volverAlPasoAnterior() },
                enabled = !uiState.isLoading
            ) {
                Text("Atrás")
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                Text("¿Ya tienes cuenta? Inicia sesión aquí")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun tituloDelPaso(paso: Int) = when (paso) {
    1 -> "Quién eres"
    2 -> "Tus datos académicos"
    else -> "Tu contraseña"
}

/* ---------- Paso 1: nombre y código ---------- */

@Composable
private fun PasoQuienEres(
    uiState: RegistrationUiState,
    viewModel: RegistrationViewModel
) {
    OutlinedTextField(
        value = uiState.username,
        onValueChange = { viewModel.onUsernameChange(it) },
        label = { Text("Nombre de usuario") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            // El teclado no corrige aqui. Corrigiendo, «prueba_claude_borrar»
            // se convirtio en «prueba_claude_borrar de» durante una prueba, y
            // a un nombre de usuario poco comun le pasa igual.
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next
        ),
        isError = uiState.errorMessage.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = uiState.studentCode,
        onValueChange = { viewModel.onStudentCodeChange(it) },
        label = { Text("Código estudiantil") },
        singleLine = true,
        supportingText = { Text("11 dígitos") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        isError = uiState.errorMessage.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )
}

/* ---------- Paso 2: correo y programa ---------- */

@Composable
private fun PasoLoAcademico(
    uiState: RegistrationUiState,
    viewModel: RegistrationViewModel
) {
    OutlinedTextField(
        value = uiState.email,
        onValueChange = { viewModel.onEmailChange(it) },
        label = { Text("Correo electrónico") },
        singleLine = true,
        supportingText = {
            Text(
                if (uiState.correoEditadoAMano) "Debe terminar en @mail.udes.edu.co"
                else "Se completó solo con tu código. Puedes cambiarlo."
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next
        ),
        isError = uiState.errorMessage.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Sugiere mientras se escribe en vez de desplegar los 49: recorrerlos con
    // el dedo es peor que teclear tres letras. Con «softw» quedan dos.
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
            imeAction = ImeAction.Done
        ),
        isError = uiState.errorMessage.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )

    // En linea y no flotando: en un telefono un menu anclado tapa justo lo
    // que se esta escribiendo.
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

    // La salida para quien no este en el catalogo —otra sede, un programa
    // recien abierto—. Exigir la lista sin escape deja gente fuera.
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
}

/* ---------- Paso 3: la contraseña ---------- */

@Composable
private fun PasoLaClave(
    uiState: RegistrationUiState,
    viewModel: RegistrationViewModel,
    quitarFoco: () -> Unit
) {
    // Un solo interruptor para los dos campos: son la misma contraseña, y
    // esconder una mientras se ve la otra no protege de nada.
    var verClave by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = uiState.password,
        onValueChange = { viewModel.onPasswordChange(it) },
        label = { Text("Contraseña") },
        singleLine = true,
        visualTransformation = if (verClave) VisualTransformation.None
                               else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { verClave = !verClave }) {
                Icon(
                    imageVector = if (verClave) Icons.Filled.VisibilityOff
                                  else Icons.Filled.Visibility,
                    // Describe lo que HARA al pulsarlo, no el estado: es lo
                    // que anuncia un lector de pantalla cuando llega al boton.
                    contentDescription = if (verClave) "Ocultar la contraseña"
                                         else "Ver la contraseña"
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        isError = uiState.errorMessage.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = uiState.confirmPassword,
        onValueChange = { viewModel.onConfirmPasswordChange(it) },
        label = { Text("Confirmar contraseña") },
        singleLine = true,
        visualTransformation = if (verClave) VisualTransformation.None
                               else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { quitarFoco() }),
        isError = uiState.errorMessage.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    )
}

/* ---------- El final ---------- */

/**
 * Antes aqui habia una espera de tres segundos que llevaba al login sola.
 *
 * Un temporizador decide por el usuario: si estaba leyendo, se le va la
 * pantalla; si no, espera mirando. Y le quita el cierre de la tarea, que es
 * justo lo que uno quiere despues de rellenar un formulario. Ahora termina el.
 */
@Composable
private fun PantallaDeCuentaCreada(onContinuar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tu cuenta está lista",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Entra con el usuario y la contraseña que acabas de crear.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinuar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }
    }
}
