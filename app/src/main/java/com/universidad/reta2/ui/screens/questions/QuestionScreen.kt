package com.universidad.reta2.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
//import com.google.android.gms.dtdi.analytics.Results
import com.universidad.reta2.ui.navigation.Screen
import androidx.activity.compose.BackHandler


private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    navController: NavController,
    competencyId: String,
    levelId: String,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    // Estados del ViewModel
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedOptionId by viewModel.selectedOptionId.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val correctAnswers by viewModel.correctAnswers.collectAsState()
    val isQuizCompleted by viewModel.isQuizCompleted.collectAsState()

    val currentQuestion = viewModel.getCurrentQuestion()
    val isLastQuestion = viewModel.isLastQuestion()

    // Cargar preguntas al iniciar
    LaunchedEffect(competencyId, levelId) {
        if (questions.isEmpty()) {
            // TODO: Reemplazar con tu lógica de carga de CompetencyData
            val levelQuestions = emptyList<com.universidad.reta2.domain.models.Question>()
            viewModel.loadQuestions(levelQuestions)
        }
    }

    // Navegar a resultados cuando se complete
    LaunchedEffect(isQuizCompleted) {
        if (isQuizCompleted) {
            navController.navigate(
                Screen.Results.createRoute(
                    competencyId = competencyId,
                    levelId = levelId,
                    score = correctAnswers,
                    totalQuestions = questions.size,
                    timeSpent = timeElapsed
                )
            ) {
                // Limpiar el back stack para no volver a las preguntas
                popUpTo(Screen.Questions.route) { inclusive = true }
            }
        }
    }

    // Manejar back press
    BackHandler {
        viewModel.stopTimer()
        navController.popBackStack()
    }

    if (currentQuestion == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (questions.isEmpty()) {
                Text("Cargando preguntas...")
            } else {
                Text("Error al cargar la pregunta")
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con temporizador
        TopAppBar(
            title = {
                Text(
                    text = "Competencia", // TODO: Obtener nombre real de la competencia
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.stopTimer()
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            actions = {
                // Temporizador
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTime(timeElapsed),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Contenido scrolleable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Racha
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔥",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Racha $streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Barra de progreso
            LinearProgressIndicator(
                progress = viewModel.getProgress(),
                modifier = Modifier.fillMaxWidth()
            )

            // Información de progreso actual
            Text(
                text = "Pregunta ${currentQuestionIndex + 1} de ${questions.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Información del nivel (placeholder - TODO: cargar datos reales)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nivel Actual", // TODO: Obtener nombre real del nivel
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Completa todas las preguntas para avanzar", // TODO: Descripción real
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Pregunta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🧠 Pregunta:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Opciones de respuesta (adaptado a la nueva estructura con IDs)
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = selectedOptionId == option.id
                    val optionLetter = ('A' + index).toString()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { viewModel.selectOption(option.id) },
                                role = Role.RadioButton
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = if (isSelected) CardDefaults.cardElevation(4.dp)
                        else CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectOption(option.id) }
                            )
                            Text(
                                text = "$optionLetter) ${option.text}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
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
            Button(
                onClick = {
                    // Enviar respuesta y avanzar automáticamente
                    viewModel.submitAnswerAndAdvance()
                },
                enabled = selectedOptionId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isLastQuestion) "Finalizar" else "Siguiente",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}