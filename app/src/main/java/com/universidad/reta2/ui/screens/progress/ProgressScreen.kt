package com.universidad.reta2.ui.screens.progress

import com.universidad.reta2.ui.components.shimmerEffect
import com.universidad.reta2.ui.components.esperaVisible
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.data.remote.dto.RankingResponse
import com.universidad.reta2.data.remote.dto.RankingEntryDto
import com.universidad.reta2.ui.navigation.Screen
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
    var isCompositionActive by remember { mutableStateOf(true) }
    val state by viewModel.state.collectAsState()
    // Con `derivedStateOf` el tiempo se quedaba clavado en «0 seg»: la función
    // lee el StateFlow del ViewModel, que no es estado de Compose, así que
    // nunca había nada que invalidara el valor guardado. La llave son las
    // estadísticas ya recogidas.
    val formattedPracticeTime = remember(state.userStats) { viewModel.getFormattedPracticeTime() }

    DisposableEffect(Unit) {
        isCompositionActive = true
        viewModel.activate()
        onDispose {
            isCompositionActive = false
            viewModel.cleanup()
        }
    }

    var navigationCompetenceId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(navigationCompetenceId) {
        navigationCompetenceId?.let { competenceId ->
            if (isCompositionActive) {
                try {
                    navController.navigate(Screen.CompetenceDetail.createRoute(competenceId))
                    navigationCompetenceId = null
                } catch (e: Exception) {
                    navigationCompetenceId = null
                }
            }
        }
    }

    if (!isCompositionActive) return

    ProgressContentUltraSafe(
        state = state,
        formattedPracticeTime = formattedPracticeTime,
        onCompetencyClick = { competence ->
            if (isCompositionActive) {
                navigationCompetenceId = competence.id
            }
        },
        onRetry = {
            if (isCompositionActive) {
                viewModel.loadProgressData()
            }
        },
        onSync = {
            if (isCompositionActive) {
                viewModel.syncNow()
            }
        }
    )
}

@Composable
private fun ProgressContentUltraSafe(
    state: ProgressState,
    formattedPracticeTime: String,
    onCompetencyClick: (Competence) -> Unit,
    onRetry: () -> Unit,
    onSync: () -> Unit
) {
    // El esqueleto es una promesa de espera: solo se ensena si la carga se
    // alarga de verdad. Lo de Room llega en milisegundos y ensenarlo produciria
    // un parpadeo en cada cambio de pestana.
    val cargandoDeVerdad = state.isLoading && state.competences.isEmpty()
    val mostrarEsqueleto = esperaVisible(cargandoDeVerdad)

    when {
        cargandoDeVerdad && !mostrarEsqueleto -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        mostrarEsqueleto -> LoadingIndicatorSafe()
        state.error != null -> ErrorStateSafe(
            error = state.error,
            onRetry = onRetry
        )
        state.competences.isEmpty() -> EmptyStateSafe()
        else -> ProgressSuccessContentSafe(
            userStats = state.userStats,
            competences = state.competences,
            weeklyProgress = state.weeklyProgress,
            ranking = state.ranking,
            rankingCargando = state.rankingCargando,
            rankingFallo = state.rankingFallo,
            formattedPracticeTime = formattedPracticeTime,
            onCompetencyClick = onCompetencyClick,
            isSyncing = state.isSyncing,
            syncMessage = state.syncMessage,
            syncFailed = state.syncFailed,
            onSync = onSync
        )
    }
}

@Composable
private fun ProgressSuccessContentSafe(
    userStats: UserStats?,
    competences: List<Competence>,
    weeklyProgress: List<DailyProgress>,
    ranking: RankingResponse?,
    rankingCargando: Boolean,
    rankingFallo: Boolean,
    formattedPracticeTime: String,
    onCompetencyClick: (Competence) -> Unit,
    isSyncing: Boolean,
    syncMessage: String?,
    syncFailed: Boolean,
    onSync: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

        item {
            WeeklyActivityCard(weeklyProgress = weeklyProgress)
        }

        // Unica seccion que depende del panel: tiene su propio estado para que
        // su espera no bloquee lo que ya esta listo.
        item {
            when {
                ranking != null -> RankingCard(ranking = ranking)
                rankingCargando -> RankingSkeleton()
                rankingFallo -> RankingNoDisponible()
            }
        }

        item {
            Text(
                text = "Progreso por competencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(items = competences, key = { it.id }) { competence ->
            CompetenceProgressCardSafe(
                competence = competence,
                onClick = { onCompetencyClick(competence) }
            )
        }

        item {
            SyncCard(
                isSyncing = isSyncing,
                message = syncMessage,
                failed = syncFailed,
                onSync = onSync
            )
        }
    }
}

/**
 * Sincronización manual con el panel, al final de Progreso.
 *
 * Es el sitio donde el dato que se envía está a la vista, así que es donde
 * tiene sentido pedir el envío. La comprobación automática de conexión se hace
 * una sola vez, en el splash.
 */
@Composable
private fun SyncCard(
    isSyncing: Boolean,
    message: String?,
    failed: Boolean,
    onSync: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sincronización",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Envía tu progreso al panel de la universidad. Practicar no necesita conexión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // El aviso va encima del botón: la tarjeta es lo último de la
            // lista y debajo del botón quedaba fuera de la pantalla.
            message?.let { texto ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (failed) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = texto,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (failed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Button(
                onClick = onSync,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizando…")
                } else {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizar ahora")
                }
            }
        }
    }
}

@Composable
fun MainStatsCardSafe(practiceTime: String, totalQuestions: Int, streakDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // «Hoy» va en la etiqueta: el contador se reinicia cada dia y sin eso
            // un cero a primera hora parece un fallo.
            StatItemSafe(label = "Tiempo hoy", value = practiceTime, modifier = Modifier.weight(1f))
            Divider(modifier = Modifier.width(1.dp).height(40.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
            StatItemSafe(label = "Preguntas", value = totalQuestions.toString(), modifier = Modifier.weight(1f))
            Divider(modifier = Modifier.width(1.dp).height(40.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
            StatItemSafe(label = "Racha", value = "$streakDays días", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatItemSafe(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), textAlign = TextAlign.Center)
    }
}

@Composable
fun CompetenceProgressCardSafe(competence: Competence, onClick: () -> Unit) {
    val nivelesCompletados = competence.levels.count { it.isCompleted }
    val nivelActual = competence.levels.firstOrNull { !it.isLocked && !it.isCompleted }
    val progress = competence.totalProgress.coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(
            onClick = onClick,
            indication = LocalIndication.current,
            interactionSource = remember { MutableInteractionSource() }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                if (competence.iconResId != 0) {
                    Image(painter = painterResource(id = competence.iconResId), contentDescription = competence.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Text(text = competence.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = competence.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "$nivelesCompletados/${competence.levels.size} niveles completados", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                nivelActual?.let { nivel ->
                    Text(text = "Nivel actual: ${nivel.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = progress, modifier = Modifier.size(50.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp, trackColor = MaterialTheme.colorScheme.outlineVariant)
                Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun WeeklyActivityCard(weeklyProgress: List<DailyProgress>) {
    val today = LocalDate.now()
    val last7Days = (6 downTo 0).map { daysAgo ->
        val date = today.minusDays(daysAgo.toLong())
        val dateStr = date.format(DateTimeFormatter.ISO_DATE)
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "CO")).replaceFirstChar { it.uppercase() }.take(2)
        val activity = weeklyProgress.firstOrNull { it.date == dateStr }
        Triple(dateStr, dayName, activity?.questionsAnswered ?: 0)
    }
    val maxQuestions = last7Days.maxOfOrNull { it.third } ?: 1

    // El día que rompió la racha: el primero en blanco después de uno activo.
    // Se marca solo ese, no los que vengan detrás: los siguientes días sin
    // practicar no rompieron nada, la racha ya estaba perdida, y pintarlos
    // todos convertiría una semana normal de tres días en un parte de faltas.
    //
    // Hoy nunca se marca. El día no ha terminado y aún se puede practicar; dar
    // por perdida una racha que todavía se puede salvar es justo lo contrario
    // de lo que debe hacer la pantalla.
    //
    // Si la ruptura ocurrió antes del primer día de la ventana no hay con qué
    // compararla y no se marca: se prefiere callar a señalar el día que no es.
    val rompioLaRacha = last7Days.mapIndexed { i, dia ->
        val esHoy = i == last7Days.lastIndex
        val vieneDeDiaActivo = i > 0 && last7Days[i - 1].third > 0
        !esHoy && dia.third == 0 && vieneDeDiaActivo
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                last7Days.forEachIndexed { i, (_, dayName, questions) ->
                    val fraction = if (maxQuestions > 0) questions.toFloat() / maxQuestions.toFloat() else 0f
                    val rachaPerdida = rompioLaRacha[i]
                    // El rojo es el mismo que la app ya usa para las respuestas
                    // incorrectas, así que el significado no hay que aprenderlo.
                    val colorBarra = when {
                        questions > 0 -> MaterialTheme.colorScheme.primary
                        rachaPerdida -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f)) {
                        // La flama ocupa el hueco donde los días activos ponen su
                        // número, así que la tarjeta no cambia de alto.
                        if (questions > 0) {
                            Text(text = "$questions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        } else if (rachaPerdida) {
                            Icon(
                                imageVector = Icons.Outlined.LocalFireDepartment,
                                contentDescription = "Aquí se perdió la racha",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val barHeight = (fraction * 80f).coerceAtLeast(4f)
                        Box(modifier = Modifier.fillMaxWidth(0.6f).height(barHeight.dp).clip(RoundedCornerShape(2.dp)).background(colorBarra).align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                questions > 0 -> MaterialTheme.colorScheme.onSurface
                                rachaPerdida -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (questions > 0 || rachaPerdida) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            val totalWeekQuestions = last7Days.sumOf { it.third }
            val activeDays = last7Days.count { it.third > 0 }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "$totalWeekQuestions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "preguntas esta semana", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "$activeDays/7", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "días activos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RankingSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Ranking Global",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Tres filas del alto de RankingItem: reservan el sitio para que la
            // lista no de un salto cuando llegue el ranking de verdad.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingNoDisponible() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Ranking Global",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "No se pudo cargar. Tu progreso sigue guardado en el teléfono.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RankingCard(ranking: RankingResponse) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(text = "Ranking Global", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ranking.top10.forEach { entry -> 
                    RankingItem(entry = entry) 
                }
            }
            ranking.currentUser?.let { currentUser ->
                if (!currentUser.inTop10 && currentUser.position != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    RankingItem(
                        entry = RankingEntryDto(position = currentUser.position, username = currentUser.username, totalQuestionsAnswered = currentUser.totalQuestionsAnswered ?: 0, isCurrentUser = true)
                    )
                }
            }
        }
    }
}

@Composable
fun RankingItem(entry: RankingEntryDto) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (entry.isCurrentUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent).padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${entry.position}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = when (entry.position) { 1 -> Color(0xFFFFD700); 2 -> Color(0xFFC0C0C0); 3 -> Color(0xFFCD7F32); else -> MaterialTheme.colorScheme.onSurfaceVariant }, modifier = Modifier.width(28.dp))
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (entry.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Text(text = entry.username.take(1).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (entry.isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = if (entry.isCurrentUser) "${entry.username} (Tú)" else entry.username, style = MaterialTheme.typography.bodyMedium, fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "${entry.totalQuestionsAnswered} pts", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun LoadingIndicatorSafe() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        repeat(3) {
            com.universidad.reta2.ui.components.CompetenceSkeletonItem()
        }
    }
}

@Composable
private fun ErrorStateSafe(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Error al cargar el progreso", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Reintentar") }
        }
    }
}

@Composable
private fun EmptyStateSafe() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        com.universidad.reta2.ui.components.IllustratedEmptyState(
            title = "No hay datos de progreso",
            description = "Completa algunos niveles para ver tus estadísticas y ranking aquí.",
            actionLabel = "Jugar primer nivel",
            onActionClick = {
                // Noop o navegación al inicio / competencias
            }
        )
    }
}
