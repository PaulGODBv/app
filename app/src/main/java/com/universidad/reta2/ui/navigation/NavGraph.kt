package com.universidad.reta2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import com.universidad.reta2.ui.screens.dashboard.DashboardScreen
import com.universidad.reta2.ui.screens.login.LoginScreen
import com.universidad.reta2.ui.screens.competencies.CompetenciesScreen
import com.universidad.reta2.ui.screens.competenceDetail.CompetenceDetailScreen
import com.universidad.reta2.ui.screens.questions.QuestionScreen
import com.universidad.reta2.ui.screens.profile.ProfileScreen
import com.universidad.reta2.ui.screens.registration.RegistrationScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController) // Solo pasa navController
        }

        composable(route = Screen.Registration.route) {
            RegistrationScreen(navController = navController)
        }

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                // Add the missing parameter here
                mainNavController = navController,
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
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            QuestionScreen(
                navController = navController,
                competencyId = competenceId,
                levelId = levelId
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}