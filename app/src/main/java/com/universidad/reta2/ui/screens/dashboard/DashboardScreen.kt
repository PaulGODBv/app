package com.universidad.reta2.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.universidad.reta2.ui.navigation.BottomNavigationBar
import com.universidad.reta2.ui.navigation.Screen
import com.universidad.reta2.ui.screens.competencies.CompetenciesScreen
import com.universidad.reta2.ui.screens.profile.ProfileScreen

@Composable
fun DashboardScreen(
    onCompetenceClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    mainNavController: NavController
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Dashboard.route, // O Screen.Home si prefieres
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                // Pantalla principal del dashboard
                DashboardHomeScreen(
                    onCompetenceClick = onCompetenceClick,
                    onProfileClick = onProfileClick
                )
            }
            composable(Screen.Competencies.route) {
                CompetenciesScreen(
                    onCompetenceClick = onCompetenceClick,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = mainNavController,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHomeScreen(
    onCompetenceClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dashboard Principal",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjetas de funcionalidades principales
        Card(
            onClick = { onCompetenceClick("competence1") }, // Ejemplo
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Competencias",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Explora y practica competencias",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            onClick = onProfileClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Perfil",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Ver tu perfil y estadísticas",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Aquí puedes agregar más componentes del dashboard:
        // - Estadísticas rápidas
        // - Progreso reciente
        // - Próximos objetivos, etc.
    }
}