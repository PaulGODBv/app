package com.universidad.reta2.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universidad.reta2.BuildConfig
import com.universidad.reta2.R

/**
 * Quién hizo esta aplicación y cómo se le escribe.
 *
 * POR QUÉ EXISTE. que quien use Reta2
 * pueda saber de dónde sale y tener por dónde contactar al que la hizo. Es un
 * trabajo de grado, y un trabajo de grado que nadie puede atribuir a nadie se
 * queda a medias.
 *
 * POR QUÉ ES UNA TARJETA Y NO UNA PANTALLA. De las cuatro ubicaciones que se
 * dibujaron —fila al final de Perfil, tarjeta dentro de Perfil, icono en la
 * cabecera, quinta pestaña— el autor eligió la tarjeta. Es la única que no
 * añade una ruta: se ve al bajar por Perfil, que es donde ya vive todo lo que
 * no es practicar.
 *
 * LOS DATOS SON CONSTANTES Y NO AJUSTES. No cambian por usuario ni por
 * compilación, y meterlos en `BuildConfig` o en `strings.xml` solo repartiría
 * en dos sitios algo que se lee de un vistazo aquí.
 */

/** Perfil de LinkedIn del autor. */
private const val ENLACE_LINKEDIN =
    "https://www.linkedin.com/in/paul-mateo-contreras-arias-0aaa6b388/"

/** Cuenta de GitHub del autor, la misma donde vive el repositorio de la app. */
private const val ENLACE_GITHUB = "https://github.com/PaulGODBv"

/**
 * Dirección a la que escribe quien pulsa el sobre.
 *
 * Cuenta propia de la aplicación y no la personal del autor, que es lo
 * correcto cuando la dirección la ve cualquiera que instale el APK.
 */
private const val CORREO_DE_CONTACTO = "appreta2@gmail.com"

/** Cómo se firma el autor, tal cual debe salir en pantalla. */
private const val NOMBRE_DEL_AUTOR = "Paul Mateo C."

/**
 * Las iniciales del círculo, mientras no haya foto.
 *
 * Para poner una: se deja el archivo en `res/drawable`, y este `Box` pasa a
 * ser un `Image` con `painterResource` y `Modifier.clip(CircleShape)`.
 */
private const val INICIALES = "PM"

@Composable
fun SobreReta2Card(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sobre Reta2",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = INICIALES,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "DESARROLLADO Y DISEÑADO POR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Text(
                    text = NOMBRE_DEL_AUTOR,
                    style = MaterialTheme.typography.titleLarge
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Las dos marcas son mascaras blancas con alfa, no dibujos
                    // a color, para que `Icon` las tina con el color del tema.
                    // De otro modo habria que elegir entre verlas en claro o
                    // verlas en oscuro, y una de las dos saldria perdiendo.
                    EnlaceDeContacto(
                        imagen = painterResource(R.drawable.ic_linkedin_marca),
                        descripcion = "Perfil de LinkedIn de $NOMBRE_DEL_AUTOR",
                        alPulsar = { abrir(contexto, ENLACE_LINKEDIN) }
                    )
                    EnlaceDeContacto(
                        imagen = painterResource(R.drawable.ic_github),
                        descripcion = "Cuenta de GitHub de $NOMBRE_DEL_AUTOR",
                        alPulsar = { abrir(contexto, ENLACE_GITHUB) }
                    )
                    EnlaceDeContacto(
                        imagen = rememberVectorPainter(Icons.Filled.Email),
                        descripcion = "Escribir a $NOMBRE_DEL_AUTOR",
                        alPulsar = { abrir(contexto, "mailto:$CORREO_DE_CONTACTO") }
                    )
                }

                // La version no estaba en el trazado elegido y se anade igual,
                // discreta: es el dato que hay que pedirle a alguien cuando
                // reporta un fallo, y el sitio donde se busca es este.
                Text(
                    text = "Versión ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EnlaceDeContacto(
    imagen: Painter,
    descripcion: String,
    alPulsar: () -> Unit
) {
    IconButton(
        onClick = alPulsar,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(painter = imagen, contentDescription = descripcion)
    }
}

/**
 * Abre un enlace fuera de la aplicación, y avisa si no hay con qué.
 *
 * El `try` no es decorativo: un teléfono sin cliente de correo lanza
 * `ActivityNotFoundException` al pulsar el sobre, y sin atraparlo la
 * aplicación se cierra desde la pantalla de perfil. Fallar en silencio
 * tampoco vale — quien pulsa un botón y no ve nada cree que está roto.
 */
private fun abrir(contexto: Context, destino: String) {
    try {
        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destino)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            contexto,
            "No hay ninguna aplicación en el teléfono que pueda abrir esto.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
