package com.universidad.reta2.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Login : Screen("login")

    object Dashboard : Screen("dashboard")
    object Competencies : Screen("competencies")
    object Profile : Screen("profile")

    object CompetenceDetail : Screen("competence_detail/{competenceId}") {
        fun createRoute(competenceId: String) = "competence_detail/$competenceId"
        val arguments = listOf(
            navArgument("competenceId") { type = NavType.StringType }
        )
    }

    object Questions : Screen("questions/{competenceId}/{moduleId}") {
        fun createRoute(competenceId: String, moduleId: String) = "questions/$competenceId/$moduleId"
        val arguments = listOf(
            navArgument("competenceId") { type = NavType.StringType },
            navArgument("moduleId") { type = NavType.StringType }
        )
    }

    object Results : Screen("results/{competenceId}/{moduleId}/{score}/{totalQuestions}/{timeSpent}") {
        fun createRoute(
            competenceId: String,
            moduleId: String,
            score: Int,
            totalQuestions: Int,
            timeSpent: Int
        ) = "results/$competenceId/$moduleId/$score/$totalQuestions/$timeSpent"

        val arguments = listOf(
            navArgument("competenceId") { type = NavType.StringType },
            navArgument("moduleId") { type = NavType.StringType },
            navArgument("score") { type = NavType.IntType },
            navArgument("totalQuestions") { type = NavType.IntType },
            navArgument("timeSpent") { type = NavType.IntType }
        )
    }
}