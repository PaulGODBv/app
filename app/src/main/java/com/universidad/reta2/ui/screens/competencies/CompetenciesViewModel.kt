package com.universidad.reta2.ui.screens.competencies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
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
    private val progressRepository: ProgressRepository // 🔥 AGREGAR ESTO
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetenceUiState())
    val uiState: StateFlow<CompetenceUiState> = _uiState

    init {
        loadCompetences()
        // 🔥 OBSERVAR CAMBIOS EN EL PROGRESO
        observeProgressUpdates()
    }

    fun loadCompetences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val competences = competenceRepository.getAllCompetences()

                // 🔥 ACTUALIZAR COMPETENCIAS CON PROGRESO EN TIEMPO REAL
                val competencesWithProgress = updateCompetencesWithProgress(competences)

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

    // 🔥 NUEVO: OBSERVAR ACTUALIZACIONES DE PROGRESO
    private fun observeProgressUpdates() {
        viewModelScope.launch {
            progressRepository.getUserProgress().collect { progressList ->
                val currentCompetences = _uiState.value.competences
                if (currentCompetences.isNotEmpty()) {
                    val updatedCompetences = updateCompetencesWithProgress(currentCompetences)
                    _uiState.value = _uiState.value.copy(competences = updatedCompetences)
                }
            }
        }
    }

    // 🔥 NUEVO: ACTUALIZAR COMPETENCIAS CON PROGRESO ACTUAL
    private suspend fun updateCompetencesWithProgress(competences: List<Competence>): List<Competence> {
        val progressList = progressRepository.getUserProgress().first()

        return competences.map { competence ->
            val competenceProgress = progressList.filter { it.competenceId == competence.id }
            val totalProgress = if (competenceProgress.isNotEmpty()) {
                competenceProgress.sumOf { it.questionsCompleted }.toFloat() /
                        competenceProgress.sumOf { it.totalQuestions }.toFloat()
            } else {
                0f
            }

            competence.copy(totalProgress = totalProgress.coerceIn(0f, 1f))
        }
    }
}

data class CompetenceUiState(
    val competences: List<Competence> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
