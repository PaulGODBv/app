package com.universidad.reta2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.universidad.reta2.ui.navigation.BottomNavigationBar
import com.universidad.reta2.ui.navigation.NavGraph
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.ui.theme.Reta2Theme
import com.universidad.reta2.ui.theme.ThemeViewModel
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.utils.Musica
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }

    // La música de fondo se calla al salir de la aplicación y vuelve al
    // entrar, pero solo si la pantalla de ese momento es de menú: eso lo
    // decide `Musica`, que recuerda si le tocaba sonar. Si se dejara a la
    // composición, volver de segundo plano arrancaría la música encima de una
    // pregunta.
    override fun onPause() {
        super.onPause()
        Musica.alIrseAlFondo()
    }

    override fun onResume() {
        super.onResume()
        Musica.alVolver(this)
    }
}


@Composable
fun AppContent(themeViewModel: ThemeViewModel = hiltViewModel()) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    Reta2Theme(darkTheme = isDark) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomNav = currentRoute in listOf(
            Screen.Home.route,
            Screen.Competencies.route,
            Screen.Progress.route,
            Screen.Profile.route
        ) && currentRoute != null // Asegurar que no se muestre durante la carga/splash

        // La música de fondo suena en el menú y calla dentro de un nivel. A
        // las cuatro de la barra inferior se suma el detalle de competencia,
        // que es la lista de niveles: ahí todavía se está eligiendo, no
        // respondiendo.
        //
        // Fuera quedan preguntas, unir, arrastrar y contrarreloj, y esa es la
        // decisión que importa: los pasajes de Lectura Crítica llegan a 2 100
        // caracteres, y una música con melodía compite con la comprensión
        // lectora justo cuando es lo único que se está midiendo.
        val rutasConMusica = listOf(
            Screen.Home.route,
            Screen.Competencies.route,
            Screen.Progress.route,
            Screen.Profile.route,
            Screen.CompetenceDetail.route
        )
        val context = LocalContext.current
        val musicaEncendida by SessionManager.musicaFlow.collectAsState()
        // Depende también del ajuste, para que encenderlo en Perfil se note
        // sin tener que cambiar de pantalla.
        LaunchedEffect(currentRoute, musicaEncendida) {
            Musica.enMenu(context, currentRoute in rutasConMusica)
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                bottomBar = {
                    if (showBottomNav) {
                        BottomNavigationBar(navController = navController)
                    }
                }
            ) { innerPadding ->
                NavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
