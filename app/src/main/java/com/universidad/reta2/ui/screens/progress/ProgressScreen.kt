package com.universidad.reta2.ui.screens.progress

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.ui.theme.*
import com.universidad.reta2.domain.models.Competence
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    //  ESTADO LOCAL PARA CONTROL ABSOLUTO DEL CICLO DE VIDA
    var isCompositionActive by remember { mutableStateOf(true) }
    val state by viewModel.state.collectAsState()
    val formattedPracticeTime by remember { derivedStateOf { viewModel.getFormattedPracticeTime() } }

    //  EFECTO PARA MANEJAR EL CICLO DE VIDA DE LA COMPOSICIÓN
    DisposableEffect(Unit) {
        println("🎬 Iniciando ProgressScreenUltraSafe")
        isCompositionActive = true
        viewModel.activate()

        onDispose {
            println("🧹 ProgressScreen siendo descompuesta")
            isCompositionActive = false
            viewModel.cleanup()
        }
    }

    //  CONTROL DE NAVEGACIÓN SEGURO
    var navigationCompetenceId by remember { mutableStateOf<Int?>(null) }

    // Effect para navegación controlada
    LaunchedEffect(navigationCompetenceId) {
        navigationCompetenceId?.let { competenceId ->
            if (isCompositionActive) {
                println("🚀 Navegando seguro a competence: $competenceId")
                try {
                    navController.navigate(Screen.CompetenceDetail.createRoute(competenceId))
                    navigationCompetenceId = null
                } catch (e: Exception) {
                    println("❌ Error en navegación: ${e.message}")
                    navigationCompetenceId = null
                }
            }
        }
    }

    //  NO RENDERIZAR NADA SI NO ESTAMOS ACTIVOS
    if (!isCompositionActive) {
        return
    }

    // CONTENIDO PRINCIPAL CON MANEJO SEGURO DE ESTADOS
    ProgressContentUltraSafe(
        state = state,
        formattedPracticeTime = formattedPracticeTime,
        onCompetencyClick = { competence ->
            if (isCompositionActive) {
                println("🖱️ Click en competencia: ${competence.id} - ${competence.name}")
                navigationCompetenceId = competence.id
            }
        },
        onRetry = {
            if (isCompositionActive) {
                viewModel.loadProgressData()
            }
        }
    )
}

@Composable
private fun ProgressContentUltraSafe(
    state: ProgressState,
    formattedPracticeTime: String,
    onCompetencyClick: (Competence) -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isLoading -> LoadingIndicatorSafe()
        state.error != null -> ErrorStateSafe(
            error = state.error,
            onRetry = onRetry
        )
        state.competences.isEmpty() -> EmptyStateSafe()
        else -> ProgressSuccessContentSafe(
            userStats = state.userStats,
            competences = state.competences,
            weeklyProgress = state.weeklyProgress,
            formattedPracticeTime = formattedPracticeTime,
            onCompetencyClick = onCompetencyClick
        )
    }
}

@Composable
private fun ProgressSuccessContentSafe(
    userStats: UserStats?,
    competences: List<Competence>,
    weeklyProgress: List<DailyProgress>,
    formattedPracticeTime: String,
    onCompetencyClick: (Competence) -> Unit
) {
    if (competences.isEmpty()) {
        EmptyStateSafe()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg100)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        userStats?.let { stats ->
            item {
                MainStatsCardSafe(
                    practiceTime = formattedPracticeTime,
                    totalQuestions = stats.totalQuestionsAnswered,
                    streakDays = stats.currentStreakDays
                )
            }
        }

        // NUEVO: Calendario semanal de actividad
        item {
            WeeklyActivityCard(
                weeklyProgress = weeklyProgress
            )
        }

        item {
            Text(
                text = "Progreso por competencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Text100,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(
            items = competences,
            key = { it.id }
        ) { competence ->
            CompetenceProgressCardSafe(
                competence = competence,
                onClick = {
                    println("🔗 Disparando click para: ${competence.name}")
                    onCompetencyClick(competence)
                }
            )
        }
    }
}

@Composable
fun MainStatsCardSafe(
    practiceTime: String,
    totalQuestions: Int,
    streakDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatsCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItemSafe(
                label = "Tiempo",
                value = practiceTime,
                modifier = Modifier.weight(1f)
            )

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            StatItemSafe(
                label = "Preguntas",
                value = totalQuestions.toString(),
                modifier = Modifier.weight(1f)
            )

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            StatItemSafe(
                label = "Racha",
                value = "$streakDays días",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItemSafe(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CompetenceProgressCardSafe(
    competence: Competence,
    onClick: () -> Unit
) {
    val nivelesCompletados = competence.levels.count { it.isCompleted }
    val nivelActual = competence.levels.firstOrNull { !it.isLocked && !it.isCompleted }
    val progress = competence.totalProgress.coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    println("🎯 Click en card: ${competence.name}")
                    onClick()
                },
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (competence.iconResId != 0) {
                    Image(
                        painter = painterResource(id = competence.iconResId),
                        contentDescription = competence.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Primary100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = competence.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = competence.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Text100
                )
                Text(
                    text = "$nivelesCompletados/${competence.levels.size} niveles completados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Text200
                )
                nivelActual?.let { nivel ->
                    Text(
                        text = "Nivel actual: ${nivel.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary100
                    )
                }
            }

            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(50.dp),
                    color = Primary100,
                    strokeWidth = 4.dp,
                    trackColor = ProgressBackground
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Text100
                )
            }
        }
    }
}

// NUEVO: Tarjeta de actividad semanal con gráfica de barras (7 días)
@Composable
fun WeeklyActivityCard(weeklyProgress: List<DailyProgress>) {
    // Construir los últimos 7 días con actividad o 0
    val today = LocalDate.now()
    val last7Days = (6 downTo 0).map { daysAgo ->
        val date = today.minusDays(daysAgo.toLong())
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        val dayName = date.dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale("es", "CO"))
            .replaceFirstChar { it.uppercase() }
            .take(2)
        val activity = weeklyProgress.firstOrNull { it.date == dateStr }
        Triple(dateStr, dayName, activity?.questionsAnswered ?: 0)
    }

    val maxQuestions = last7Days.maxOfOrNull { it.third } ?: 1
    val barColor = Primary100
    val emptyBarColor = Bg300

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Gráfica de barras
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                last7Days.forEach { (_, dayName, questions) ->
                    val fraction = if (maxQuestions > 0)
                        questions.toFloat() / maxQuestions.toFloat()
                    else 0f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Número encima de la barra (solo si > 0)
                        if (questions > 0) {
                            Text(
                                text = "$questions",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary100,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Barra
                        val barHeight = (fraction * 80f).coerceAtLeast(4f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (questions > 0) barColor else emptyBarColor)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Día
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (questions > 0) Text100 else Text200,
                            fontWeight = if (questions > 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Bg300)
            Spacer(modifier = Modifier.height(12.dp))

            // Resumen textual de la semana
            val totalWeekQuestions = last7Days.sumOf { it.third }
            val activeDays = last7Days.count { it.third > 0 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$totalWeekQuestions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary100
                    )
                    Text(
                        text = "preguntas esta semana",
                        style = MaterialTheme.typography.bodySmall,
                        color = Text200
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$activeDays/7",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary100
                    )
                    Text(
                        text = "días activos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Text200
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicatorSafe() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg100),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Primary100)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando progreso...", color = Text100)
        }
    }
}

@Composable
private fun ErrorStateSafe(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg100)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Error al cargar el progreso",
                style = MaterialTheme.typography.titleMedium,
                color = Text100
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = Text200,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary100)
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyStateSafe() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg100),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No hay datos de progreso",
                style = MaterialTheme.typography.titleMedium,
                color = Text100
            )
            Text(
                text = "Completa algunos niveles para ver tu progreso aquí",
                style = MaterialTheme.typography.bodyMedium,
                color = Text200,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}