package com.universidad.reta2.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * «Tu progreso no se guardará. ¿Seguro quieres salir?»
 *
 * Salir de un cuestionario a medias **pierde la sesión entera**: las
 * respuestas no se registran una a una, se registran al terminar. Hasta ahora
 * un toque en la flecha de atrás —o un gesto de retroceso mal dado, que en
 * pantalla completa es facilísimo— tiraba sin avisar lo que se llevara hecho.
 *
 * Pregunta **siempre que se esté dentro**, y no solo cuando ya hay respuestas:
 * quien lleva cuatro preguntas leídas y ninguna contestada tampoco quiere
 * empezar de cero, y una confirmación que unas veces sale y otras no es peor
 * que una que sale siempre, porque deja de poder anticiparse.
 *
 * Cubre las dos salidas, que son dos y no una: la flecha de la barra y el
 * gesto o botón de retroceso del sistema. Tapar solo la primera dejaría
 * abierta justo la que se dispara sin querer.
 *
 * **Emite y además devuelve.** Pone el diálogo y engancha el retroceso del
 * sistema por su cuenta, y entrega la acción que la flecha de la barra tiene
 * que llamar. Se hizo así para no envolver el `Scaffold` de cuatro pantallas
 * —el diálogo vive en su propia ventana, así que no estorba a ningún
 * `layout`— y se usa en una línea:
 *
 *     val pedirSalida = confirmarAntesDeSalir { navController.popBackStack() }
 *     ...
 *     IconButton(onClick = pedirSalida) { Icon(ArrowBack, "Volver") }
 *
 * El botón de quedarse dice «Seguir practicando» donde se está practicando y
 * «Seguir» a secas donde no: en una evaluación y en el contrarreloj nadie está
 * practicando, y ofrecer seguir haciendo algo que no se está haciendo
 * desconcierta justo en el momento en que hay que decidir rápido.
 *
 * @param onSalir qué hacer cuando el estudiante confirma que sí quiere irse.
 * @param esPractica solo cambia esa palabra; no cambia ningún comportamiento.
 * @param activo si hay algo que perder ahora mismo. En falso, el retroceso del
 *   sistema vuelve a su comportamiento de siempre. Lo necesita el contrarreloj,
 *   que es un asistente de varios pasos y solo tiene una partida que perder
 *   mientras se juega: preguntar al retroceder desde «elegir competencia»
 *   sería preguntar por nada.
 * @return la acción que pide confirmación; no sale por su cuenta.
 */
@Composable
fun confirmarAntesDeSalir(
    esPractica: Boolean = true,
    activo: Boolean = true,
    onSalir: () -> Unit
): () -> Unit {
    var preguntando by remember { mutableStateOf(false) }

    // El gesto del sistema entra por aquí. Con el diálogo abierto se
    // desactiva, para que un segundo gesto lo cierre —que es lo que hace el
    // retroceso con un diálogo delante— en vez de encadenar dos preguntas.
    BackHandler(enabled = activo && !preguntando) { preguntando = true }

    if (preguntando) {
        AlertDialog(
            onDismissRequest = { preguntando = false },
            title = { Text("Tu progreso no se guardará") },
            text = { Text("¿Seguro quieres salir?") },
            confirmButton = {
                TextButton(onClick = {
                    preguntando = false
                    onSalir()
                }) {
                    // En rojo porque es la que destruye algo. El color no es
                    // decoración: es lo que distingue de un vistazo la opción
                    // que no tiene vuelta atrás.
                    Text("Sí", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { preguntando = false }) {
                    Text(if (esPractica) "Seguir practicando" else "Seguir")
                }
            }
        )
    }

    return { preguntando = true }
}
