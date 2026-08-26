package com.universidad.reta2.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.usecases.GetCompetencesUseCase
import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.data.repositories.RankingRepository
import com.universidad.reta2.data.remote.dto.RankingResponse
import com.universidad.reta2.data.preferences.SessionManager
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressState(
    val isLoading: Boolean = true,
    val userStats: UserStats? = null,
    val competences: List<Competence> = emptyList(),
    val weeklyProgress: List<DailyProgress> = emptyList(),
    val ranking: RankingResponse? = null,
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getCompetencesUseCase: GetCompetencesUseCase,
    private val userStatsRepository: UserStatsRepository,
    private val rankingRepository: RankingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ESTADO SEGURO CON PROTECCIONES
    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    private var isViewModelActive = true

    init {
        println(" ProgressViewModel INIT")
        // Reset explícito del estado al iniciar
        _state.value = ProgressState(isLoading = true)
        loadProgressData()
    }

    // ACTIVACIÓN SEGURA
    fun activate() {
        println(" Activando ProgressViewModel")
        isViewModelActive = true
    }

    // CARGA DE DATOS CON PROTECCIÓN
    fun loadProgressData() {
        if (!isViewModelActive) {
            println(" ViewModel no activo, ignorando carga")
            return
        }

        viewModelScope.launch {
            try {
                println(" ProgressViewModel: Iniciando carga de datos...")

                _state.update { it.copy(isLoading = true, error = null) }

                val username = SessionManager.getCurrentUsername(context) ?: ""

                // 1. Cargar estadísticas de forma reactiva (Inicia de inmediato)
                val statsJob = launch {
                    getUserStatsUseCase().collect { userStats ->
                        if (isViewModelActive) {
                            println(" ProgressViewModel: Stats actualizados recibidos")
                            _state.update {
                                it.copy(
                                    userStats = userStats,
                                    isLoading = false, // Quitar loading al recibir los primeros stats
                                    error = null
                                )
                            }
                        }
                    }
                }

                // 2. Cargar datos locales pesados en segundo plano
                launch {
                    try {
                        val competences = getCompetencesUseCase()
                        val weeklyProgress = userStatsRepository.getWeeklyProgress()
                        if (isViewModelActive) {
                            _state.update {
                                it.copy(
                                    competences = competences,
                                    weeklyProgress = weeklyProgress
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("⚠️ Error cargando datos locales: ${e.message}")
                    }
                }

                // 3. Cargar Ranking en segundo plano (No bloquea el resto)
                if (username.isNotEmpty()) {
                    launch {
                        try {
                            val rankingResult = rankingRepository.getGlobalRanking(username).getOrNull()
                            if (rankingResult != null && isViewModelActive) {
                                println(" ProgressViewModel: Ranking cargado exitosamente")
                                _state.update { it.copy(ranking = rankingResult) }
                            }
                        } catch (e: Exception) {
                            println("⚠️ Error cargando ranking: ${e.message}")
                        }
                    }
                }

            } catch (e: Exception) {
                if (isViewModelActive) {
                    println(" ❌ Error crítico en ProgressViewModel: ${e.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al cargar datos"
                        )
                    }
                }
            }
        }
    }

    // FORMATO SEGURO DE TIEMPO
    fun getFormattedPracticeTime(): String {
        val totalSeconds = state.value.userStats?.dailyPracticeTime ?: 0
        return when {
            totalSeconds >= 3600 -> {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                "$hours hr $minutes min $seconds seg"
            }
            totalSeconds >= 60 -> {
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                "$minutes min $seconds seg"
            }
            else -> "$totalSeconds seg"
        }
    }

    // LIMPIEZA SEGURA
    fun cleanup() {
        println(" Limpiando ProgressViewModel")
        isViewModelActive = false
    }

    override fun onCleared() {
        println(" ProgressViewModel siendo destruido")
        super.onCleared()
        isViewModelActive = false
    }
}