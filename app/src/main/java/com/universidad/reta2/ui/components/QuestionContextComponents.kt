package com.universidad.reta2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

/**
 * Que le damos a Coil para pintar la imagen de contexto.
 *
 * Coil acepta tanto una URL como el id de un recurso, asi que aqui se decide
 * una vez y el resto del archivo no vuelve a preguntarselo:
 *
 * - Si la cadena es una URL, viene del panel. Coil la descarga y la deja en su
 *   cache de disco, de modo que la siguiente vez no necesita conexion.
 * - Si no, es el nombre de un drawable del contenido de arranque. Se resuelve
 *   con getIdentifier, que solo funciona para lo empaquetado en el APK; ese
 *   camino desaparece cuando se retire CompetencyData.
 */
@Composable
internal fun modeloDeImagen(imagen: String?): Any? {
    val contexto = LocalContext.current
    return remember(imagen) {
        when {
            imagen.isNullOrBlank() -> null
            imagen.startsWith("http", ignoreCase = true) -> imagen
            else -> runCatching {
                contexto.resources.getIdentifier(imagen, "drawable", contexto.packageName)
            }.getOrDefault(0).takeIf { it != 0 }
        }
    }
}


/**
 * Tine el trazo de una figura con el color del tema, o no tine nada.
 *
 * Las figuras que publica el panel vienen despegadas del papel: el fondo es
 * transparente y el dibujo es tinta negra con el alfa haciendo de matiz. Sobre
 * una superficie oscura, esa tinta negra seria invisible, asi que se sustituye
 * su color por el del tema —oscuro en claro, claro en oscuro— conservando el
 * alfa. `SrcIn` hace justo eso.
 *
 * **Solo para lo que llega por URL.** El contenido de arranque son drawables
 * del APK, imagenes opacas sin matiz: tenirlas con `SrcIn` las convertiria en
 * un rectangulo de color liso. `modeloDeImagen` devuelve `String` para lo del
 * panel e `Int` para lo empaquetado, y esa es la distincion que se usa aqui.
 */
@Composable
private fun filtroDeFigura(modelo: Any?, tinta: Color): ColorFilter? =
    if (modelo is String) ColorFilter.tint(tinta, BlendMode.SrcIn) else null

// ── CARD DE CONTEXTO ──────────────────────────────────────────
// Reutilizable en QuestionScreen y TimedModeScreen

@Composable
internal fun QuestionContextCard(
    readingText: String,
    contextImage: String?,
    contextImageAlt: String? = null,
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
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Contexto:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                readingText.isNotEmpty() && contextImage == null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = readingText,
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
                }

                contextImage != null && readingText.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Imagen de referencia:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        LoadContextImage(
                            imageName = contextImage,
                            alt = contextImageAlt,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
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

                readingText.isNotEmpty() && contextImage != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = readingText,
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

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Imagen de referencia:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            LoadContextImage(
                                imageName = contextImage,
                                alt = contextImageAlt,
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

                else -> { /* Sin contexto, no mostrar nada */ }
            }
        }
    }
}

// ── MODAL DE TEXTO COMPLETO VIA MODAL BOTTOM SHEET ─────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TextContextModal(
    readingText: String,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Dos alturas: abre a media pantalla y el arrastre la sube hasta arriba.
    // `skipPartiallyExpanded = false` es el valor por defecto, pero se escribe
    // a proposito: es la decision de diseno, no una casualidad de la libreria.
    val estadoDeLaHoja = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = estadoDeLaHoja,
        // Sin esto, al subirla del todo la hoja se mete debajo de la barra de
        // estado y el tirador de arrastre acaba entre los iconos del reloj y
        // la bateria. Arriba del todo es hasta el borde util, no hasta el
        // borde fisico.
        modifier = Modifier.statusBarsPadding(),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            // Alto completo, no 0.7f. Con el tope al 70 % la hoja no podia
            // subir mas aunque se arrastrase: es lo que se veia como "solo
            // cubre 3/4 de la pantalla". Ahora el estado manda, y el contenido
            // da de si hasta arriba.
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contexto Completo",
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

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
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

            // Aqui habia un boton de "Cerrar" anclado abajo. Se quita: repetia
            // lo que ya hacen la X de la cabecera, el gesto hacia abajo y el
            // boton de atras, y a media altura quedaba fuera de la pantalla
            // mientras se comia espacio de lectura al subirla.
        }
    }
}

// ── MODAL DE IMAGEN CON ZOOM ───────────────────────────────────

@Composable
internal fun ImageContextModal(
    imageName: String,
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onDismiss: () -> Unit
) {
    val modelo = modeloDeImagen(imageName)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f)),
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Imagen de Contexto",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Divider()

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

                        if (modelo != null) {
                            AsyncImage(
                                colorFilter = filtroDeFigura(
                                    modelo,
                                    MaterialTheme.colorScheme.onSurface
                                ),
                                model = modelo,
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
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.BrokenImage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(56.dp)
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

// ── IMAGEN PEQUEÑA DE PREVIEW ──────────────────────────────────

@Composable
internal fun LoadContextImage(
    imageName: String,
    alt: String? = null,
    modifier: Modifier = Modifier,
    tinta: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val modelo = modeloDeImagen(imageName)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (modelo != null) {
            AsyncImage(
                model = modelo,
                // El texto alternativo describe lo que se ve; el nombre del
                // archivo no le dice nada a quien usa lector de pantalla.
                contentDescription = alt ?: "Imagen de contexto",
                modifier = modifier.clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Fit,
                colorFilter = filtroDeFigura(modelo, tinta)
            )
            // Pie de figura. La URL no sirve de pie —saldria "Https: media
            // question-context…"—, asi que solo se pinta el texto alternativo
            // que manda el panel; para el contenido de arranque se cae al
            // nombre del drawable, que es lo unico que hay.
            val pie = alt?.takeIf { it.isNotBlank() }
                ?: imageName.takeIf { !it.startsWith("http", ignoreCase = true) }
                    ?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
            if (pie != null) {
                Text(
                    text = pie,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic
                )
            }
        } else {
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
                    Icon(
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
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
                }
            }
        }
    }
}

// ── UTILIDAD ───────────────────────────────────────────────────

/**
 * Recortaba la vista previa del contexto a 150 caracteres. Ya no recorta.
 *
 * El `Text` que la pintaba ya tenia `maxLines = 2` y `TextOverflow.Ellipsis`,
 * asi que este recorte a mano llegaba **antes** que el del propio componente
 * y cortaba de mas: un contexto de 158 caracteres se veia como «...poblacion
 * col...» y al abrir «Ver texto completo» aparecian ocho letras mas. Parecia
 * que el dato estaba incompleto cuando no lo estaba, y eso mando a buscar un
 * fallo de datos en Competencias Ciudadanas que no existia.
 *
 * Ahora el limite lo pone la tipografia: dos lineas exactas, con puntos
 * suspensivos solo si de verdad sobra texto.
 */
@Deprecated("El recorte lo hace maxLines; pasa el texto entero.")
internal fun getTextPreview(fullText: String, maxLines: Int = 2): String = fullText
