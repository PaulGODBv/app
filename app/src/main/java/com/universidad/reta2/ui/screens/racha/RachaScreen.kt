package com.universidad.reta2.ui.screens.racha

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.ui.navigation.Screen
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * El latido de la racha: sale entre terminar un nivel y ver los resultados.
 *
 * **Va en dos tiempos, y el orden importa.** Primero el número de días, que
 * cuenta del anterior al de hoy con un golpe de escala; cuando se ha asentado,
 * entra la semana desde abajo y la barra de hoy crece hasta las preguntas que
 * se acaban de responder. Un mensaje cada vez: primero la recompensa, después
 * la prueba. Los dos a la vez se estorban, porque la vista no sabe a cuál ir.
 *
 * **Solo aparece el día que la racha sube.** Lo decide quien navega hasta
 * aquí, no esta pantalla. Del segundo nivel del día en adelante se va directo
 * a resultados: una celebración que se repite sin motivo deja de celebrar.
 *
 * La semana se dibuja aquí y no se reutiliza `WeeklyActivityCard` de Progreso,
 * aunque se parezcan: aquella es un resumen con pie de totales y marca en rojo
 * el día en que se rompió la racha. Aquí no toca ninguna de las dos cosas —se
 * acaba de extender la racha, señalar una rotura sería contradecir el
 * momento—, así que sale más limpio dibujar las siete barras que parametrizar
 * la otra con tres interruptores.
 */
@Composable
fun RachaScreen(
    navController: NavController,
    competencyId: Int,
    levelId: Int,
    score: Int,
    totalQuestions: Int,
    timeSpent: Int,
    origin: String,
    viewModel: RachaViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargar(totalQuestions) }

    // 0: nada todavía · 1: el número cuenta · 2: entra la semana.
    var fase by remember { mutableIntStateOf(0) }
    LaunchedEffect(estado.cargado) {
        if (!estado.cargado) return@LaunchedEffect
        delay(200)
        fase = 1
        delay(900)
        fase = 2
    }

    val dias by animateIntAsState(
        targetValue = if (fase >= 1) estado.racha else estado.rachaAnterior,
        animationSpec = tween(durationMillis = 650),
        label = "diasDeRacha"
    )
    val escala by animateFloatAsState(
        targetValue = if (fase >= 1) 1f else 0.55f,
        // Un muelle con rebote: el número «aterriza» en vez de deslizarse, que
        // es lo que hace que se sienta como un premio y no como una carga.
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "escalaDelNumero"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            // Ámbar, que en esta paleta es el color del logro: rachas,
            // insignias y desbloqueos. El azul es para acciones y estado.
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(96.dp)
                .scale(escala)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$dias",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 72.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.scale(escala)
        )
        Text(
            text = if (dias == 1) "día de racha" else "días de racha",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = fase >= 2,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { alto -> alto / 2 }
        ) {
            SemanaQueCrece(
                semana = estado.semana,
                preguntasDeLaSesion = estado.preguntasDeLaSesion
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                navController.navigate(
                    Screen.Results.createRoute(
                        competenceId = competencyId,
                        levelId = levelId,
                        score = score,
                        totalQuestions = totalQuestions,
                        timeSpent = timeSpent,
                        origin = origin
                    )
                ) {
                    // Esta pantalla no vuelve: desde resultados, atrás tiene
                    // que llevar al menú y no a la celebración otra vez.
                    popUpTo(Screen.Racha.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }
    }
}

/**
 * Las siete barras de la semana, con la de hoy creciendo.
 *
 * La barra de hoy arranca de la altura que tenía **antes** de esta sesión —el
 * total del día menos lo que se acaba de responder— y sube hasta la de ahora.
 * Ver el hueco llenarse es lo que convierte un número en un logro.
 */
@Composable
private fun SemanaQueCrece(semana: List<DailyProgress>, preguntasDeLaSesion: Int) {
    val hoy = LocalDate.now()
    val dias = (6 downTo 0).map { atras ->
        val fecha = hoy.minusDays(atras.toLong())
        val clave = fecha.format(DateTimeFormatter.ISO_DATE)
        val nombre = fecha.dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale("es", "CO"))
            .replaceFirstChar { it.uppercase() }
            .take(2)
        Triple(clave, nombre, semana.firstOrNull { it.date == clave }?.questionsAnswered ?: 0)
    }
    val maximo = (dias.maxOfOrNull { it.third } ?: 1).coerceAtLeast(1)

    // Se dispara al componerse, que es justo cuando la tarjeta termina de
    // entrar: así el crecimiento no empieza mientras todavía está deslizándose.
    var crecer by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { crecer = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            dias.forEachIndexed { i, (_, nombre, preguntas) ->
                val esHoy = i == dias.lastIndex
                val antes = if (esHoy) (preguntas - preguntasDeLaSesion).coerceAtLeast(0) else preguntas
                val destino = if (esHoy && !crecer) antes else preguntas

                val fraccion by animateFloatAsState(
                    targetValue = destino.toFloat() / maximo.toFloat(),
                    animationSpec = tween(durationMillis = if (esHoy) 800 else 0),
                    label = "altura$i"
                )
                val cuenta by animateIntAsState(
                    targetValue = destino,
                    animationSpec = tween(durationMillis = if (esHoy) 800 else 0),
                    label = "cuenta$i"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (cuenta > 0) "$cuenta" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esHoy) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height((fraccion * 70f).coerceAtLeast(4f).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    // Hoy en ámbar: es el día que acaba de
                                    // sumar, y tiene que distinguirse del resto
                                    // de la semana de un vistazo.
                                    esHoy -> MaterialTheme.colorScheme.secondary
                                    preguntas > 0 -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esHoy) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
