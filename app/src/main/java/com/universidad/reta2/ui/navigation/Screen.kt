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



    /**
     * El latido de la racha, entre terminar un nivel y ver los resultados.
     *
     * Lleva los mismos argumentos que [Results] porque su unico destino es
     * Results: no los usa, los reenvia. Pasarlos asi evita tener que guardar
     * el resultado en algun sitio mientras se ensena la animacion.
     *
     * **Solo aparece el dia que la racha sube**, que es una vez al dia. Del
     * segundo nivel en adelante se va directo a Results: una celebracion que
     * se repite sin motivo deja de celebrar.
     */
    /**
     * El `practica` de esta ruta y de la de resultados va como argumento de
     * consulta —con `?`— y no como tramo: asi las rutas viejas sin el
     * parametro siguen resolviendo, y no hay que tocar cada sitio que navegue.
     *
     * Viaja hasta el final del recorrido porque la pantalla de resultados
     * tiene que saberlo. Sin el ofrecia «Siguiente nivel» despues de una
     * practica, y practicar no desbloquea nada: el boton llevaba al nivel
     * siguiente sin que estuviera abierto.
     */
    object Racha : Screen("racha/{competenceId}/{levelId}/{score}/{totalQuestions}/{timeSpent}/{origin}?practica={practica}") {
        fun createRoute(
            competenceId: Int,
            levelId: Int,
            score: Int,
            totalQuestions: Int,
            timeSpent: Int,
            origin: String = "competences",
            esPractica: Boolean = false
        ) = "racha/$competenceId/$levelId/$score/$totalQuestions/$timeSpent/$origin?practica=$esPractica"

        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.IntType },
            navArgument("score") { type = NavType.IntType },
            navArgument("totalQuestions") { type = NavType.IntType },
            navArgument("timeSpent") { type = NavType.IntType },
            navArgument("origin") { type = NavType.StringType },
            navArgument("practica") { type = NavType.BoolType; defaultValue = false }
        )
    }

    object Results : Screen("results/{competenceId}/{levelId}/{score}/{totalQuestions}/{timeSpent}/{origin}?practica={practica}") {
        fun createRoute(
            competenceId: Int,
            levelId: Int,
            score: Int,
            totalQuestions: Int,
            timeSpent: Int,
            origin: String = "competences",
            esPractica: Boolean = false
        ) = "results/$competenceId/$levelId/$score/$totalQuestions/$timeSpent/$origin?practica=$esPractica"

        val arguments = listOf(
            navArgument("competenceId") { type = NavType.IntType },
            navArgument("levelId") { type = NavType.IntType },
            navArgument("score") { type = NavType.IntType },
            navArgument("totalQuestions") { type = NavType.IntType },
            navArgument("timeSpent") { type = NavType.IntType },
            navArgument("origin") { type = NavType.StringType },
            navArgument("practica") { type = NavType.BoolType; defaultValue = false }
        )
    }
}
