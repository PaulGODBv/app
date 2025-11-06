package com.universidad.reta2.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.domain.models.QuestionOption
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun QuestionScreenUltraSafe(
    navController: NavHostController,
    competencyId: Int,
    levelId: Int,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    // Estado local para control absoluto del ciclo de vida
    var isCompositionActive by remember { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsState()

    // Efecto para manejar el ciclo de vida de la composición
    DisposableEffect(competencyId, levelId) {
        println("🎬 Iniciando QuestionScreen para competency: $competencyId, level: $levelId")
        isCompositionActive = true
        viewModel.activate()

        onDispose {
            println("🧹 QuestionScreen siendo descompuesta")
            isCompositionActive = false
            viewModel.resetState()
        }
    }

    // Cargar preguntas de forma segura
    LaunchedEffect(competencyId, levelId) {
        if (isCompositionActive && uiState.questions.isEmpty() && !uiState.isLoading) {
            viewModel.loadQuestions(competencyId, levelId)
        }
    }

    // No renderizar nada si no estamos activos
    if (!isCompositionActive) {
        return
    }

    // Scaffold seguro
    SafeScaffold(
        navController = navController,
        competencyId = competencyId,
        levelId = levelId,
        uiState = uiState,
        viewModel = viewModel,
        isCompositionActive = isCompositionActive
    )
}

@Composable
private fun SafeScaffold(
    navController: NavHostController,
    competencyId: Int,
    levelId: Int,
    uiState: QuestionViewModel.QuestionUiState,
    viewModel: QuestionViewModel,
    isCompositionActive: Boolean
) {
    Scaffold(
        topBar = {
            SafeTopBar(
                currentIndex = uiState.currentQuestionIndex,
                totalQuestions = uiState.questions.size,
                timeElapsed = uiState.timeElapsed,
                isLoading = uiState.isLoading,
                onBackClick = {
                    if (isCompositionActive) {
                        navController.popBackStack()
                    }
                }
            )
        }
    ) { paddingValues ->
        SafeContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onOptionSelected = { optionId ->
                if (isCompositionActive) {
                    viewModel.selectOption(optionId)
                }
            },
            onNextClicked = {
                if (!isCompositionActive) return@SafeContent

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafeTopBar(
    currentIndex: Int,
    totalQuestions: Int,
    timeElapsed: Int,
    isLoading: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                if (isLoading) "Cargando..."
                else "Pregunta ${currentIndex + 1}/$totalQuestions"
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                enabled = !isLoading
            ) {
                Icon(Icons.Default.ArrowBack, "Volver")
            }
        },
        actions = {
            Text(formatTime(timeElapsed))
        }
    )
}

@Composable
private fun SafeContent(
    uiState: QuestionViewModel.QuestionUiState,
    paddingValues: PaddingValues,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            uiState.isLoading -> {
                SafeLoadingState()
            }
            uiState.error != null -> {
                SafeErrorState(error = uiState.error)
            }
            !uiState.hasValidCurrentQuestion -> {
                SafeEmptyState()
            }
            else -> {
                SafeQuestionContent(
                    uiState = uiState,
                    onOptionSelected = onOptionSelected,
                    onNextClicked = onNextClicked
                )
            }
        }
    }
}

@Composable
private fun SafeQuestionContent(
    uiState: QuestionViewModel.QuestionUiState,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit
) {
    val currentQuestion = uiState.questions[uiState.currentQuestionIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pregunta
        key("question_${currentQuestion.id}_${uiState.currentQuestionIndex}") {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentQuestion.text,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Opciones
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            currentQuestion.options.forEach { option ->
                key("option_${option.id}_${uiState.currentQuestionIndex}") {
                    SafeOptionItem(
                        option = option,
                        isSelected = uiState.selectedOptionId == option.id,
                        onOptionSelected = onOptionSelected
                    )
                }
            }
        }

        // Botón siguiente
        Button(
            onClick = onNextClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedOptionId != null
        ) {
            Text(
                if (uiState.currentQuestionIndex == uiState.questions.size - 1) "Finalizar"
                else "Siguiente"
            )
        }
    }
}

@Composable
private fun SafeOptionItem(
    option: QuestionOption,
    isSelected: Boolean,
    onOptionSelected: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onOptionSelected(option.id) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = option.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SafeLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Cargando preguntas...")
        }
    }
}

@Composable
private fun SafeErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Error: $error", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun SafeEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No hay preguntas disponibles")
    }
}


private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}


