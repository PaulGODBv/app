package com.universidad.reta2.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.usecases.GetCompetencesUseCase
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.models.Level
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
    private val progressRepository: ProgressRepository
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

                val competencesWithProgress=updateCompetencesWithRealProgress(competences)

                // Combinar con estadísticas (Flow)
                getUserStatsUseCase().collect { userStats ->
                    if (isViewModelActive) {
                        println("✅ Datos cargados: ${competences.size} competencias, stats: $userStats")

                        _state.update {
                            it.copy(
                                isLoading = false,
                                userStats = userStats,
                                competences = competencesWithProgress,
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

    private suspend fun updateCompetencesWithRealProgress(competences: List<Competence>): List<Competence> {
        return try {
            // Obtener progreso del usuario desde la base de datos
            val userProgress = progressRepository.getUserProgress().first()


            competences.map { competence ->
                // Buscar progreso específico para esta competencia
                val competenceProgress = userProgress.filter { it.competenceId == competence.id }


                // Calcular progreso total basado en niveles completados
                val totalProgress = calculateTotalProgress(competence, competenceProgress)

                // Actualizar niveles con progreso real
                val updatedLevels = updateLevelsWithProgress(competence.levels, competenceProgress)

                competence.copy(
                    totalProgress = totalProgress,
                    levels = updatedLevels
                )
            }
        } catch (e: Exception) {
            competences.map { it.copy(totalProgress = 0f) } // Devolver con progreso 0 en caso de error
        }
    }

    private fun calculateTotalProgress(competence: Competence, progress: List<LevelProgress>): Float {
        if (progress.isEmpty()) return 0f

        // Calcular progreso basado en niveles completados y progreso parcial
        var totalProgress = 0f
        var levelsWithProgress = 0

        competence.levels.forEach { level ->
            val levelProgress = progress.find { it.levelId == level.id }
            if (levelProgress != null) {
                val levelCompletion = if (levelProgress.isCompleted) {
                    1f // Nivel completado = 100%
                } else {
                    // Progreso parcial basado en preguntas respondidas
                    if (levelProgress.totalQuestions > 0) {
                        levelProgress.questionsCompleted.toFloat() / levelProgress.totalQuestions.toFloat()
                    } else {
                        0f
                    }
                }
                totalProgress += levelCompletion
                levelsWithProgress++
            }
        }

        return if (levelsWithProgress > 0) totalProgress / competence.levels.size else 0f
    }

    private fun updateLevelsWithProgress(
        levels: List<Level>,
        progress: List<LevelProgress>
    ): List<Level> {
        return levels.map { level ->
            val levelProgress = progress.find { it.levelId == level.id }
            level.copy(
                isCompleted = levelProgress?.isCompleted ?: false,
                progress = levelProgress?.let {
                    if (it.totalQuestions > 0) {
                        it.questionsCompleted.toFloat() / it.totalQuestions.toFloat()
                    } else {
                        0f
                    }
                } ?: 0f,
                // El primer nivel nunca está bloqueado, los demás dependen del nivel anterior
                isLocked = shouldLevelBeLocked(level.id, levels, progress)
            )
        }
    }

    private fun shouldLevelBeLocked(
        levelId: Int,
        levels: List<Level>,
        progress: List<LevelProgress>
    ): Boolean {
        if (levelId == 1) return false // El primer nivel nunca está bloqueado

        // Buscar nivel anterior
        val previousLevel = levels.find { it.id == levelId - 1 }
        val previousLevelProgress = progress.find { it.levelId == levelId - 1 }

        // El nivel está bloqueado si el anterior no está completado
        return previousLevelProgress?.isCompleted != true
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