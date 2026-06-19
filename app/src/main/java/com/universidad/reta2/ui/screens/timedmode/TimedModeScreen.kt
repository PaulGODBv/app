package com.universidad.reta2.ui.screens.timedmode

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.verticalScroll
import com.universidad.reta2.ui.components.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.models.QuestionOption
import com.universidad.reta2.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimedModeScreen(
    navController: NavController,
    viewModel: TimedModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Interceptar botón atrás del sistema en PLAYING
    val onBackPressed: () -> Unit = {
        when (uiState.step) {
            TimedModeStep.PLAYING -> {
                viewModel.resetMode()
            }
            TimedModeStep.FINISHED -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            }
            TimedModeStep.SETUP_SOURCE -> {
                navController.popBackStack()
            }
            else -> viewModel.goBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.step) {
                            TimedModeStep.SETUP_SOURCE    -> "Modo contrarreloj"
                            TimedModeStep.SETUP_COMPETENCE -> "Elegir competencia"
                            TimedModeStep.SETUP_LEVEL     -> "Elegir nivel"
                            TimedModeStep.SETUP_TIME      -> "Configurar tiempo"
                            TimedModeStep.PLAYING         -> "⏱ Contrarreloj"
                            TimedModeStep.FINISHED        -> "Resultado"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (uiState.step != TimedModeStep.PLAYING) {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.step) {
                TimedModeStep.SETUP_SOURCE -> SetupSourceStep(
                    onSelectSource = { viewModel.selectSource(it) }
                )
                TimedModeStep.SETUP_COMPETENCE -> SetupCompetenceStep(
                    competences = uiState.competences,
                    onSelectCompetence = { viewModel.selectCompetence(it) }
                )
                TimedModeStep.SETUP_LEVEL -> SetupLevelStep(
                    competence = uiState.selectedCompetence,
                    onSelectLevel = { viewModel.selectLevel(it) }
                )
                TimedModeStep.SETUP_TIME -> SetupTimeStep(
                    selectedMinutes = uiState.selectedMinutes,
                    questionSource = uiState.questionSource,
                    selectedCompetence = uiState.selectedCompetence,
                    selectedLevelId = uiState.selectedLevelId,
                    isLoading = uiState.isLoadingQuestions,
                    error = uiState.error,
                    onMinutesChange = { viewModel.selectMinutes(it) },
                    onStart = { viewModel.startTimedMode() }
                )
                TimedModeStep.PLAYING -> PlayingStep(
                    uiState = uiState,
                    onSelectOption = { viewModel.selectOption(it) }
                )
                TimedModeStep.FINISHED -> FinishedStep(
                    totalAnswered = uiState.totalAnswered,
                    totalCorrect = uiState.totalCorrect,
                    minutes = uiState.selectedMinutes,
                    onPlayAgain = { viewModel.resetMode() },
                    onGoHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}

// ── PASO 1: ELEGIR FUENTE ──────────────────────────────────────

@Composable
private fun SetupSourceStep(
    onSelectSource: (QuestionSource) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⏱",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Modo contrarreloj",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Responde el mayor número de preguntas antes de que se acabe el tiempo",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "¿De dónde vienen las preguntas?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        SourceOptionCard(
            emoji = "🌐",
            title = "Todas las competencias",
            description = "Preguntas mezcladas de todos los temas y niveles",
            onClick = { onSelectSource(QuestionSource.ALL) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SourceOptionCard(
            emoji = "📚",
            title = "Una competencia",
            description = "Elige una competencia y practica todos sus niveles",
            onClick = { onSelectSource(QuestionSource.COMPETENCE) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SourceOptionCard(
            emoji = "🎯",
            title = "Nivel específico",
            description = "Elige competencia y nivel exacto a practicar",
            onClick = { onSelectSource(QuestionSource.LEVEL) }
        )
    }
}

@Composable
private fun SourceOptionCard(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    // Rotar la flecha para que apunte a la derecha
                    .then(Modifier),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── PASO 2A: ELEGIR COMPETENCIA ────────────────────────────────

@Composable
private fun SetupCompetenceStep(
    competences: List<Competence>,
    onSelectCompetence: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Selecciona una competencia",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (competences.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            competences.forEach { competence ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectCompetence(competence.id) }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = competence.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${competence.levels.size} niveles disponibles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── PASO 2B: ELEGIR NIVEL ──────────────────────────────────────

@Composable
private fun SetupLevelStep(
    competence: Competence?,
    onSelectLevel: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Selecciona un nivel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        competence?.let {
            Text(
                text = it.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        competence?.levels?.forEach { level ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectLevel(level.id) }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (level.isLocked)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (level.isLocked) 0.dp else 2.dp
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = level.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (level.isLocked)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = level.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (level.isLocked) {
                        Text(
                            text = "🔒",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

// ── PASO 3: ELEGIR TIEMPO ──────────────────────────────────────

@Composable
private fun SetupTimeStep(
    selectedMinutes: Int,
    questionSource: QuestionSource,
    selectedCompetence: Competence?,
    selectedLevelId: Int?,
    isLoading: Boolean,
    error: String?,
    onMinutesChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Resumen de configuración
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = when (questionSource) {
                        QuestionSource.ALL        -> "Todas las competencias"
                        QuestionSource.COMPETENCE -> selectedCompetence?.name ?: "Competencia seleccionada"
                        QuestionSource.LEVEL      -> {
                            val levelName = selectedCompetence?.levels
                                ?.firstOrNull { it.id == selectedLevelId }?.name
                            "${selectedCompetence?.name} · $levelName"
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "¿Cuánto tiempo quieres?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Display del tiempo seleccionado
        Text(
            text = "$selectedMinutes min",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Slider de 3 a 9 minutos (requerimiento v2)
        Slider(
            value = selectedMinutes.toFloat(),
            onValueChange = { onMinutesChange(it.toInt()) },
            valueRange = 3f..9f,
            steps = 5, // 3,4,5,6,7,8,9 → 5 pasos intermedios
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "3 min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "9 min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onStart,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "▶ Iniciar ($selectedMinutes min)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── PLAYING ───────────────────────────────────────────────────

@Composable
private fun PlayingStep(
    uiState: TimedModeUiState,
    onSelectOption: (Int) -> Unit
) {
    val currentQuestion = uiState.currentQuestion

    // Modales y estados de contexto (texto / imagen)
    var showTextModal by remember { mutableStateOf(false) }
    var showImageModal by remember { mutableStateOf(false) }
    var currentImageResource by remember { mutableStateOf("") }
    var currentReadingText by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Timer bar
        TimerBar(
            timeRemainingSeconds = uiState.timeRemainingSeconds,
            totalSeconds = uiState.selectedMinutes * 60
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats en tiempo real
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip(label = "Respondidas", value = "${uiState.totalAnswered}")
                StatChip(label = "Correctas", value = "${uiState.totalCorrect}")
                StatChip(
                    label = "Precisión",
                    value = if (uiState.totalAnswered > 0)
                        "${(uiState.totalCorrect * 100) / uiState.totalAnswered}%"
                    else "—"
                )
            }

            if (currentQuestion == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // CONTEXTO (preview) si existe
                if (currentQuestion.readingText.isNotEmpty() || currentQuestion.contextImage != null) {
                    QuestionContextCard(
                        readingText = currentQuestion.readingText,
                        contextImage = currentQuestion.contextImage,
                        onShowTextModal = {
                            if (currentQuestion.readingText.isNotEmpty()) {
                                currentReadingText = currentQuestion.readingText
                                showTextModal = true
                            }
                        },
                        onShowImageModal = { imageName ->
                            currentImageResource = imageName
                            showImageModal = true
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                // Pregunta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Pregunta ${uiState.currentQuestionIndex + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = currentQuestion.text,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.titleMedium.lineHeight * 1.2
                        )
                    }
                }

                // Opciones
                currentQuestion.options.forEach { option ->
                    TimedOptionItem(
                        option = option,
                        isSelected = uiState.selectedOptionId == option.id,
                        showFeedback = uiState.showFeedback,
                        isCorrect = option.id == currentQuestion.correctOptionId,
                        // evitar doble selección: se protege en onClick interno
                        onSelect = { if (!uiState.showFeedback) onSelectOption(option.id) }
                    )
                }
            }
        }
    }

    // Modales fuera del content scroll
    if (showTextModal && currentReadingText.isNotEmpty()) {
        TextContextModal(
            readingText = currentReadingText,
            onDismiss = { showTextModal = false }
        )
    }

    if (showImageModal && currentImageResource.isNotEmpty()) {
        ImageContextModal(
            imageName = currentImageResource,
            scale = scale,
            offset = offset,
            onScaleChange = { newScale -> scale = newScale },
            onOffsetChange = { newOffset -> offset = newOffset },
            onDismiss = {
                showImageModal = false
                scale = 1f
                offset = Offset.Zero
            }
        )
    }
}

@Composable
private fun TimerBar(
    timeRemainingSeconds: Int,
    totalSeconds: Int
) {
    val progress = if (totalSeconds > 0)
        timeRemainingSeconds.toFloat() / totalSeconds.toFloat()
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "timer"
    )

    val timerColor by animateColorAsState(
        targetValue = when {
            progress > 0.5f -> MaterialTheme.colorScheme.primary
            progress > 0.25f -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        },
        label = "timerColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏱ Tiempo restante",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatSeconds(timeRemainingSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = timerColor
            )
        }
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TimedOptionItem(
    option: QuestionOption,
    isSelected: Boolean,
    showFeedback: Boolean,
    isCorrect: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = when {
        showFeedback && isCorrect  -> MaterialTheme.colorScheme.primaryContainer
        showFeedback && isSelected -> MaterialTheme.colorScheme.errorContainer
        isSelected                 -> MaterialTheme.colorScheme.primaryContainer
        else                       -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        showFeedback && isCorrect  -> MaterialTheme.colorScheme.primary
        showFeedback && isSelected -> MaterialTheme.colorScheme.error
        isSelected                 -> MaterialTheme.colorScheme.primary
        else                       -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(
            width = if (isSelected || (showFeedback && isCorrect)) 2.dp else 1.dp,
            color = borderColor
        ),
        shape = MaterialTheme.shapes.large
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
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = option.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Feedback visual
            if (showFeedback && isCorrect) {
                Text("✓", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary)
            } else if (showFeedback && isSelected && !isCorrect) {
                Text("✗", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ── FINISHED ───────────────────────────────────────────────────

@Composable
private fun FinishedStep(
    totalAnswered: Int,
    totalCorrect: Int,
    minutes: Int,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val percentage = if (totalAnswered > 0) (totalCorrect * 100) / totalAnswered else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                percentage >= 80 -> "🏆"
                percentage >= 60 -> "🎯"
                percentage >= 40 -> "💪"
                else             -> "📚"
            },
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Tiempo agotado!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tarjetas de estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultStatCard(
                modifier = Modifier.weight(1f),
                label = "Respondidas",
                value = "$totalAnswered"
            )
            ResultStatCard(
                modifier = Modifier.weight(1f),
                label = "Correctas",
                value = "$totalCorrect"
            )
            ResultStatCard(
                modifier = Modifier.weight(1f),
                label = "Precisión",
                value = "$percentage%"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    percentage >= 80 -> MaterialTheme.colorScheme.primaryContainer
                    percentage >= 60 -> MaterialTheme.colorScheme.secondaryContainer
                    else             -> MaterialTheme.colorScheme.errorContainer
                }
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = when {
                    percentage >= 80 -> "¡Excelente desempeño en $minutes minutos! 🎉"
                    percentage >= 60 -> "¡Buen trabajo! Sigue practicando 👍"
                    else             -> "Practica más para mejorar tu velocidad 💪"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
                color = when {
                    percentage >= 80 -> MaterialTheme.colorScheme.onPrimaryContainer
                    percentage >= 60 -> MaterialTheme.colorScheme.onSecondaryContainer
                    else             -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Jugar de nuevo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Volver al inicio",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ResultStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── UTILS ──────────────────────────────────────────────────────

private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
