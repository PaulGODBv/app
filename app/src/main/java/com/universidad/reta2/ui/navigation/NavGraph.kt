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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import com.universidad.reta2.ui.screens.racha.RachaScreen
import com.universidad.reta2.ui.screens.login.LoginScreen
import com.universidad.reta2.ui.screens.competencies.CompetenciesScreen
import com.universidad.reta2.ui.screens.competenceDetail.CompetenceDetailScreen
import com.universidad.reta2.ui.screens.profile.ProfileScreen
import com.universidad.reta2.ui.screens.registration.RegistrationScreen
import com.universidad.reta2.ui.screens.progress.ProgressScreen
import com.universidad.reta2.ui.screens.splash.SplashScreen
import com.universidad.reta2.ui.screens.home.HomeScreen
import com.universidad.reta2.ui.screens.timedmode.TimedModeScreen
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.ui.screens.results.ResultsScreen
import com.universidad.reta2.ui.screens.arrastrar.ArrastrarScreen
import com.universidad.reta2.ui.screens.unir.UnirParejasScreen
import com.universidad.reta2.ui.screens.questions.QuestionScreenUltraSafe
import kotlinx.coroutines.delay

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    //  PROTECCIÓN GLOBAL CONTRA MÚLTIPLES NAVEGACIONES
    var isNavigating by remember { mutableStateOf(false) }

    //  EFFECT PARA RESETEAR EL ESTADO DE NAVEGACIÓN
    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            delay(500) // Esperar 500ms después de navegar
            isNavigating = false
        }
    }

    // Se pregunta una vez: apagar las animaciones del sistema exige salir de
    // la aplicacion, asi que no hay nada que vigilar.
    val contexto = LocalContext.current
    val animar = remember { Transiciones.animacionesActivas(contexto) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        // Por defecto, bajar en la jerarquia: la pantalla nueva entra desde la
        // derecha y el boton de atras la saca por donde entro. Las pantallas
        // que no son un "dentro" de nada —las pestanas de abajo, el arranque—
        // lo sustituyen abajo por un fundido.
        enterTransition = Transiciones.entrarDesdeLaDerecha(animar),
        exitTransition = Transiciones.salirHaciaLaIzquierda(animar),
        popEnterTransition = Transiciones.volverDesdeLaIzquierda(animar),
        popExitTransition = Transiciones.salirHaciaLaDerecha(animar)
    ) {
        // Splash
        composable(
            route = Screen.Splash.route,
            enterTransition = Transiciones.fundidoEntrada(animar),
            exitTransition = Transiciones.fundidoSalida(animar)
        ) {
            SplashScreen(navController = navController)
        }

        // ---------- Login ----------
        // El arranque no es una escalera: del splash al login, y del login al
        // registro, no se esta bajando a ningun sitio. Se funden.
        composable(
            route = Screen.Login.route,
            enterTransition = Transiciones.fundidoEntrada(animar),
            exitTransition = Transiciones.fundidoSalida(animar),
            popEnterTransition = Transiciones.fundidoEntrada(animar),
            popExitTransition = Transiciones.fundidoSalida(animar)
        ) {
            LoginScreen(navController = navController)
        }

        // ---------- Registro ----------
        composable(
            route = Screen.Registration.route,
            enterTransition = Transiciones.fundidoEntrada(animar),
            exitTransition = Transiciones.fundidoSalida(animar),
            popEnterTransition = Transiciones.fundidoEntrada(animar),
            popExitTransition = Transiciones.fundidoSalida(animar)
        ) {
            RegistrationScreen(navController = navController)
        }

        // ---------- Home ----------
        // Pestana de la barra de abajo: fundido en las cuatro direcciones.
        // Deslizar entre hermanas insinuaria un orden que no existe.
        composable(
            route = Screen.Home.route,
            enterTransition = Transiciones.fundidoEntrada(animar),
            exitTransition = Transiciones.fundidoSalida(animar),
            popEnterTransition = Transiciones.fundidoEntrada(animar),
            popExitTransition = Transiciones.fundidoSalida(animar)
        ) {
            HomeScreen(navController = navController)
        }

        // ---------- Timed Mode ----------
        composable(route = Screen.TimedMode.route) {
            TimedModeScreen(navController = navController)
        }

        // ---------- Competencias ----------
        composable(
            route = Screen.Competencies.route,
            enterTransition = Transiciones.fundidoEntrada(animar),
            popEnterTransition = Transiciones.fundidoEntrada(animar),
            // La salida NO se funde: desde aqui se baja a una competencia, y
            // esa si es una escalera. Se deja la de por defecto.
        ) {
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
                onLevelClick = { levelId, modo, formato ->
                    if (!isNavigating) {
                        val practicando = modo == Screen.Questions.MODO_PRACTICA
                        val target = if (practicando && formato == Level.FORMATO_UNIR) {
                            Screen.Unir.createRoute(competenceId, levelId)
                        } else if (practicando && formato == Level.FORMATO_ARRASTRAR) {
                            Screen.Arrastrar.createRoute(competenceId, levelId)
                        } else {
                            Screen.Questions.createRoute(
                                competenceId = competenceId,
                                levelId = levelId,
                                origin = origin,
                                modo = modo
                            )
                        }
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

        // ---------- Unir parejas ----------
        composable(
            route = Screen.Unir.route,
            arguments = Screen.Unir.arguments
        ) { backStackEntry ->
            UnirParejasScreen(
                navController = navController,
                competenceId = backStackEntry.arguments?.getInt("competenceId") ?: 0,
                levelId = backStackEntry.arguments?.getInt("levelId") ?: 0
            )
        }

        // ---------- Completar arrastrando ----------
        composable(
            route = Screen.Arrastrar.route,
            arguments = Screen.Arrastrar.arguments
        ) { backStackEntry ->
            ArrastrarScreen(
                navController = navController,
                competenceId = backStackEntry.arguments?.getInt("competenceId") ?: 0,
                levelId = backStackEntry.arguments?.getInt("levelId") ?: 0
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
            val modo = backStackEntry.arguments?.getString("modo")
                ?: Screen.Questions.MODO_EVALUACION

            // Key única para forzar recomposición limpia. Incluye el modo: la
            // misma pantalla del mismo nivel en práctica y en evaluación no
            // debe reutilizar estado.
            key("questions_safe_${competenceId}_${levelId}_${modo}") {
                QuestionScreenUltraSafe(
                    navController = navController,
                    competencyId = competenceId,
                    levelId = levelId,
                    origin = origin,
                    esPractica = modo == Screen.Questions.MODO_PRACTICA
                )
            }
        }

        // ---------- Progreso ----------
        composable(
            route = Screen.Progress.route,
            enterTransition = Transiciones.fundidoEntrada(animar),
            popEnterTransition = Transiciones.fundidoEntrada(animar)
        ) {
            ProgressScreen(navController = navController)
        }

        // ---------- Perfil ----------
        composable(
            route = Screen.Profile.route,
            enterTransition = Transiciones.fundidoEntrada(animar),
            exitTransition = Transiciones.fundidoSalida(animar),
            popEnterTransition = Transiciones.fundidoEntrada(animar),
            popExitTransition = Transiciones.fundidoSalida(animar)
        ) {
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
            route = Screen.Racha.route,
            arguments = Screen.Racha.arguments
        ) { backStackEntry ->
            RachaScreen(
                navController = navController,
                competencyId = backStackEntry.arguments?.getInt("competenceId") ?: 0,
                levelId = backStackEntry.arguments?.getInt("levelId") ?: 0,
                score = backStackEntry.arguments?.getInt("score") ?: 0,
                totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0,
                timeSpent = backStackEntry.arguments?.getInt("timeSpent") ?: 0,
                origin = backStackEntry.arguments?.getString("origin") ?: "competencies",
                esPractica = backStackEntry.arguments?.getBoolean("practica") ?: false
            )
        }

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
            val esPractica = backStackEntry.arguments?.getBoolean("practica") ?: false

            key("results_${competenceId}_${levelId}_${score}") {
                ResultsScreen(
                    navController = navController,
                    competencyId = competenceId,
                    levelId = levelId,
                    score = score,
                    totalQuestions = totalQuestions,
                    timeSpent = timeSpent,
                    origin = origin,
                    esPractica = esPractica
                )
            }
        }
    }
}
