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
    object TimedMode : Screen("timed_mode") // NUEVO


    object CompetenceDetail : Screen("competence_detail/{competenceId}") {
        fun createRoute(competenceId: Int) = "competence_detail/$competenceId"
        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType }
        )
    }


    object Questions : Screen("questions/{competenceId}/{levelId}/{origin}/{modo}") {
        /**
         * El modo viaja en la ruta y no en un estado compartido a propósito:
         * así una sesión de práctica y una de evaluación del mismo nivel son
         * dos destinos distintos, y volver atrás no deja a medias una sesión
         * en el modo equivocado.
         */
        fun createRoute(
            competenceId: Int,
            levelId: Int,
            origin: String = "competencies",
            modo: String = MODO_EVALUACION
        ) = "questions/$competenceId/$levelId/$origin/$modo"

        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.IntType },
            navArgument("origin") { type = NavType.StringType },
            navArgument("modo") { type = NavType.StringType }
        )

        const val MODO_PRACTICA = "practica"
        const val MODO_EVALUACION = "evaluacion"
    }

    /**
     * Tablero de unir parejas.
     *
     * Destino aparte y no un modo de `Questions` porque no avanza pregunta a
     * pregunta: el nivel entero es un tablero, y meterlo en la pantalla de
     * siempre habria sido un `if` gigante sobre dos flujos que no se parecen.
     */
    object Unir : Screen("unir/{competenceId}/{levelId}") {
        fun createRoute(competenceId: Int, levelId: Int) = "unir/$competenceId/$levelId"
        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.IntType }
        )
    }

    /** Completar el texto arrastrando palabras a sus huecos. */
    object Arrastrar : Screen("arrastrar/{competenceId}/{levelId}") {
        fun createRoute(competenceId: Int, levelId: Int) = "arrastrar/$competenceId/$levelId"
        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.IntType }
        )
    }



    object Results : Screen("results/{competenceId}/{levelId}/{score}/{totalQuestions}/{timeSpent}/{origin}") {
        fun createRoute(
            competenceId: Int,
            levelId: Int,
            score: Int,
            totalQuestions: Int,
            timeSpent: Int,
            origin: String = "competences"
        ) = "results/$competenceId/$levelId/$score/$totalQuestions/$timeSpent/$origin"

        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.IntType },
            navArgument("score") { type = NavType.IntType },
            navArgument("totalQuestions") { type = NavType.IntType },
            navArgument("timeSpent") { type = NavType.IntType },
            navArgument("origin") { type = NavType.StringType }
        )
    }
}
