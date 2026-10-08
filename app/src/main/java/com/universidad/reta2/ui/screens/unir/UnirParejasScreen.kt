package com.universidad.reta2.ui.screens.unir

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.ui.theme.Veredicto

/**
 * Tablero de unir parejas (disposición B de tres).
 *
 * Los enunciados ocupan el ancho y el banco de palabras va debajo, porque los
 * datos reales son frases largas contra palabras sueltas: en dos columnas
 * enfrentadas las frases quedaban en una tira ilegible en un móvil. Y se ve el
 * tablero entero a propósito, que es lo que permite descartar por eliminación
 * y volver a intentar una pareja fallada.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UnirParejasScreen(
    navController: NavController,
    competenceId: Int,
    levelId: Int,
    viewModel: UnirParejasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(competenceId, levelId) {
        viewModel.cargar(competenceId, levelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Une las parejas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Text(
                        text = "${uiState.resueltas}/${uiState.parejas.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        when {
            uiState.cargando -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(uiState.error!!, style = MaterialTheme.typography.bodyLarge) }

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                Text(
                    text = if (uiState.terminado) "¡Todas emparejadas!"
                           else "Toca un enunciado y después su respuesta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.parejas, key = { it.questionId }) { pareja ->
                        FilaDeEnunciado(
                            pareja = pareja,
                            seleccionada = uiState.filaSeleccionada == pareja.questionId,
                            enRojo = uiState.fallo?.first == pareja.questionId,
                            onClick = { viewModel.seleccionarFila(pareja.questionId) }
                        )
                    }
                }

                if (!uiState.terminado) {
                    BancoDePalabras(
                        palabras = uiState.banco,
                        enRojo = uiState.fallo?.second,
                        hayFilaElegida = uiState.filaSeleccionada != null,
                        onElegir = viewModel::elegirPalabra
                    )
                } else {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                    ) {
                        Text("Terminar")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaDeEnunciado(
    pareja: UnirParejasViewModel.ParejaUi,
    seleccionada: Boolean,
    enRojo: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val verde = Veredicto.colores

    val fondo = when {
        enRojo -> MaterialTheme.colorScheme.errorContainer
        pareja.resuelta -> verde.aciertoContenedor
        seleccionada -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borde = when {
        enRojo -> MaterialTheme.colorScheme.error
        pareja.resuelta -> verde.acierto
        seleccionada -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                // Resuelta deja de aceptar toques: esa pareja ya está hecha.
                enabled = !pareja.resuelta,
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = androidx.compose.foundation.BorderStroke(
            width = if (seleccionada || pareja.resuelta) 2.dp else 1.dp,
            color = borde
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pareja.resuelta) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Emparejada",
                    tint = verde.acierto,
                    modifier = Modifier.size(20.dp)
                )
            } else if (enRojo) {
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = "No era esa",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = pareja.enunciado,
                style = MaterialTheme.typography.bodyMedium,
                // La resuelta se apaga para que destaque lo que falta.
                color = if (pareja.resuelta) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // El hueco: vacío mientras falta, con la palabra una vez resuelta.
            if (pareja.resuelta) {
                Text(
                    text = pareja.correcta,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = verde.acierto
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 58.dp, height = 26.dp)
                        .padding(2.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BancoDePalabras(
    palabras: List<String>,
    enRojo: String?,
    hayFilaElegida: Boolean,
    onElegir: (String) -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (!hayFilaElegida) {
                Text(
                    text = "Elige antes un enunciado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                palabras.forEach { palabra ->
                    PalabraDelBanco(
                        palabra = palabra,
                        enRojo = palabra == enRojo,
                        atenuada = !hayFilaElegida,
                        onClick = { onElegir(palabra) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PalabraDelBanco(
    palabra: String,
    enRojo: Boolean,
    atenuada: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier.clickable(
            indication = LocalIndication.current,
            interactionSource = interactionSource,
            onClick = onClick
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (enRojo) MaterialTheme.colorScheme.errorContainer
                             else MaterialTheme.colorScheme.surfaceVariant
                                 .copy(alpha = if (atenuada) 0.5f else 1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (enRojo) 2.dp else 1.dp,
            color = if (enRojo) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = palabra,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (enRojo) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}
