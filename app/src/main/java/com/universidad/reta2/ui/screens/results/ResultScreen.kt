package com.universidad.reta2.ui.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.domain.LevelRules
import com.universidad.reta2.ui.navigation.Screen

/**
 * Porcentaje mínimo para dar un nivel por superado.
 *
 * Alias local de [LevelRules.PASSING_PERCENTAGE]: el valor vive en una sola
 * constante de dominio, que es la que aplica también `ProgressRepository` al
 * consolidar el nivel y desbloquear el siguiente.
 */
private const val PASSING_PERCENTAGE = LevelRules.PASSING_PERCENTAGE

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

    val competency by viewModel.competenceState.collectAsState()
    val level by viewModel.levelState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Sin esta carga, la tarjeta de competencia y nivel se quedaba con los
    // textos de reserva ("Competencia", "Nivel").
    LaunchedEffect(competencyId, levelId) {
        viewModel.loadData(competencyId, levelId)
    }

    val nextLevelId = remember(competency, levelId) {
        competency?.levels?.let { levels ->
            val currentIndex = levels.indexOfFirst { it.id == levelId }

            if (currentIndex >= 0 && currentIndex < levels.size - 1) {
                levels[currentIndex + 1].id
            } else {
                null
            }
        }
    }

    val percentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
    val passed = percentage >= PASSING_PERCENTAGE
    val incorrect = (totalQuestions - score).coerceAtLeast(0)

    // Aciertos que faltan para alcanzar el umbral, para que el mensaje sea concreto
    val missingForPass = remember(score, totalQuestions) {
        val needed = kotlin.math.ceil(totalQuestions * PASSING_PERCENTAGE / 100.0).toInt()
        (needed - score).coerceAtLeast(0)
    }

    // Sincronizar con el servidor al mostrar resultados
    LaunchedEffect(Unit) {
        viewModel.syncProgress()
    }

    // Feedback de sincronización via Snackbar
    LaunchedEffect(syncState) {
        when (val state = syncState) {
            is SyncUiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
            }
            is SyncUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = "Reintentar",
                    duration = SnackbarDuration.Long
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.syncProgress()
                    }
                }
            }
            else -> Unit
        }
    }

    val buttonText = when (origin) {
        "progress" -> "Volver a progreso"
        else -> "Volver a competencias"
    }

    val accent = when {
        percentage >= PASSING_PERCENTAGE -> MaterialTheme.colorScheme.primary
        percentage >= 60 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido scrolleable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ---- Competencia y nivel ----
                // IntrinsicSize.Min iguala la altura de las dos etiquetas: los
                // nombres largos ocupan las lineas que necesiten sin que una
                // etiqueta quede mas alta que la otra.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    ContextChip(
                        icon = Icons.Filled.School,
                        text = competency?.name ?: "Competencia",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    ContextChip(
                        icon = Icons.Filled.Layers,
                        text = level?.name ?: "Nivel",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                // ---- Anillo de porcentaje ----
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(176.dp)
                ) {
                    CircularProgressIndicator(
                        progress = percentage / 100f,
                        modifier = Modifier.fillMaxSize(),
                        color = accent,
                        strokeWidth = 12.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Text(
                            text = "$score de $totalQuestions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ---- Veredicto ----
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                percentage >= PASSING_PERCENTAGE -> Icons.Filled.EmojiEvents
                                percentage >= 60 -> Icons.Filled.ThumbUp
                                else -> Icons.Filled.TrendingUp
                            },
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (passed) "¡Nivel superado!" else "Nivel no superado",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = when {
                            percentage >= PASSING_PERCENTAGE -> "Excelente trabajo, sigue así."
                            percentage >= 60 -> "Buen intento, ya estás cerca."
                            else -> "Repasa el contenido y vuelve a intentarlo."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // ---- Detalle de la partida ----
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ResultStat(
                            icon = Icons.Filled.CheckCircle,
                            value = score.toString(),
                            label = "Correctas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        VerticalDivider(
                            modifier = Modifier.height(48.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        ResultStat(
                            icon = Icons.Filled.Cancel,
                            value = incorrect.toString(),
                            label = "Incorrectas",
                            tint = MaterialTheme.colorScheme.error
                        )
                        VerticalDivider(
                            modifier = Modifier.height(48.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        ResultStat(
                            icon = Icons.Filled.Timer,
                            value = formatTime(timeSpent),
                            label = "Tiempo",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // ---- Estado del desbloqueo ----
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (passed) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (passed) Icons.Filled.LockOpen else Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = if (passed) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                        Text(
                            text = when {
                                passed && nextLevelId != null ->
                                    "Has desbloqueado el siguiente nivel de esta competencia."
                                passed ->
                                    "Completaste el último nivel de esta competencia."
                                missingForPass == 1 ->
                                    "Te falta 1 acierto para alcanzar el $PASSING_PERCENTAGE% y desbloquear el siguiente nivel."
                                else ->
                                    "Te faltan $missingForPass aciertos para alcanzar el $PASSING_PERCENTAGE% y desbloquear el siguiente nivel."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (passed) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ---- Acciones ----
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val canGoToNext = passed && nextLevelId != null

                Button(
                    onClick = {
                        val targetRoute = if (canGoToNext) {
                            println("Avanzando al siguiente nivel: $nextLevelId")
                            Screen.Questions.createRoute(competencyId, nextLevelId!!, origin)
                        } else {
                            println("Reintentando nivel: $levelId")
                            Screen.Questions.createRoute(competencyId, levelId, origin)
                        }

                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Results.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (canGoToNext) {
                            Icons.AutoMirrored.Filled.ArrowForward
                        } else {
                            Icons.Filled.Refresh
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (canGoToNext) "Siguiente nivel" else "Reintentar nivel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = {
                        val targetRoute =
                            if (origin == "home") Screen.Home.route else Screen.Competencies.route

                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = true // Recupera el estado limpio del tab
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = buttonText)
                }
            }
        }
    }
}

/** Etiqueta con el nombre de la competencia o del nivel. */
@Composable
private fun ContextChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            // Sin tope de lineas: el nombre del nivel se lee entero.
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Dato suelto del resumen: icono, cifra y etiqueta. */
@Composable
private fun ResultStat(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
