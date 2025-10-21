package com.universidad.reta2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.universidad.reta2.ui.screens.dashboard.DashboardScreen
import com.universidad.reta2.ui.screens.login.LoginScreen
import com.universidad.reta2.ui.screens.registration.RegistrationScreen
import com.universidad.reta2.ui.screens.competencies.CompetenciesScreen
import com.universidad.reta2.ui.screens.competenceDetail.CompetenceDetailScreen
import com.universidad.reta2.ui.screens.questions.QuestionScreen
import com.universidad.reta2.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Pantalla de Login
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Pantalla de Registro
        composable(route = Screen.Registration.route) {
            RegistrationScreen(navController = navController)
        }

        // Dashboard principal
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onCompetenceClick = { competenceId ->
                    navController.navigate(Screen.CompetenceDetail.createRoute(competenceId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Competencias
        composable(route = Screen.Competencies.route) {
            CompetenciesScreen(
                onCompetenceClick = { competenceId ->
                    navController.navigate(Screen.CompetenceDetail.createRoute(competenceId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Detalle de competencia
        composable(
            route = Screen.CompetenceDetail.route + "/{competenceId}"
        ) { backStackEntry ->
            val competenceId = backStackEntry.arguments?.getString("competenceId") ?: ""
            CompetenceDetailScreen(
                competenceId = competenceId,
                onBackClick = { navController.popBackStack() }
            )
        }

        //Preguntas
        composable(
                route = Screen.Questions.route,
                arguments = Screen.Questions.arguments
            ) { backStackEntry ->
                val competencyId = backStackEntry.arguments?.getString("competencyId") ?: ""
                val levelId = backStackEntry.arguments?.getString("levelId") ?: ""

                QuestionScreen(
                    navController = navController,
                    competencyId = competencyId,
                    levelId = levelId
                )
            }

            // Perfil
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

