package com.universidad.reta2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.universidad.reta2.ui.screens.dashboard.DashboardScreen
import com.universidad.reta2.ui.screens.login.LoginScreen
import com.universidad.reta2.ui.screens.competencies.CompetenciesScreen
import com.universidad.reta2.ui.screens.competenceDetail.CompetenceDetailScreen
import com.universidad.reta2.ui.screens.questions.QuestionScreen
import com.universidad.reta2.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

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

        composable(route = Screen.Competencies.route) {
            CompetenciesScreen(
                onCompetenceClick = { competenceId ->
                    navController.navigate(Screen.CompetenceDetail.createRoute(competenceId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CompetenceDetail.route,
            arguments = Screen.CompetenceDetail.arguments
        ) { backStackEntry ->
            val competenceId = backStackEntry.arguments?.getString("competenceId") ?: ""
            CompetenceDetailScreen(
                competenceId = competenceId,
                onModuleClick = { moduleId ->
                    navController.navigate(Screen.Questions.createRoute(competenceId, moduleId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Questions.route,
            arguments = Screen.Questions.arguments
        ) { backStackEntry ->
            val competenceId = backStackEntry.arguments?.getString("competenceId") ?: ""
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            QuestionScreen(
                competenceId = competenceId,
                moduleId = moduleId,
                onBackClick = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}