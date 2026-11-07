package com.universidad.reta2.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Inicio", Screen.Home.route, Icons.Filled.Home),
        // «Modulos» y no «Competencias»: con cinco pestanias la larga se
        // partia en dos lineas y desnivelaba la barra entera. Y no es solo
        // que quepa — «modulo» es como llama el Icfes a Lectura Critica,
        // Razonamiento Cuantitativo y las demas.
        BottomNavItem("Módulos", Screen.Competencies.route, Icons.Filled.List),
        BottomNavItem("Progreso", Screen.Progress.route, Icons.Filled.Star),
        BottomNavItem("Perfil", Screen.Profile.route, Icons.Filled.Person),
        // «Ajustes» y no «Configuración»: con cinco pestañas cada una baja de
        // 270 a 216 px, y la palabra larga no cabe. La pantalla sí se titula
        // Configuración por dentro, donde hay sitio.
        BottomNavItem("Ajustes", Screen.Configuracion.route, Icons.Filled.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    /* El fondo, explicito y no el de serie.
     *
     * `NavigationBar` toma por defecto `surfaceContainer`, y el tema de la app
     * NO define ese rol, asi que Material 3 lo sacaba de su esquema base: un
     * gris violaceo, #211f26 medido en pantalla, que no es ninguno de los
     * grises del proyecto (#0e1116, #161a21, #252b34). En una app azul se
     * notaba.
     *
     * Se le pone `surface`, que es el mismo gris de las tarjetas. La pastilla
     * del seleccionado se queda como estaba, por decision del autor. */
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            // Respuesta al pulsar, como en Material 3: el icono se hunde un
            // poco mientras el dedo esta encima y vuelve con un rebote corto al
            // soltar. El ripple de la pestana lo sigue poniendo el componente.
            val interaccion = remember { MutableInteractionSource() }
            val pulsado by interaccion.collectIsPressedAsState()
            val escala by animateFloatAsState(
                targetValue = if (pulsado) 0.86f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "escalaPulsacion"
            )

            NavigationBarItem(
                interactionSource = interaccion,
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.scale(escala)
                    )
                },
                /* Una linea y recorte, aunque hoy ninguna etiqueta lo
                 * necesite: con cinco pestanias cada una baja de 270 a 216 px,
                 * y la que se pase se parte en dos lineas desnivelando la
                 * barra entera, porque el item mas alto manda. Recortada se ve
                 * mal una; partida se descolocan las cinco. Queda de red. */
                label = {
                    Text(
                        text = item.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentRoute == item.route,
                // Estar en una pestaña es estado, no logro: el indicador va en
                // azul. El ámbar queda reservado para rachas y desbloqueos.
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)