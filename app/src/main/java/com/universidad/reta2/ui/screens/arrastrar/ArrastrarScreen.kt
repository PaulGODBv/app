package com.universidad.reta2.ui.screens.arrastrar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.ui.components.TextContextModal
import com.universidad.reta2.ui.theme.Veredicto
import kotlin.math.roundToInt

/**
 * Completar el texto arrastrando (disposición A de tres).
 *
 * Una frase por línea, con su hueco ancho, y el banco de palabras abajo. Se
 * colocan todas y **después** se comprueba de golpe, que es lo que distingue
 * este formato del de unir parejas.
 *
 * Además del arrastre se puede tocar: una palabra y luego su hueco. No es un
 * capricho — el arrastre con el dedo tapa la diana, y quien use lector de
 * pantalla no puede arrastrar.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArrastrarScreen(
    navController: NavController,
    competenceId: Int,
    levelId: Int,
    viewModel: ArrastrarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(competenceId, levelId) {
        viewModel.cargar(competenceId, levelId)
    }

    // Arrastre: qué palabra va en el aire, dónde está el dedo, y dónde quedó
    // cada hueco. Los límites se miden en coordenadas de la raíz para poder
    // compararlos con la posición del dedo sin convertir nada.
    var enElAire by remember { mutableStateOf<String?>(null) }
    var posicionDelDedo by remember { mutableStateOf(Offset.Zero) }
    val limitesDeHueco = remember { mutableStateMapOf<Int, Rect>() }

    // Alternativa por toque.
    var palabraTocada by remember { mutableStateOf<String?>(null) }
    var verPasaje by remember { mutableStateOf(false) }

    if (verPasaje && uiState.pasaje.isNotBlank()) {
        TextContextModal(readingText = uiState.pasaje, onDismiss = { verPasaje = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Completa el texto",
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
                    if (uiState.comprobado) {
                        Text(
                            text = "${uiState.aciertos}/${uiState.huecos.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.cargando -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                uiState.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(uiState.error!!, style = MaterialTheme.typography.bodyLarge) }

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    if (uiState.pasaje.isNotBlank()) {
                        OutlinedButton(
                            onClick = { verPasaje = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text("Ver el texto completo")
                        }
                    }

                    Text(
                        text = when {
                            uiState.comprobado ->
                                "Acertaste ${uiState.aciertos} de ${uiState.huecos.size}"
                            palabraTocada != null -> "Ahora toca su hueco"
                            else -> "Arrastra cada palabra a su hueco, o tócala y luego el hueco"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.huecos, key = { it.questionId }) { hueco ->
                            LineaConHueco(
                                hueco = hueco,
                                comprobado = uiState.comprobado,
                                resaltado = enElAire != null || palabraTocada != null,
                                onMedido = { limitesDeHueco[hueco.questionId] = it },
                                onClick = {
                                    val tocada = palabraTocada
                                    if (tocada != null) {
                                        viewModel.colocar(hueco.questionId, tocada)
                                        palabraTocada = null
                                    } else {
                                        viewModel.quitar(hueco.questionId)
                                    }
                                }
                            )
                        }
                    }

                    if (!uiState.comprobado) {
                        BancoArrastrable(
                            palabras = uiState.banco,
                            tocada = palabraTocada,
                            onTocar = { palabraTocada = if (palabraTocada == it) null else it },
                            onArrastrarInicio = { palabra, posicion ->
                                enElAire = palabra
                                posicionDelDedo = posicion
                                palabraTocada = null
                            },
                            onArrastrar = { posicionDelDedo = it },
                            onArrastrarFin = {
                                val palabra = enElAire
                                val destino = limitesDeHueco.entries
                                    .firstOrNull { it.value.contains(posicionDelDedo) }
                                if (palabra != null && destino != null) {
                                    viewModel.colocar(destino.key, palabra)
                                }
                                enElAire = null
                            }
                        )

                        Button(
                            onClick = viewModel::comprobar,
                            enabled = uiState.todosLlenos,
                            modifier = Modifier.fillMaxWidth().padding(20.dp)
                        ) {
                            Text(
                                if (uiState.todosLlenos) "Comprobar"
                                else "Coloca todas las palabras"
                            )
                        }
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

            // La palabra que va en el aire, siguiendo al dedo. Encima de todo.
            enElAire?.let { palabra ->
                Surface(
                    modifier = Modifier
                        .zIndex(10f)
                        .offset {
                            IntOffset(
                                (posicionDelDedo.x - 60).roundToInt(),
                                (posicionDelDedo.y - 90).roundToInt()
                            )
                        },
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = palabra,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LineaConHueco(
    hueco: ArrastrarViewModel.HuecoUi,
    comprobado: Boolean,
    resaltado: Boolean,
    onMedido: (Rect) -> Unit,
    onClick: () -> Unit
) {
    val verde = Veredicto.colores
    val interactionSource = remember { MutableInteractionSource() }

    val borde = when {
        comprobado && hueco.acertado -> verde.acierto
        comprobado -> MaterialTheme.colorScheme.error
        hueco.colocada != null -> MaterialTheme.colorScheme.primary
        // Mientras hay una palabra en juego, los huecos vacíos se señalan para
        // que se vea dónde se puede soltar.
        resaltado -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { onMedido(it.boundsInRoot()) }
            .clickable(
                enabled = !comprobado,
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                comprobado && hueco.acertado -> verde.aciertoContenedor
                comprobado -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = BorderStroke(if (comprobado || hueco.colocada != null) 2.dp else 1.dp, borde),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (comprobado) {
                Icon(
                    imageVector = if (hueco.acertado) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = if (hueco.acertado) "Bien" else "Mal",
                    tint = if (hueco.acertado) verde.acierto else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildString {
                        append(hueco.antes)
                        append("  ")
                        append(hueco.colocada ?: "______")
                        append("  ")
                        append(hueco.despues)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Al comprobar, la fallada enseña cuál era.
                if (comprobado && !hueco.acertado) {
                    Text(
                        text = "Era: ${hueco.correcta}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = verde.acierto,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BancoArrastrable(
    palabras: List<String>,
    tocada: String?,
    onTocar: (String) -> Unit,
    onArrastrarInicio: (String, Offset) -> Unit,
    onArrastrar: (Offset) -> Unit,
    onArrastrarFin: () -> Unit
) {
    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            palabras.forEach { palabra ->
                PalabraArrastrable(
                    palabra = palabra,
                    tocada = palabra == tocada,
                    onTocar = { onTocar(palabra) },
                    onArrastrarInicio = { onArrastrarInicio(palabra, it) },
                    onArrastrar = onArrastrar,
                    onArrastrarFin = onArrastrarFin
                )
            }
        }
    }
}

@Composable
private fun PalabraArrastrable(
    palabra: String,
    tocada: Boolean,
    onTocar: () -> Unit,
    onArrastrarInicio: (Offset) -> Unit,
    onArrastrar: (Offset) -> Unit,
    onArrastrarFin: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    // La posición de la ficha en la raíz: sumada al desplazamiento local del
    // gesto da la del dedo en el mismo sistema en que se miden los huecos.
    var posicionEnRaiz by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .widthIn(min = 56.dp)
            .onGloballyPositioned { posicionEnRaiz = it.positionInRoot() }
            .pointerInput(palabra) {
                detectDragGestures(
                    onDragStart = { local -> onArrastrarInicio(posicionEnRaiz + local) },
                    onDrag = { cambio, delta ->
                        cambio.consume()
                        onArrastrar(posicionEnRaiz + cambio.position)
                    },
                    onDragEnd = { onArrastrarFin() },
                    onDragCancel = { onArrastrarFin() }
                )
            }
            .clickable(
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                onClick = onTocar
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (tocada) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = if (tocada) 2.dp else 1.dp,
            color = if (tocada) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = palabra,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}
