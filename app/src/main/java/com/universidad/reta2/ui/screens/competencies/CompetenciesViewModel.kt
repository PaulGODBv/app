package com.universidad.reta2.ui.screens.competencies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import com.universidad.reta2.R

@HiltViewModel
class CompetenciesViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetenceUiState())
    val uiState: StateFlow<CompetenceUiState> = _uiState

    init {
        loadCompetences()
        observeProgressUpdates()
    }

    fun loadCompetences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val competences = competenceRepository.getAllCompetences()

                val competencesWithProgress = updateCompetencesWithRealProgress(competences)

                _uiState.value = CompetenceUiState(
                    competences = competencesWithProgress,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Error al cargar las competencias"
                )
            }
        }
    }

    private fun observeProgressUpdates() {
        viewModelScope.launch {
            progressRepository.getUserProgress().collect { progressList ->
                val currentCompetences = _uiState.value.competences
                if (currentCompetences.isNotEmpty()) {
                    val updatedCompetences = updateCompetencesWithRealProgress(currentCompetences)
                    _uiState.value = _uiState.value.copy(competences = updatedCompetences)
                }
            }
        }
    }

    // 🔥 NUEVO: ACTUALIZAR COMPETENCIAS CON PROGRESO ACTUAL
    private suspend fun updateCompetencesWithRealProgress(competences: List<Competence>): List<Competence> {
        return try {
            val progressList = progressRepository.getUserProgress().first()

            competences.map { competence ->
                val competenceProgress = progressList.filter { it.competenceId == competence.id }

                // Calcular progreso total
                val totalProgress = calculateTotalProgress(competence, competenceProgress)

                competence.copy(
                    totalProgress = totalProgress.coerceIn(0f, 1f)
                )
            }
        } catch (e: Exception) {
            println(" Error actualizando progreso: ${e.message}")
            competences.map { it.copy(totalProgress = 0f) }
        }
    }
}

private fun calculateTotalProgress(competence: Competence, progress: List<LevelProgress>): Float {
    if (progress.isEmpty()) return 0f

    var totalProgress = 0f
    var levelsWithProgress = 0

    competence.levels.forEach { level ->
        val levelProgress = progress.find { it.levelId == level.id }
        if (levelProgress != null) {
            val levelCompletion = if (levelProgress.isCompleted) {
                1f
            } else {
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


data class CompetenceUiState(
    val competences: List<Competence> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
