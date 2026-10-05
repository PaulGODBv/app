package com.universidad.reta2.ui.screens.competenceDetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.universidad.reta2.domain.LevelRules
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetenceDetailScreen(
    competenceId: Int,
    onLevelClick: (Int, String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: CompetenceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Nivel sobre el que se está eligiendo modo. Null = no hay hoja abierta.
    var nivelElegido by remember { mutableStateOf<Level?>(null) }

    nivelElegido?.let { nivel ->
        HojaDeModo(
            nivel = nivel,
            onCerrar = { nivelElegido = null },
            onElegir = { modo ->
                nivelElegido = null
                // El formato del nivel decide el destino cuando se practica.
                // En evaluacion se ignora: ahi siempre se elige una opcion.
                onLevelClick(nivel.id, modo, nivel.formatoPractica)
            }
        )
    }

    LaunchedEffect(competenceId) {
        viewModel.loadCompetenceDetail(competenceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.competence?.name ?: "Cargando...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${uiState.error}")
                }
            }

            uiState.competence == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Competencia no encontrada")
                }
            }

            else -> {
                val competence = uiState.competence!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- Tarjeta principal de la competencia ---
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // --- CONTENEDOR DE IMAGEN MEJORADO ---
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (competence.iconResId != 0) {
                                        Image(
                                            painter = painterResource(id = competence.iconResId),
                                            contentDescription = competence.name,
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(MaterialTheme.shapes.small),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        // Fallback elegante
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = competence.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = competence.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = competence.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Práctica y evaluación van en secciones aparte a
                    // propósito: son dos cosas distintas y mezclarlas en una
                    // sola lista perdía justo la diferencia que se pedía.
                    // Los "Calentamiento" que creó TODO-22 quedan fuera de la
                    // lista: con la práctica convertida en modo de cada nivel,
                    // ya no hacen falta como nivel aparte.
                    val evaluacion = competence.levels.filterNot { LevelRules.esDePractica(it.id) }

                    item {
                        Text(
                            text = "Niveles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(evaluacion) { level ->
                        LevelCard(
                            level = level,
                            // Se abre la hoja también en los bloqueados: ahí
                            // dentro se puede practicar, que es justo cómo se
                            // prepara uno para desbloquearlos. El candado solo
                            // cierra la evaluación.
                            onLevelClick = { nivelElegido = level }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Elegir entre practicar y evaluarse, al tocar un nivel.
 *
 * La decisión se pide aquí y no con un conmutador global porque un conmutador
 * se olvida, y olvidarlo significa creer que estás practicando mientras el
 * intento cuenta para el progreso. Eso es caro de deshacer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaDeModo(
    nivel: Level,
    onCerrar: () -> Unit,
    onElegir: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = nivel.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "¿Cómo quieres hacerlo?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            OpcionDeModo(
                icono = Icons.Filled.Bolt,
                titulo = "Practicar",
                detalle = "Te dice si acertaste y por qué, al momento. No cuenta para el progreso.",
                destacada = true,
                habilitada = true,
                onClick = { onElegir(Screen.Questions.MODO_PRACTICA) }
            )

            OpcionDeModo(
                icono = Icons.Filled.Flag,
                titulo = "Evaluarme",
                detalle = if (nivel.isLocked)
                    "Necesitas superar el nivel anterior para evaluarte en este."
                else
                    "Cuenta para tu progreso y puede desbloquear el siguiente nivel.",
                destacada = false,
                habilitada = !nivel.isLocked,
                onClick = { onElegir(Screen.Questions.MODO_EVALUACION) }
            )
        }
    }
}

@Composable
private fun OpcionDeModo(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    destacada: Boolean,
    habilitada: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val acento = if (habilitada) MaterialTheme.colorScheme.primary
                 else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = habilitada,
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !habilitada -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                destacada -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = acento,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (habilitada) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LevelCard(level: Level, esPractica: Boolean = false, onLevelClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                // Siempre habilitada, incluso con el nivel bloqueado: el toque
                // abre la hoja de modo, y ahí dentro se puede practicar. El
                // candado solo cierra la evaluación, no el nivel entero.
                enabled = true,
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                onClick = onLevelClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                esPractica -> MaterialTheme.colorScheme.primaryContainer
                level.isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (level.isLocked) 1.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icono del nivel
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            when {
                                esPractica -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                level.isLocked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            esPractica -> Icons.Filled.Bolt
                            level.isLocked -> Icons.Filled.Lock
                            else -> Icons.Filled.Star
                        },
                        contentDescription = when {
                            esPractica -> "Nivel de práctica"
                            level.isLocked -> "Nivel bloqueado"
                            else -> "Nivel disponible"
                        },
                        tint = if (level.isLocked && !esPractica)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = level.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (level.isLocked)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = level.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Progreso del nivel
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = level.progress.coerceIn(0f, 1f),
                    modifier = Modifier.width(80.dp),
                    color = if (level.isLocked)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Text(
                    text = "${(level.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (level.isLocked)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

