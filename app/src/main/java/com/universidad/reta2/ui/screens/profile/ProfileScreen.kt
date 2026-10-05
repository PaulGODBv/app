package com.universidad.reta2.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.ui.navigation.Screen
import kotlinx.coroutines.delay
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.eventChannel.collect { event ->
            when (event) {
                ProfileViewModel.ProfileEvent.ThemeChanged -> {
                    // El tema cambia reactivamente, no es necesaria acción extra aquí
                }
            }
        }
    }

    // Auto-limpiar mensajes
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage.isNotEmpty()) {
            delay(4000)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage.isNotEmpty()) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Componentes Premium de Experiencia y Gamificación
            com.universidad.reta2.ui.components.LevelXpBar(totalQuestionsAnswered = state.totalQuestionsAnswered)
            com.universidad.reta2.ui.components.UnlockableBadgesGrid(
                totalQuestionsAnswered = state.totalQuestionsAnswered,
                mejorRacha = state.maxStreak
            )

            // ----- Información Personal -----
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Nombre de usuario") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Correo electrónico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ----- Apariencia (Modo Oscuro) -----
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Apariencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    val themeMode by viewModel.themeMode.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (themeMode) {
                                1 -> "Modo Claro"
                                2 -> "Modo Oscuro"
                                else -> "Seguir Sistema"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )

                        IconButton(onClick = {
                            val nextMode = (themeMode + 1) % 3
                            viewModel.setThemeMode(nextMode)
                        }) {
                            Icon(
                                imageVector = when (themeMode) {
                                    1 -> Icons.Default.LightMode
                                    2 -> Icons.Default.DarkMode
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = "Cambiar tema",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Botones de opción rápida (Reemplazando FilterChip por Button por seguridad).
                    // Elegir tema es una acción, no un logro: el seleccionado va en
                    // azul y los otros en gris neutro. Con el tonal de Material
                    // heredaban `secondaryContainer`, que en esta paleta es el
                    // ámbar reservado a rachas y desbloqueos.
                    val tonalNeutro = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setThemeMode(0) },
                            colors = if (themeMode == 0) ButtonDefaults.buttonColors() else tonalNeutro,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Auto", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { viewModel.setThemeMode(1) },
                            colors = if (themeMode == 1) ButtonDefaults.buttonColors() else tonalNeutro,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Claro", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { viewModel.setThemeMode(2) },
                            colors = if (themeMode == 2) ButtonDefaults.buttonColors() else tonalNeutro,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Oscuro", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // ----- Cambio de Contraseña -----
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cambiar Contraseña",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = state.currentPassword,
                        onValueChange = viewModel::onCurrentPasswordChange,
                        label = { Text("Contraseña actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = viewModel::onNewPasswordChange,
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Confirmar nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ----- Mensajes de error o éxito -----
        // Van fuera del scroll, pegados a las acciones: el aviso nace al pulsar
        // «Guardar cambios» y antes podía quedar tapado por esa misma zona fija.
        if (state.errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                MessageCard(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.errorContainer,
                    textColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        if (state.successMessage.isNotEmpty()) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                MessageCard(
                    text = state.successMessage,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // ----- Acciones -----
        // La acción principal conserva el peso visual; cerrar sesión pasa a ser un
        // botón de texto: sigue accesible, pero deja de competir con el contenido.
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { viewModel.updateProfile() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Guardar cambios")
                    }
                }
            }

            TextButton(
                onClick = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        // Limpiar el back stack completamente para destruir ViewModels
                        popUpTo(0) { inclusive = true }
                        // Evitar múltiples instancias de la pantalla de login
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}

// Move MessageCard outside of ProfileScreen function and keep it private
@Composable
private fun MessageCard(text: String, color: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}
