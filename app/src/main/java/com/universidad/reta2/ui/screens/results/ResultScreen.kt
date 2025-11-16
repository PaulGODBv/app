package com.universidad.reta2.ui.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import kotlinx.coroutines.delay
import com.universidad.reta2.ui.navigation.Screen

@Composable
fun ResultsScreen(
    navController: NavController,
    competencyId: Int,
    levelId: Int,
    score: Int,
    totalQuestions: Int,
    timeSpent: Int,
    origin: String,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    // Usar el ViewModel para obtener datos de la competencia
    val competency by viewModel.getCompetency(competencyId).collectAsState(initial = null)
    val level by viewModel.getLevel(competencyId, levelId).collectAsState(initial = null)

    val percentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0

    // Actualiza el progreso una sola vez al mostrar la pantalla
    val hasUpdatedProgress = remember { mutableStateOf(false) }
    val previousRoute by remember {
        derivedStateOf {
            navController.previousBackStackEntry?.destination?.route ?: ""
        }
    }
    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(competencyId, levelId) {
        if (!hasUpdatedProgress.value) {
            viewModel.updateUserProgress(competencyId, levelId, score, totalQuestions)
            hasUpdatedProgress.value = true
        }
    }

    // Resetear después de un tiempo
    LaunchedEffect(Unit) {
        delay(1000)
        isNavigating = false
    }

    val buttonText = when(origin){
        "progress" -> "Volver a progreso"
        else -> "Volver a competencias"
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Contenido scrolleable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icono de éxito
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completado",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Título
            Text(
                text = "¡Nivel Completado!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Información del nivel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = competency?.name ?: "Competencia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = level?.name ?: "Nivel",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Resultados
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Resultados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Puntuación
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Puntuación",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Puntuación",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$score/$totalQuestions",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // Tiempo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Tiempo",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Tiempo empleado",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatTime(timeSpent),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Mensaje motivacional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        percentage >= 80 -> MaterialTheme.colorScheme.primaryContainer
                        percentage >= 60 -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = when {
                        percentage >= 80 -> "¡Excelente trabajo! 🎉"
                        percentage >= 60 -> "¡Buen trabajo! 👍"
                        else -> "¡Sigue practicando! 💪"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    color = when {
                        percentage >= 80 -> MaterialTheme.colorScheme.onPrimaryContainer
                        percentage >= 60 -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón fijo en la parte inferior
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isNavigating) return@Button
                        isNavigating = true

                        try {
                            when (origin) {
                                "progress" -> {
                                    println("🔄 Returning to Progress (origin: $origin)")
                                    navController.navigate(Screen.Progress.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                                else -> {
                                    println("🔄 Returning to Competencies (origin: $origin)")
                                    navController.navigate(Screen.Competencies.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            println("❌ Navigation failed: ${e.message}")
                            navController.popBackStack(Screen.Home.route, false)
                        }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isNavigating // 🔥 Deshabilitar durante navegación
                ) {
                    if (isNavigating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                OutlinedButton(
                    onClick = {
                        println("🔄 Retrying level: $levelId")
                        navController.navigate(Screen.Questions.createRoute(competencyId, levelId)) {
                            // Limpiar stack apropiadamente según procedencia
                            when {
                                previousRoute.startsWith("progress") -> popUpTo(Screen.Progress.route)
                                previousRoute.contains("competence") -> popUpTo(Screen.CompetenceDetail.route)
                                else -> popUpTo(Screen.Competencies.route)
                            }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (percentage >= 70) "Siguiente Nivel" else "Reintentar",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}