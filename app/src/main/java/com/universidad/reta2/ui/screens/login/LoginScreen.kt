package com.udes.reta2app.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import com.udes.reta2app.data.UserRepository
import com.udes.reta2app.data.SessionManager
import com.udes.reta2app.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // Obtener mensaje de éxito de los argumentos de navegación
    LaunchedEffect(Unit) {
        val backStackEntry = navController.currentBackStackEntry
        val message = backStackEntry?.arguments?.getString("successMessage")
        if (message != null) {
            successMessage = message
        }
    }

    // Auto-dismiss para mensajes
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            delay(4000) // 4 segundos
            errorMessage = ""
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage.isNotEmpty()) {
            delay(3000) // 3 segundos
            successMessage = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = usernameOrEmail,
            onValueChange = {
                usernameOrEmail = it
                errorMessage = "" // Limpiar error al escribir
            },
            label = { Text("Usuario o Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = "" // Limpiar error al escribir
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (successMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = successMessage,
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
                    try {
                        errorMessage = ""

                        when {
                            usernameOrEmail.isEmpty() -> {
                                errorMessage = "El usuario o correo no puede estar vacío"
                            }
                            password.isEmpty() -> {
                                errorMessage = "La contraseña no puede estar vacía"
                            }
                            else -> {
                                val user = userRepository.getUserByUsernameOrEmail(usernameOrEmail, usernameOrEmail)

                                if (user != null && user.password == password) {
                                    // Guardar sesión del usuario
                                    SessionManager.saveUserSession(
                                        context,
                                        user.username,
                                        user.email
                                    )
                                    navController.navigate(Screen.Dashboard.route)
                                } else {
                                    errorMessage = "Usuario/Correo o contraseña incorrectos"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error al verificar credenciales: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {
            navController.navigate(Screen.Registration.route)
        }) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}