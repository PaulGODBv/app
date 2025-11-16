package com.universidad.reta2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import com.universidad.reta2.ui.screens.login.LoginScreen
import com.universidad.reta2.ui.screens.competencies.CompetenciesScreen
import com.universidad.reta2.ui.screens.competenceDetail.CompetenceDetailScreen
import com.universidad.reta2.ui.screens.questions.QuestionScreen
import com.universidad.reta2.ui.screens.profile.ProfileScreen
import com.universidad.reta2.ui.screens.registration.RegistrationScreen
import com.universidad.reta2.ui.screens.progress.ProgressScreen
import com.universidad.reta2.ui.screens.splash.SplashScreen
import com.universidad.reta2.ui.screens.home.HomeScreen
import com.universidad.reta2.ui.screens.results.ResultsScreen
import com.universidad.reta2.ui.screens.questions.QuestionScreenUltraSafe
import kotlinx.coroutines.delay

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // 🔒 PROTECCIÓN GLOBAL CONTRA MÚLTIPLES NAVEGACIONES
    var isNavigating by remember { mutableStateOf(false) }

    // 🔒 EFFECT PARA RESETEAR EL ESTADO DE NAVEGACIÓN
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            delay(500) // Esperar 500ms después de navegar
            isNavigating = false
        }
    }

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
                    if (!isNavigating) {
                        val target = Screen.CompetenceDetail.createRoute(competenceId)
                        if (navController.safeNavigate(target) ) {
                            isNavigating = true
                        }
                    }
                },
                onBackClick = {
                    if (!isNavigating) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ---------- Detalle de competencia ----------
        composable(
            route = Screen.CompetenceDetail.route,
            arguments = Screen.CompetenceDetail.arguments
        ) { backStackEntry ->
            val competenceId = backStackEntry.arguments?.getInt("competenceId") ?: 1

            val previousRoute = navController.previousBackStackEntry?.destination?.route ?: "competencies"
            val origin = when {
                previousRoute == Screen.Progress.route -> "progress"
                else -> "competencies"
            }

            CompetenceDetailScreen(
                competenceId = competenceId,
                onLevelClick = { levelId ->
                    if (!isNavigating) {
                        val target = Screen.Questions.createRoute(
                            competenceId = competenceId,
                            levelId = levelId,
                            origin = origin
                        )
                        if (navController.safeNavigate(target)) {
                            isNavigating = true
                        }
                    }
                },
                onBackClick = {
                    if (!isNavigating) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ---------- Preguntas ----------
        composable(
            route = Screen.Questions.route,
            arguments = Screen.Questions.arguments
        ) { backStackEntry ->
            val competenceId = backStackEntry.arguments?.getInt("competenceId") ?: 0
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 0
            val origin = backStackEntry.arguments?.getString("origin") ?: "competencies"

            // Key única para forzar recomposición limpia
            key("questions_safe_${competenceId}_${levelId}_${System.currentTimeMillis()}") {
                QuestionScreenUltraSafe(
                    navController = navController,
                    competencyId = competenceId,
                    levelId = levelId,
                    origin = origin
                )
            }
        }

        // ---------- Progreso ----------
        composable(route = Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }

        // ---------- Perfil ----------
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                onBackClick = {
                    if (!isNavigating) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ---------- Resultados ----------
        composable(
            route = Screen.Results.route,
            arguments = Screen.Results.arguments
        ) { backStackEntry ->
            val competenceId = backStackEntry.arguments?.getInt("competenceId") ?: 0
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 0
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
            val timeSpent = backStackEntry.arguments?.getInt("timeSpent") ?: 0
            val origin = backStackEntry.arguments?.getString("origin") ?: "competencies"


            key("results_${competenceId}_${levelId}_${score}_${System.currentTimeMillis()}") {
                ResultsScreen(
                    navController = navController,
                    competencyId = competenceId,
                    levelId = levelId,
                    score = score,
                    totalQuestions = totalQuestions,
                    timeSpent = timeSpent,
                    origin = origin
                )
            }
        }
    }
}
