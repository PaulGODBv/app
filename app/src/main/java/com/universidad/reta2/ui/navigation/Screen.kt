package com.universidad.reta2.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Competencies : Screen("competencies")
    object Profile : Screen("profile")
    object Progress : Screen("progress")
    object Splash:Screen("splash")
    object Home:Screen("home")


    object CompetenceDetail : Screen("competence_detail/{competenceId}") {
        fun createRoute(competenceId: Int) = "competence_detail/$competenceId"
        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType }
        )
    }


    object Questions : Screen("questions/{competenceId}/{levelId}") {
        fun createRoute(competenceId: Int, levelId: Int) = "questions/$competenceId/$levelId"
        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.IntType }
        )
    }


    object Results : Screen("results/{competenceId}/{levelId}/{score}/{totalQuestions}/{timeSpent}") {
        fun createRoute(
            competenceId: Int,
            levelId: String,
            score: Int,
            totalQuestions: Int,
            timeSpent: Int
        ) = "results/$competenceId/$levelId/$score/$totalQuestions/$timeSpent"

        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.StringType },
            navArgument("score") { type = NavType.IntType },
            navArgument("totalQuestions") { type = NavType.IntType },
            navArgument("timeSpent") { type = NavType.IntType }
        )
    }
}