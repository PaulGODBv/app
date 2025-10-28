package com.universidad.reta2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.universidad.reta2.ui.screens.login.LoginScreen
import com.universidad.reta2.ui.screens.competencies.CompetenciesScreen
import com.universidad.reta2.ui.screens.competenceDetail.CompetenceDetailScreen
import com.universidad.reta2.ui.screens.questions.QuestionScreen
import com.universidad.reta2.ui.screens.profile.ProfileScreen
import com.universidad.reta2.ui.screens.registration.RegistrationScreen
import com.universidad.reta2.ui.screens.progress.ProgressScreen
import com.universidad.reta2.ui.screens.splash.SplashScreen
import com.universidad.reta2.ui.screens.home.HomeScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        // ---------- Login ----------
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // ---------- Registro ----------
        composable(route = Screen.Registration.route) {
            RegistrationScreen(navController = navController)
        }

        // ---------- Home ----------
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }



        // ---------- Competencias ----------
        composable(route = Screen.Competencies.route) {
            CompetenciesScreen(
                onCompetenceClick = { competenceId ->
                    navController.navigate(Screen.CompetenceDetail.createRoute(competenceId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ---------- Detalle de competencia ----------
        composable(
            route = Screen.CompetenceDetail.route,
            arguments = Screen.CompetenceDetail.arguments
        ) { backStackEntry ->
            val competenceId = backStackEntry.arguments?.getInt("competenceId") ?: 0
            CompetenceDetailScreen(
                competenceId = competenceId,
                onModuleClick = { moduleId ->
                    navController.navigate(
                        Screen.Questions.createRoute(
                            competenceId = competenceId,
                            levelId = moduleId
                        )
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }


        // ---------- Preguntas ----------
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

        // ---------- Progreso ----------
        composable(route = Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }

        // ---------- Perfil ----------
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
