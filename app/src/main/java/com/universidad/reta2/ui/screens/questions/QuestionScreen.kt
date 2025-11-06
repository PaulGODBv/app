package com.universidad.reta2.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
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
import androidx.navigation.NavHostController
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.domain.models.Question
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    navController: NavHostController,
    competencyId: Int,
    levelId: Int,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar preguntas
    LaunchedEffect(competencyId, levelId) {
        viewModel.loadQuestions(competencyId, levelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isLoading) "Cargando..."
                        else "Pregunta ${uiState.currentQuestionIndex + 1}/${uiState.questions.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    Text(formatTime(uiState.timeElapsed))
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    //ErrorState(error = uiState.error)
                }
                uiState.questions.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    QuestionContent(
                        currentQuestion = uiState.questions[uiState.currentQuestionIndex],
                        selectedOptionId = uiState.selectedOptionId,
                        isLastQuestion = viewModel.isLastQuestion(),
                        onOptionSelected = viewModel::selectOption,
                        onNextClicked = {
                            if (viewModel.isLastQuestion()) {
                                navController.navigate(
                                    Screen.Results.createRoute(
                                        competenceId = competencyId,
                                        levelId = levelId,
                                        score = uiState.score,
                                        totalQuestions = uiState.questions.size,
                                        timeSpent = uiState.timeElapsed
                                    )
                                ) {
                                    popUpTo(Screen.Questions.route) { inclusive = true }
                                }
                            } else {
                                viewModel.nextQuestion()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionContent(
    currentQuestion: Question,
    selectedOptionId: Int?,
    isLastQuestion: Boolean,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pregunta
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = currentQuestion.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Opciones
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentQuestion.options.forEach { option ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(option.id) },
                    shape = MaterialTheme.shapes.small,
                    color = if (selectedOptionId == option.id) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    tonalElevation = 1.dp,
                    border = BorderStroke(
                        1.dp,
                        if (selectedOptionId == option.id) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOptionId == option.id,
                            onClick = { onOptionSelected(option.id) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option.text)
                    }
                }
            }
        }

        // Botón Siguiente/Finalizar
        Button(
            onClick = onNextClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedOptionId != null
        ) {
            Text(if (isLastQuestion) "Finalizar" else "Siguiente")
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Error: $error")
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No hay preguntas disponibles")
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}