package com.universidad.reta2.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.universidad.reta2.ui.components.LevelXpBar
import com.universidad.reta2.ui.components.UnlockableBadgesGrid

/**
 * Quién eres dentro del juego: tu nivel y tus insignias.
 *
 * QUÉ SE FUE DE AQUÍ, Y POR QUÉ. Hasta ahora esta pantalla era también los
 * ajustes: datos personales, tema, sonido, contraseña y «Sobre Reta2», todo
 * desplegado a la vez, con una barra fija abajo que tenía «Guardar cambios»
 * aunque no hubiera nada que guardar y que cortaba el contenido a media
 * ficha. Eso vive ahora en [com.universidad.reta2.ui.screens.configuracion],
 * plegado y en su propia pestaña.
 *
 * Lo que queda es lo que el estudiante viene a mirar, no a cambiar: cuánto
 * lleva y qué ha desbloqueado. Por eso la cabecera dice **cuánto falta para
 * el nivel siguiente** — sin eso, una pantalla de solo lectura no da ninguna
 * razón para volver.
 *
 * No se confunde con Progreso: allí se ve el avance por competencia, la
 * semana y el ranking; aquí, el nivel y las insignias.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        CabeceraDelEstudiante(nombre = state.username)

        LevelXpBar(totalQuestionsAnswered = state.totalQuestionsAnswered)

        UnlockableBadgesGrid(
            totalQuestionsAnswered = state.totalQuestionsAnswered,
            mejorRacha = state.maxStreak
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * El avatar y el nombre, centrados.
 *
 * Con iniciales y no con foto: la aplicación no pide ninguna, y un hueco gris
 * esperando una imagen que nunca llega se ve peor que una inicial.
 */
@Composable
private fun CabeceraDelEstudiante(nombre: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = inicialesDe(nombre),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(
            text = nombre.ifBlank { "Estudiante" },
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/** «Paul Contreras» → «PC». Una sola palabra da una sola letra. */
private fun inicialesDe(nombre: String): String {
    val palabras = nombre.trim().split(" ").filter { it.isNotBlank() }
    return when {
        palabras.isEmpty() -> "?"
        palabras.size == 1 -> palabras[0].take(1).uppercase()
        else -> (palabras[0].take(1) + palabras[1].take(1)).uppercase()
    }
}
