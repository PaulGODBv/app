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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.OutlinedButton
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.ui.theme.Veredicto
import com.universidad.reta2.domain.models.QuestionOption
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import com.universidad.reta2.ui.components.*

@Composable
fun QuestionScreenUltraSafe(
    navController: NavHostController,
    competencyId: Int,
    levelId: Int,
    origin: String,
    esPractica: Boolean = false,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    // Estado local para control absoluto del ciclo de vida
    //var isCompositionActive by remember { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(origin) {
        viewModel.setOrigin(origin)
        // El modo llega por la ruta y se fija antes de cargar: de él
        // dependen la revelación inmediata y que el intento no cuente.
        viewModel.fijarModo(esPractica)
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

    // NAVEGACIÓN AL TERMINAR: a la pantalla de racha si la racha subió, y si
    // no directo a resultados.
    //
    // Depende de `listoParaResultados` y no de `isQuizCompleted`: el segundo
    // se pone fuera de la corrutina que guarda el progreso, así que llegaba
    // antes de que estuviera escrito y lo que tapaba la carrera era un
    // `delay(100)`. El primero lo pone la propia corrutina al terminar.
    LaunchedEffect(uiState.listoParaResultados) {
        if (uiState.listoParaResultados) {
            println("📊 Score final: ${uiState.score}/${uiState.questions.size}")
            println("⏱️ Tiempo total: ${uiState.timeElapsed}s")
            println("📍 Origin: $origin  ·  racha subió: ${uiState.subioLaRacha}")

            val destino = if (uiState.subioLaRacha) {
                // Solo el día que sube. Del segundo nivel en adelante la
                // racha ya está contada y celebrarla otra vez sería ruido.
                Screen.Racha.createRoute(
                    competenceId = competencyId,
                    levelId = levelId,
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    timeSpent = uiState.timeElapsed,
                    origin = origin
                )
            } else {
                Screen.Results.createRoute(
                    competenceId = competencyId,
                    levelId = levelId,
                    score = uiState.score,
                    totalQuestions = uiState.questions.size,
                    timeSpent = uiState.timeElapsed,
                    origin = origin
                )
            }

            navController.navigate(destino) {
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
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Racha",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
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
            uiState.isQuizCompleted -> {
                // Evitar mostrar el estado vacío momentáneamente antes de la navegación
                SafeLoadingState()
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

    // Al cambiar de pregunta, arriba del todo y de golpe. Sin esto la pregunta
    // nueva heredaría el desplazamiento de la anterior y nacería empezada por
    // la mitad.
    LaunchedEffect(uiState.currentQuestionIndex) {
        scrollState.scrollTo(0)
    }

    // Al revelar, bajar hasta el porqué.
    //
    // Con una pregunta larga el panel nace por debajo del pliegue, y una
    // explicación que hay que ir a buscar no la lee nadie. Se desplaza al
    // final en vez de a una posición fija porque el alto depende del enunciado
    // y del número de opciones.
    //
    // **De golpe y no animado, y esto tumbaba la aplicación.** `animateScrollTo`
    // dura unos cientos de milisegundos, y en ese rato se puede tocar
    // «Siguiente pregunta». Entonces el contenedor cambia TODOS sus hijos
    // —cada bloque va dentro de un `key(...)` que incluye el índice de la
    // pregunta— mientras la animación sigue moviendo el mismo `ScrollState`,
    // y Compose revienta al quitar los nodos con un
    // `NullPointerException` en `LayoutNode.onChildRemoved`. La pila no trae
    // ni una línea nuestra, asi que no se ve de dónde sale.
    //
    // Reproducido el 09/10/2026: responder y tocar «Siguiente» antes de un
    // segundo mata la aplicación; esperando cuatro, no. Y avanzar rápido SIN
    // responder tampoco la mata, porque sin revelar no hay animación. En
    // evaluación nunca ocurrió por lo mismo: ahí no se revela nada.
    //
    // Un salto instantáneo no se puede interrumpir, así que la carrera deja de
    // existir. Se pierde el deslizamiento suave; es un cambio que se nota
    // menos que un cierre inesperado.
    LaunchedEffect(uiState.respuestaRevelada, uiState.currentQuestionIndex) {
        if (uiState.respuestaRevelada) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

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
            // AQUI NO VA NINGUN key(...), Y ES IMPORTANTE QUE SIGA ASI.
            //
            // Cada bloque de esta columna estuvo envuelto en un `key(...)` que
            // incluia el indice de la pregunta, asi que al avanzar cambiaban
            // todas las claves y Compose tenia que DESTRUIR Y RECREAR el
            // contenido entero en lugar de actualizarlo. Esa retirada masiva
            // fallaba de dos maneras distintas, y costo entender que eran la
            // misma cosa:
            //
            //  - A veces reventaba, con un `NullPointerException` en
            //    `LayoutNode.onChildRemoved` y ni una linea nuestra en la pila.
            //  - A veces **no quitaba los nodos viejos** y la pantalla pintaba
            //    dos veces el contenido: dos barras de progreso con numeros
            //    distintos —2/5 y 1/5— y dos preguntas a la vez, con una sola
            //    barra superior y un solo boton. Eso fue lo que lo delato.
            //
            // Sin claves, Compose empareja por posicion y **actualiza en el
            // sitio**: la misma barra cambia de numero, la misma tarjeta cambia
            // de texto, y no hay ninguna retirada que pueda fallar.
            //
            // Las claves no aportaban nada: ningun bloque de aqui guarda estado
            // propio que haya que reiniciar entre preguntas.

            //  RACHA Y BARRA DE PROGRESO
            ProgressSection(
                currentIndex = uiState.currentQuestionIndex,
                totalQuestions = uiState.questions.size,
                streak = uiState.streak
            )

            //  CONTEXTO DE LA PREGUNTA CON PREVIEW
            if (currentQuestion.readingText.isNotEmpty() || currentQuestion.contextImage != null) {
                QuestionContextCard(
                    readingText = currentQuestion.readingText,
                    // La URL del panel manda; el nombre de drawable es el respaldo
                    // del contenido de arranque.
                    contextImage = currentQuestion.contextImageUrl?.takeIf { it.isNotBlank() }
                        ?: currentQuestion.contextImage?.takeIf { it.isNotBlank() },
                    contextImageAlt = currentQuestion.contextImageAlt?.takeIf { it.isNotBlank() },
                    onShowTextModal = onShowTextModal,
                    onShowImageModal = onShowImageModal
                )
            }

            // PREGUNTA
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

            // OPCIONES
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentQuestion.options.forEach { option ->
                    SafeOptionItem(
                        option = option,
                        isSelected = uiState.selectedOptionId == option.id,
                        revelada = uiState.respuestaRevelada,
                        esLaCorrecta = option.id == currentQuestion.correctOptionId,
                        onOptionSelected = onOptionSelected
                    )
                }
            }

            // Retroalimentación inmediata: solo en práctica y solo una vez
            // respondida. Va debajo de las opciones y no en una hoja ni en una
            // banda superior a propósito: así la pregunta, lo que elegiste y la
            // correcta siguen a la vista mientras lees el porqué, que es lo
            // que hace que la corrección se fije.
            if (uiState.respuestaRevelada) {
                Spacer(modifier = Modifier.height(16.dp))
                PorQueDeLaRespuesta(
                    acerto = uiState.selectedOptionId == currentQuestion.correctOptionId,
                    explicacion = currentQuestion.explanation
                )
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
                // `puedeAvanzar` bloquea el botón un instante tras revelar la
                // respuesta en práctica: revelar y avanzar en el mismo fotograma
                // cerraba la aplicación (ver MILIS_ANTES_DE_AVANZAR).
                enabled = uiState.selectedOptionId != null && uiState.puedeAvanzar,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                val esUltima = uiState.currentQuestionIndex == uiState.questions.size - 1
                Icon(
                    imageVector = if (esUltima) {
                        Icons.Filled.Flag
                    } else {
                        Icons.AutoMirrored.Filled.ArrowForward
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (esUltima) "Finalizar quiz" else "Siguiente pregunta",
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
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
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






/**
 * El porqué de la respuesta, debajo de las opciones ya teñidas.
 *
 * Solo aparece en práctica. En evaluación la explicación se guarda para el
 * repaso de la pantalla de resultados: enseñarla en cada pregunta convertiría
 * el examen en un tutorial.
 */
@Composable
private fun PorQueDeLaRespuesta(acerto: Boolean, explicacion: String) {
    val veredicto = Veredicto.colores
    val acento = if (acerto) veredicto.acierto else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (acerto) veredicto.aciertoContenedor
                             else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (acerto) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = acento,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (acerto) "Correcto" else "No era esa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = acento
                )
            }

            // El contenido de arranque puede no traer explicación; las del
            // panel sí la traen.
            if (explicacion.isNotBlank()) {
                Text(
                    text = explicacion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                )
            }
        }
    }
}

@Composable
private fun SafeOptionItem(
    option: QuestionOption,
    isSelected: Boolean,
    revelada: Boolean = false,
    esLaCorrecta: Boolean = false,
    onOptionSelected: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Tras revelar hay tres papeles: la correcta, la elegida si no lo era, y
    // las demás, que se apagan para que no compitan por la atención.
    val marcaCorrecta = revelada && esLaCorrecta
    val marcaFallo = revelada && isSelected && !esLaCorrecta
    val apagada = revelada && !marcaCorrecta && !marcaFallo

    // Verde para acierto y rojo para fallo. El azul se queda para la selección
    // antes de revelar, que es estado y no veredicto: mezclar los dos era lo
    // que hacía falta distinguir.
    val veredicto = Veredicto.colores
    val acento = when {
        marcaCorrecta -> veredicto.acierto
        marcaFallo -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                // Revelada deja de aceptar toques: la respuesta ya está dada.
                enabled = !revelada,
                interactionSource = interactionSource,
                indication = null, // 🔒 MANTENER null PARA SEGURIDAD
                onClick = { onOptionSelected(option.id) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                marcaCorrecta -> veredicto.aciertoContenedor
                marcaFallo -> MaterialTheme.colorScheme.errorContainer
                apagada -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant // ✅ CAMBIO SEGURO
            }
        ),
        elevation = CardDefaults.cardElevation( // ✅ AGREGAR ELEVACIÓN SEGURA
            defaultElevation = if (isSelected && !apagada) 8.dp else 4.dp
        ),
        border = BorderStroke(
            width = if (isSelected || marcaCorrecta) 2.dp else 1.dp, // ✅ CAMBIO SEGURO
            color = acento
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
            // Revelada, el círculo de selección deja paso al veredicto.
            if (marcaCorrecta || marcaFallo) {
                Icon(
                    imageVector = if (marcaCorrecta) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = if (marcaCorrecta) "Respuesta correcta" else "Tu respuesta, incorrecta",
                    tint = acento,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // RadioButton con mejor diseño
                RadioButton(
                    selected = isSelected,
                    onClick = null, // 🔒 MANTENER null - EL CLICK ESTÁ EN EL CARD
                    colors = RadioButtonDefaults.colors( // ✅ CAMBIO SEGURO
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

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
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
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


