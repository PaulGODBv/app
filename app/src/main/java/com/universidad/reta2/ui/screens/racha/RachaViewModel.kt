package com.universidad.reta2.ui.screens.racha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Los datos del latido de racha: cuántos días lleva y cómo va la semana.
 *
 * Lee **una sola vez** y no se suscribe a nada. La pantalla dura dos segundos
 * y lo que enseña es una foto de un instante concreto —el de acabar de
 * practicar—; si los datos cambiaran por debajo mientras corre la animación,
 * la barra daría un salto a mitad de camino.
 */
@HiltViewModel
class RachaViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RachaUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * @param preguntasDeLaSesion las que se acaban de responder. Hacen falta
     *   para saber de dónde arranca la barra de hoy: el total del día ya las
     *   incluye, así que la altura anterior es la diferencia.
     */
    fun cargar(preguntasDeLaSesion: Int) {
        if (_uiState.value.cargado) return
        viewModelScope.launch {
            runCatching {
                val stats = userStatsRepository.getUserStatsOnce()
                val semana = userStatsRepository.getWeeklyProgress()
                _uiState.update {
                    it.copy(
                        cargado = true,
                        racha = stats.currentStreakDays,
                        semana = semana,
                        preguntasDeLaSesion = preguntasDeLaSesion
                    )
                }
            }.onFailure {
                // Sin datos no hay nada que celebrar, pero tampoco hay que
                // atascar al estudiante: la pantalla se da por cargada y el
                // botón de continuar sigue llevando a resultados.
                println("⚠️ No se pudieron leer los datos de la racha: ${it.message}")
                _uiState.update { s -> s.copy(cargado = true) }
            }
        }
    }

    data class RachaUiState(
        val cargado: Boolean = false,
        /** Días de racha ya contando el de hoy. */
        val racha: Int = 0,
        val semana: List<DailyProgress> = emptyList(),
        val preguntasDeLaSesion: Int = 0
    ) {
        /**
         * El número del que arranca la cuenta. La pantalla solo sale el día
         * que la racha sube, así que el anterior es siempre uno menos.
         */
        val rachaAnterior: Int get() = (racha - 1).coerceAtLeast(0)
    }
}
