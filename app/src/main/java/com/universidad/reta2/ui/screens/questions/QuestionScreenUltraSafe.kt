package com.universidad.reta2.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.OutlinedButton
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.domain.models.QuestionOption
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun QuestionScreenUltraSafe(
    navController: NavHostController,
    competencyId: Int,
    levelId: Int,
    origin: String,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    // Estado local para control absoluto del ciclo de vida
    //var isCompositionActive by remember { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(origin) {
        viewModel.setOrigin(origin)
    }

    // 🔥 ESTADOS PARA MODALES
    var showTextModal by remember { mutableStateOf(false) }
    var showImageModal by remember { mutableStateOf(false) }
    var currentImageResource by remember { mutableStateOf("") }
    var currentReadingText by remember { mutableStateOf("") }

    // Estados para zoom de imagen
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Efecto para manejar el ciclo de vida de la composición


    // Cargar preguntas de forma segura
    LaunchedEffect(competencyId, levelId) {
        // Simplemente llama a loadQuestions.
        // El ViewModel (que ahora es más inteligente)
        // decidirá si necesita cargar los datos o no.
        viewModel.loadQuestions(competencyId, levelId)
    }

    // No renderizar nada si no estamos activos


    // ACTUALIZAR TEXTO PARA MODAL CUANDO CAMBIA LA PREGUNTA
    LaunchedEffect(uiState.currentQuestionIndex) {
        val currentQuestion = uiState.currentQuestion
        if (currentQuestion != null && currentQuestion.readingText.isNotEmpty()) {
            currentReadingText = currentQuestion.readingText
        }
    }

    //  LAUNCHED EFFECT PARA NAVEGACIÓN A RESULTS
    LaunchedEffect(uiState.isQuizCompleted) {
        if (uiState.isQuizCompleted) {
            println("🎯 Navegando a Results desde QuestionScreenUltraSafe")
            println("📊 Score final: ${uiState.score}/${uiState.questions.size}")
            println("⏱️ Tiempo total: ${uiState.timeElapsed}s")
            println("📍 Origin: $origin")

            // Pequeño delay para asegurar procesamiento completo
            delay(100)

            navController.navigate(
                Screen.Results.createRoute(
                    competenceId = competencyId,
                    levelId = levelId,
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    timeSpent = uiState.timeElapsed,
                    origin = origin
                )
            ) {
                popUpTo(Screen.Questions.route) { inclusive = true }
            }
        }
    }

    // Scaffold seguro con modales
    Box(modifier = Modifier.fillMaxSize()) {
        SafeScaffold(
            navController = navController,
            uiState = uiState,
            viewModel = viewModel,
            //isCompositionActive = isCompositionActive,
            onShowTextModal = {
                uiState.currentQuestion?.readingText?.let { text ->
                    if (text.isNotEmpty()) {
                        currentReadingText = text
                        showTextModal = true
                    }
                }
            },
            onShowImageModal = { imageName ->
                currentImageResource = imageName
                showImageModal = true
            }

        )

        //  MODALES
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
}

@Composable
private fun SafeScaffold(
    navController: NavHostController,
    uiState: QuestionViewModel.QuestionUiState,
    viewModel: QuestionViewModel,
    onShowTextModal: () -> Unit,
    onShowImageModal: (String) -> Unit,
) {

    Scaffold(
        topBar = {
            SafeTopBar(
                currentIndex = uiState.currentQuestionIndex,
                totalQuestions = uiState.questions.size,
                timeElapsed = uiState.timeElapsed,
                isLoading = uiState.isLoading,
                streak = uiState.streak,
                onBackClick = {
                        navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        SafeContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onOptionSelected = { optionId ->
                    viewModel.selectOption(optionId)

            },
            onNextClicked = {
                    viewModel.nextQuestion()
            },
            onShowTextModal = onShowTextModal,
            onShowImageModal = onShowImageModal
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
    streak: Int,
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
            // CONTENEDOR PARA RACHA Y TIEMPO
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //  INDICADOR DE RACHA
                if (streak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔥",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$streak",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // TEMPORIZADOR
                Text(
                    text = formatTime(timeElapsed),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
private fun SafeContent(
    uiState: QuestionViewModel.QuestionUiState,
    paddingValues: PaddingValues,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit,
    onShowTextModal: () -> Unit, //
    onShowImageModal: (String) -> Unit //
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
                    onNextClicked = onNextClicked,
                    onShowTextModal = onShowTextModal,
                    onShowImageModal = onShowImageModal
                )
            }
        }
    }
}


@Composable
private fun SafeQuestionContent(
    uiState: QuestionViewModel.QuestionUiState,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit,
    onShowTextModal: () -> Unit,
    onShowImageModal: (String) -> Unit
) {
    val currentQuestion = uiState.currentQuestion ?: return
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // CONTENIDO SCROLLEABLE
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            //  RACHA Y BARRA DE PROGRESO
            key("progress_${uiState.currentQuestionIndex}") {
                ProgressSection(
                    currentIndex = uiState.currentQuestionIndex,
                    totalQuestions = uiState.questions.size,
                    streak = uiState.streak
                )
            }

            //  CONTEXTO DE LA PREGUNTA CON PREVIEW
            if (currentQuestion.readingText.isNotEmpty() || currentQuestion.contextImage != null) {
                key("context_${currentQuestion.id}_${uiState.currentQuestionIndex}") {
                    QuestionContextCard(
                        readingText = currentQuestion.readingText,
                        contextImage = currentQuestion.contextImage,
                        onShowTextModal = onShowTextModal,
                        onShowImageModal = onShowImageModal
                    )
                }
            }

            // PREGUNTA
            key("question_${currentQuestion.id}_${uiState.currentQuestionIndex}") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Pregunta:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = currentQuestion.text,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.1
                        )
                    }
                }
            }

            // OPCIONES
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
        }

        // BOTÓN FIJO EN LA PARTE INFERIOR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Button(
                onClick = onNextClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.selectedOptionId != null,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = if (uiState.currentQuestionIndex == uiState.questions.size - 1) {
                        "🎯 Finalizar Quiz"
                    } else {
                        "➡️ Siguiente Pregunta"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(
    currentIndex: Int,
    totalQuestions: Int,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (streak > 0) {
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
            }

            // 📊 BARRA DE PROGRESO
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Indicador de progreso textual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progreso:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currentIndex + 1}/$totalQuestions",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Barra de progreso lineal
                LinearProgressIndicator(
                    progress = if (totalQuestions > 0) {
                        (currentIndex + 1).toFloat() / totalQuestions
                    } else {
                        0f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@Composable
private fun QuestionContextCard(
    readingText: String,
    contextImage: String?,
    onShowTextModal: () -> Unit,
    onShowImageModal: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // TÍTULO DEL CONTEXTO
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Contexto:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CONTENIDO DEL CONTEXTO CON PREVIEW
            when {

                readingText.isNotEmpty() && contextImage == null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        val textPreview = getTextPreview(readingText, maxLines = 2)
                        Text(
                            text = textPreview,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // BOTÓN PARA VER TEXTO COMPLETO
                        OutlinedButton(
                            onClick = onShowTextModal,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ver texto completo")
                        }
                    }
                }

                // ✅ CASO 2: SOLO IMAGEN - CON PREVIEW Y BOTÓN
                contextImage != null && readingText.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🖼️ Imagen de referencia:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )

                        // PREVIEW PEQUEÑA DE LA IMAGEN
                        LoadContextImage(
                            imageName = contextImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        // BOTÓN PARA VER IMAGEN COMPLETA
                        OutlinedButton(
                            onClick = { onShowImageModal(contextImage) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ampliar imagen")
                        }
                    }
                }

                // ✅ CASO 3: TEXTO E IMAGEN - CON PREVIEWS Y BOTONES
                readingText.isNotEmpty() && contextImage != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // PREVIEW DEL TEXTO
                        val textPreview = getTextPreview(readingText, maxLines = 2)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = textPreview,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            OutlinedButton(
                                onClick = onShowTextModal,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Ver texto completo")
                            }
                        }

                        Divider(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        // PREVIEW DE LA IMAGEN
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🖼️ Imagen de referencia:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )

                            LoadContextImage(
                                imageName = contextImage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )

                            OutlinedButton(
                                onClick = { onShowImageModal(contextImage) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Ampliar imagen")
                            }
                        }
                    }
                }

                //  CASO 4: SIN CONTEXTO
                else -> {
                    // No mostrar nada si no hay contexto
                }
            }
        }
    }
}

private fun getTextPreview(fullText: String, maxLines: Int = 2): String {
    if (fullText.length <= 150) return fullText

    val lines = fullText.lineSequence().take(maxLines).toList()
    return if (lines.size < maxLines) {
        fullText.take(150) + "..."
    } else {
        lines.joinToString("\n").take(150) + "..."
    }
}


@Composable
private fun TextContextModal(
    readingText: String,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // Fondo semi-transparente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.8f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header del modal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 Contexto Completo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Divider()

                    // Contenido scrolleable del texto
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = readingText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                            textAlign = TextAlign.Justify
                        )
                    }

                    // Botón de cerrar en la parte inferior
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ImageContextModal(
    imageName: String,
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imageResourceId = remember(imageName) {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // Fondo semi-transparente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header del modal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🖼️ Imagen de Contexto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Divider()

                    // Imagen con zoom
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                            onScaleChange((scale * zoomChange).coerceIn(0.5f, 5f))
                            onOffsetChange(offset + panChange)
                        }

                        if (imageResourceId != 0) {
                            Image(
                                painter = painterResource(id = imageResourceId),
                                contentDescription = "Imagen de contexto: $imageName",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                                    .transformable(state = transformableState)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // Placeholder si la imagen no existe
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "🖼️",
                                        style = MaterialTheme.typography.displayLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Imagen no encontrada",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Nombre: $imageName",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Botón de cerrar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun LoadContextImage(
    imageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Obtener el resource ID de la imagen
    val imageResourceId = remember(imageName) {
        getImageResourceId(context, imageName)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (imageResourceId != 0) {
            // ✅ IMAGEN ENCONTRADA - MOSTRARLA
            Image(
                painter = painterResource(id = imageResourceId),
                contentDescription = "Imagen de contexto: $imageName",
                modifier = modifier
                    .clip(MaterialTheme.shapes.medium)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentScale = ContentScale.Fit
            )

            // Texto descriptivo pequeño
            Text(
                text = imageName.replace("_", " ").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                fontStyle = FontStyle.Italic
            )
        } else {
            // ❌ IMAGEN NO ENCONTRADA - MOSTRAR PLACEHOLDER
            Box(
                modifier = modifier
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🖼️",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Imagen no encontrada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Nombre: $imageName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Verifica que exista en res/drawable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun getImageResourceId(context: android.content.Context, imageName: String): Int {
    return try {
        // Buscar el resource ID por nombre
        val resourceId = context.resources.getIdentifier(
            imageName, // "imagen_sismos"
            "drawable",
            context.packageName
        )

        println("🖼️ Buscando imagen: $imageName -> Resource ID: $resourceId")
        resourceId
    } catch (e: Exception) {
        println("❌ Error buscando imagen $imageName: ${e.message}")
        0
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
                indication = null, // 🔒 MANTENER null PARA SEGURIDAD
                onClick = { onOptionSelected(option.id) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant // ✅ CAMBIO SEGURO
            }
        ),
        elevation = CardDefaults.cardElevation( // ✅ AGREGAR ELEVACIÓN SEGURA
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp, // ✅ CAMBIO SEGURO
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // ✅ CAMBIO SEGURO
            }
        ),
        shape = MaterialTheme.shapes.large // ✅ CAMBIO SEGURO
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp) // ✅ AUMENTAR PADDING SEGURO
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // RadioButton con mejor diseño
            RadioButton(
                selected = isSelected,
                onClick = null, // 🔒 MANTENER null - EL CLICK ESTÁ EN EL CARD
                colors = RadioButtonDefaults.colors( // ✅ CAMBIO SEGURO
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.width(16.dp)) // ✅ AUMENTAR ESPACIO SEGURO

            Text(
                text = option.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface // ✅ CAMBIO SEGURO
                }
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
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Cargando preguntas...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SafeErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Error al cargar",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
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


