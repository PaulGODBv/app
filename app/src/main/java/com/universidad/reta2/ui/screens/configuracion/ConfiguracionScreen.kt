package com.universidad.reta2.ui.screens.configuracion

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.ui.components.SeccionPlegable
import com.universidad.reta2.ui.components.SobreReta2Card
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.ui.screens.profile.ProfileViewModel

/** Qué sección está abierta. `null` es ninguna. */
private enum class Seccion { PERSONAL, APARIENCIA, SONIDO, CLAVE, SOBRE }

/**
 * Los ajustes de la aplicación, plegados.
 *
 * POR QUÉ EXISTE, Y POR QUÉ SE LLEVÓ ESTO DE PERFIL. Perfil enseñaba a la vez
 * el nivel, las insignias, los datos personales, el tema, el sonido y el
 * cambio de contraseña, con una barra fija abajo que tenía «Guardar cambios»
 * **siempre presente aunque no hubiera nada que guardar**, y que por ser fija
 * cortaba el contenido a media ficha. Debajo de ese botón grande y relleno iba
 * «Cerrar sesión», que es la acción más destructiva de la pantalla: dos pesos
 * opuestos pegados.
 *
 * Aquí no hay barra fija. **Guardar vive dentro de «Información personal»**,
 * que es la única sección con algo que guardar, y cerrar sesión es la última
 * fila, lejos de todo lo demás.
 *
 * SE ABRE UNA SOLA SECCIÓN A LA VEZ. Permitir varias devolvería la pantalla
 * larga que se quería evitar; cerrando la anterior, lo que se está tocando
 * queda siempre a la vista sin desplazar.
 */
@Composable
fun ConfiguracionScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var abierta by remember { mutableStateOf<Seccion?>(null) }

    fun alternar(cual: Seccion) {
        abierta = if (abierta == cual) null else cual
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        // ----- Información personal -----
        SeccionPlegable(
            titulo = "Información personal",
            icono = Icons.Default.Person,
            resumen = state.username.ifBlank { null },
            expandida = abierta == Seccion.PERSONAL,
            alPulsar = { alternar(Seccion.PERSONAL) },
        ) {
            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("Nombre de usuario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )

            // El guardar que antes vivía en la barra fija. Aquí abajo, dentro
            // de lo único que se edita.
            Button(
                onClick = { viewModel.updateProfile() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios")
                }
            }
        }

        // ----- Apariencia -----
        val themeMode by viewModel.themeMode.collectAsState()
        SeccionPlegable(
            titulo = "Apariencia",
            icono = Icons.Default.Palette,
            resumen = when (themeMode) {
                1 -> "Modo claro"
                2 -> "Modo oscuro"
                else -> "Seguir el sistema"
            },
            expandida = abierta == Seccion.APARIENCIA,
            alPulsar = { alternar(Seccion.APARIENCIA) },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when (themeMode) {
                        1 -> "Modo Claro"
                        2 -> "Modo Oscuro"
                        else -> "Seguir Sistema"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                IconButton(onClick = { viewModel.setThemeMode((themeMode + 1) % 3) }) {
                    Icon(
                        imageVector = when (themeMode) {
                            1 -> Icons.Default.LightMode
                            2 -> Icons.Default.DarkMode
                            else -> Icons.Default.Settings
                        },
                        contentDescription = "Cambiar tema",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Elegir tema es una acción, no un logro: el seleccionado va en
            // azul y los otros en gris neutro. Con el tonal de Material
            // heredaban `secondaryContainer`, que en esta paleta es el ámbar
            // reservado a rachas y desbloqueos.
            val tonalNeutro = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(0 to "Auto", 1 to "Claro", 2 to "Oscuro").forEach { (modo, etiqueta) ->
                    Button(
                        onClick = { viewModel.setThemeMode(modo) },
                        colors = if (themeMode == modo) ButtonDefaults.buttonColors() else tonalNeutro,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(etiqueta, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // ----- Sonido y vibración -----
        val vibracion by viewModel.vibracion.collectAsState()
        val efectos by viewModel.efectos.collectAsState()
        val musica by viewModel.musica.collectAsState()
        SeccionPlegable(
            titulo = "Sonido y vibración",
            icono = Icons.Default.VolumeUp,
            resumen = resumenDeSonido(musica, efectos, vibracion),
            expandida = abierta == Seccion.SONIDO,
            alPulsar = { alternar(Seccion.SONIDO) },
        ) {
            val volMusica by viewModel.volumenMusica.collectAsState()
            val volEfectos by viewModel.volumenEfectos.collectAsState()

            FilaDeAjuste(
                icono = Icons.Default.MusicNote,
                titulo = "Música de menú",
                detalle = "Solo en el menú; dentro de un nivel no suena",
                activo = musica,
                alCambiar = viewModel::setMusica,
            )
            DeslizadorDeVolumen(
                valor = volMusica,
                habilitado = musica,
                alCambiar = viewModel::setVolumenMusica,
            )

            FilaDeAjuste(
                icono = Icons.Default.VolumeUp,
                titulo = "Efectos de sonido",
                detalle = "Confirmación corta en cada respuesta",
                activo = efectos,
                alCambiar = viewModel::setEfectos,
            )
            DeslizadorDeVolumen(
                valor = volEfectos,
                habilitado = efectos,
                alCambiar = viewModel::setVolumenEfectos,
                alSoltar = viewModel::probarEfecto,
            )

            // La vibración no lleva deslizador: no tiene volumen, y en los
            // teléfonos sin control de amplitud ni siquiera tiene intensidad.
            // Ofrecer uno sería prometer lo que el hardware no da.
            FilaDeAjuste(
                icono = Icons.Default.Vibration,
                titulo = "Vibración",
                detalle = "Al acertar, fallar y superar un nivel",
                activo = vibracion,
                alCambiar = viewModel::setVibracion,
            )

            // Si el teléfono está en silencio no va a sonar nada, aunque los
            // interruptores estén encendidos. Decirlo aquí ahorra pensar que
            // la aplicación está rota.
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ----- Contraseña -----
        SeccionPlegable(
            titulo = "Contraseña",
            icono = Icons.Default.Lock,
            expandida = abierta == Seccion.CLAVE,
            alPulsar = { alternar(Seccion.CLAVE) },
        ) {
            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                label = { Text("Contraseña actual") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                label = { Text("Nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirmar nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { viewModel.updateProfile() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cambiar contraseña")
            }
        }

        // ----- Sobre Reta2 -----
        SeccionPlegable(
            titulo = "Sobre Reta2",
            icono = Icons.Default.Info,
            expandida = abierta == Seccion.SOBRE,
            alPulsar = { alternar(Seccion.SOBRE) },
        ) {
            SobreReta2Card()
        }

        // Los avisos van al final y no en una franja fija: nacen al pulsar
        // guardar, que ahora está dentro de su sección y no abajo del todo.
        if (state.errorMessage.isNotEmpty()) {
            MensajeDeEstado(
                texto = state.errorMessage,
                color = MaterialTheme.colorScheme.errorContainer,
                colorTexto = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        if (state.successMessage.isNotEmpty()) {
            MensajeDeEstado(
                texto = state.successMessage,
                color = MaterialTheme.colorScheme.primaryContainer,
                colorTexto = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        // Lejos del resto y sin relleno: es la acción que no conviene pulsar
        // por descuido.
        TextButton(
            onClick = {
                viewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** «Música, efectos y vibración» · «Solo vibración» · «Todo en silencio». */
private fun resumenDeSonido(musica: Boolean, efectos: Boolean, vibracion: Boolean): String {
    val encendidos = buildList {
        if (musica) add("música")
        if (efectos) add("efectos")
        if (vibracion) add("vibración")
    }
    return when {
        encendidos.isEmpty() -> "Todo apagado"
        encendidos.size == 3 -> "Música, efectos y vibración"
        else -> encendidos.joinToString(" y ").replaceFirstChar { it.uppercase() }
    }
}

@Composable
private fun MensajeDeEstado(
    texto: String,
    color: androidx.compose.ui.graphics.Color,
    colorTexto: androidx.compose.ui.graphics.Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(16.dp),
            color = colorTexto,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun FilaDeAjuste(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    activo: Boolean,
    alCambiar: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (activo) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
 * *eso*— y evita que la sección cambie de alto al encender y apagar.
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
    alSoltar: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Slider(
            value = valor,
            onValueChange = alCambiar,
            onValueChangeFinished = alSoltar,
            enabled = habilitado,
            modifier = Modifier.weight(1f),
        )
        Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "${(valor * 100).toInt()} %",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
