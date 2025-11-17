package com.universidad.reta2.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.usecases.GetCompetencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressState(
    val isLoading: Boolean = true,
    val userStats: UserStats? = null,
    val competences: List<Competence> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getCompetencesUseCase: GetCompetencesUseCase,
) : ViewModel() {

    // 🔒 ESTADO SEGURO CON PROTECCIONES
    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    private var isViewModelActive = true

    init {
        println("🔧 ProgressViewModel INIT")
        loadProgressData()
    }

    // 🔒 ACTIVACIÓN SEGURA
    fun activate() {
        println("✅ Activando ProgressViewModel")
        isViewModelActive = true
    }

    // 🔒 CARGA DE DATOS CON PROTECCIÓN
    fun loadProgressData() {
        if (!isViewModelActive) {
            println("⚠️ ViewModel no activo, ignorando carga")
            return
        }

        viewModelScope.launch {
            try {
                println("🔄 Cargando datos de progreso...")

                _state.update { it.copy(isLoading = true, error = null) }

                // Cargar competencias (suspend)
                val competences = getCompetencesUseCase()


                // Combinar con estadísticas (Flow)
                getUserStatsUseCase().collect { userStats ->
                    if (isViewModelActive) {
                        println("✅ Datos cargados: ${competences.size} competencias, stats: $userStats")

                        _state.update {
                            it.copy(
                                isLoading = false,
                                userStats = userStats,
                                competences = competences,
                                error = null
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                if (isViewModelActive) {
                    println("❌ Error cargando progreso: ${e.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al cargar datos",
                            competences = emptyList()
                        )
                    }
                }
            }
        }
    }


    // 🔒 FORMATO SEGURO DE TIEMPO
    fun getFormattedPracticeTime(): String {
        val totalSeconds = state.value.userStats?.dailyPracticeTime ?: 0
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // 🔒 LIMPIEZA SEGURA
    fun cleanup() {
        println("🧹 Limpiando ProgressViewModel")
        isViewModelActive = false
    }

    override fun onCleared() {
        println("🚮 ProgressViewModel siendo destruido")
        super.onCleared()
        isViewModelActive = false
    }
}