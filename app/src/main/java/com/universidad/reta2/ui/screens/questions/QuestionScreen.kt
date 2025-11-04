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
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.domain.models.Question
import androidx.activity.compose.BackHandler

// 🔹 FUNCIÓN PÚBLICA - Esta es la que usas en el NavGraph
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QuestionScreen(
    navController: NavController,
    competencyId: Int,
    levelId: Int
) {
    val viewModel: QuestionViewModel = hiltViewModel()

    // Estados del ViewModel
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedOptionId by viewModel.selectedOptionId.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val correctAnswers by viewModel.correctAnswers.collectAsState()
    val isQuizCompleted by viewModel.isQuizCompleted.collectAsState()
    val currentCompetence by viewModel.currentCompetence.collectAsState()

    // 🔹 Manejo de navegación
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is QuestionViewModel.NavigationEvent.NavigateToResults -> {
                    navController.navigate(
                        Screen.Results.createRoute(
                            competenceId = event.competenceId,
                            levelId = event.levelId,
                            score = event.score,
                            totalQuestions = event.totalQuestions,
                            timeSpent = event.timeSpent
                        )
                    ) {
                        popUpTo(Screen.Questions.route) { inclusive = true }
                    }
                }
                is QuestionViewModel.NavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // 🔹 Cargar preguntas al iniciar
    LaunchedEffect(competencyId, levelId) {
        if (questions.isEmpty()) {
            viewModel.loadQuestions(competencyId, levelId)
        }
    }

    // 🔹 Back handler
    BackHandler {
        viewModel.navigateBack()
    }

    // UI
    Column(Modifier.fillMaxSize()) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = currentCompetence?.name ?: "Competencia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Filled.ArrowBack, "Volver")
                }
            },
            actions = {
                Text(
                    text = formatTime(timeElapsed),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        )

        // Contenido
        if (questions.isEmpty()) {
            // Estado de carga
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Cargando preguntas...")
                }
            }
        } else {
            val currentQuestion = viewModel.getCurrentQuestion()
            if (currentQuestion != null) {
                QuestionContent(
                    viewModel = viewModel,
                    currentQuestion = currentQuestion,
                    currentQuestionIndex = currentQuestionIndex,
                    totalQuestions = questions.size,
                    streak = streak,
                    selectedOptionId = selectedOptionId,
                    isLastQuestion = viewModel.isLastQuestion()
                )
            } else {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Error al cargar la pregunta")
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun QuestionContent(
    viewModel: QuestionViewModel,
    currentQuestion: Question,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    streak: Int,
    selectedOptionId: Int?,
    isLastQuestion: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Contenido scrolleable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Racha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Racha $streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Barra de progreso
            LinearProgressIndicator(
                progress = viewModel.getProgress(),
                modifier = Modifier.fillMaxWidth()
            )

            // Progreso actual
            Text(
                text = "Pregunta ${currentQuestionIndex + 1} de $totalQuestions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Pregunta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🧠 Pregunta:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Opciones de respuesta
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
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
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

        // Botón inferior fijo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Button(
                onClick = { viewModel.submitAnswerAndAdvance() },
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

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
