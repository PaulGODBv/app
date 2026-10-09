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
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.media.AudioManager
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
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.MusicNote
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

            // ----- Sonido y vibración -----
            // Tres ajustes y no uno: responden a cosas distintas. Los efectos
            // y la vibración son respuesta a algo que hizo el estudiante y van
            // encendidos; la música empieza sola, así que va apagada hasta que
            // alguien la pida.
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
                        text = "Sonido y vibración",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    val vibracion by viewModel.vibracion.collectAsState()
                    val efectos by viewModel.efectos.collectAsState()
                    val musica by viewModel.musica.collectAsState()

                    val volMusica by viewModel.volumenMusica.collectAsState()
                    val volEfectos by viewModel.volumenEfectos.collectAsState()

                    FilaDeAjuste(
                        icono = Icons.Default.MusicNote,
                        titulo = "Música de menú",
                        detalle = "Solo en el menú; dentro de un nivel no suena",
                        activo = musica,
                        alCambiar = viewModel::setMusica
                    )
                    DeslizadorDeVolumen(
                        valor = volMusica,
                        habilitado = musica,
                        alCambiar = viewModel::setVolumenMusica
                    )

                    FilaDeAjuste(
                        icono = Icons.Default.VolumeUp,
                        titulo = "Efectos de sonido",
                        detalle = "Confirmación corta en cada respuesta",
                        activo = efectos,
                        alCambiar = viewModel::setEfectos
                    )
                    DeslizadorDeVolumen(
                        valor = volEfectos,
                        habilitado = efectos,
                        alCambiar = viewModel::setVolumenEfectos,
                        alSoltar = viewModel::probarEfecto
                    )

                    // La vibración no lleva deslizador: no tiene volumen, y en
                    // los teléfonos sin control de amplitud ni siquiera tiene
                    // intensidad. Ofrecer uno sería prometer lo que el
                    // hardware no da.
                    FilaDeAjuste(
                        icono = Icons.Default.Vibration,
                        titulo = "Vibración",
                        detalle = "Al acertar, fallar y superar un nivel",
                        activo = vibracion,
                        alCambiar = viewModel::setVibracion
                    )

                    // Si el teléfono está en silencio no va a sonar nada,
                    // aunque los interruptores estén encendidos. Decirlo aquí
                    // ahorra pensar que la aplicación está rota.
                    val contexto = LocalContext.current
                    val enSilencio = remember {
                        val audio = contexto.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        audio?.ringerMode == AudioManager.RINGER_MODE_SILENT
                    }
                    if (enSilencio && (efectos || musica)) {
                        Text(
                            text = "El teléfono está en silencio, así que no sonará. " +
                                "La vibración sí funciona.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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


/**
 * Una fila de ajuste: icono, nombre, una línea de explicación e interruptor.
 *
 * El icono se apaga con el ajuste —color primario encendido, gris apagado—
 * para que el estado se lea de un vistazo sin tener que mirar el interruptor,
 * y la línea de detalle existe porque «Música de menú» no dice por sí sola
 * que dentro de un nivel no suena, que es justo lo que alguien preguntaría.
 */
@Composable
private fun FilaDeAjuste(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    detalle: String,
    activo: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (activo) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = activo, onCheckedChange = alCambiar)
    }
}


/**
 * El volumen de una de las dos fuentes de sonido.
 *
 * Se atenúa y se bloquea cuando su interruptor está apagado, en lugar de
 * esconderse: dejarlo a la vista enseña la relación —esto es el volumen de
 * *eso*— y evita que la tarjeta cambie de alto al encender y apagar, que es lo
 * que haría saltar el resto de la pantalla.
 *
 * El valor **no** se borra al apagar. Apagar y volver a encender devuelve el
 * volumen que había, que es la diferencia entre un interruptor y un deslizador
 * arrastrado a cero.
 */
@Composable
private fun DeslizadorDeVolumen(
    valor: Float,
    habilitado: Boolean,
    alCambiar: (Float) -> Unit,
    alSoltar: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Slider(
            value = valor,
            onValueChange = alCambiar,
            onValueChangeFinished = { if (habilitado) alSoltar?.invoke() },
            enabled = habilitado,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${(valor * 100).toInt()} %",
            style = MaterialTheme.typography.labelMedium,
            color = if (habilitado) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.outline
            },
            modifier = Modifier.width(44.dp)
        )
    }
}
