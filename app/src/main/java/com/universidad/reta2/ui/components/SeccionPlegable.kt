package com.universidad.reta2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Una sección de ajustes que se abre al tocarla.
 *
 * POR QUÉ. La pantalla de perfil enseñaba todas sus tarjetas a la vez
 * —información personal, apariencia, sonido, contraseña— y había que bajar
 * por todas para llegar a cualquiera. Plegadas, la pantalla entera cabe de un
 * vistazo y se abre solo lo que se va a tocar.
 *
 * El contenido **no se compone mientras está plegada**: no es solo que no se
 * vea, es que no existe. Importa porque dentro hay campos de texto, y uno que
 * no está compuesto no puede robar el foco ni abrir el teclado al entrar.
 */
@Composable
fun SeccionPlegable(
    titulo: String,
    icono: ImageVector,
    expandida: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    resumen: String? = null,
    contenido: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = alPulsar)
                // Sin esto, un lector de pantalla lee el título y no dice si
                // está abierta o cerrada, que es justo lo que hay que saber
                // antes de decidir si tocarla.
                .semantics {
                    stateDescription = if (expandida) "Desplegada" else "Plegada"
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                // El resumen es lo que evita tener que abrir para mirar: dice
                // «Modo Oscuro» o «Paul Contreras» sin desplegar nada.
                if (resumen != null && !expandida) {
                    Text(
                        text = resumen,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = if (expandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (expandida) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                contenido()
            }
        }
    }
}
