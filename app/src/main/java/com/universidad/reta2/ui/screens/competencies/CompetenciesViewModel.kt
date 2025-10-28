package com.universidad.reta2.ui.screens.competencies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetenciesViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetenceUiState())
    val uiState: StateFlow<CompetenceUiState> = _uiState

    init {
        loadCompetences()
    }

    fun loadCompetences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val competences = competenceRepository.getAllCompetences()
                _uiState.value = CompetenceUiState(
                    competences = competences,
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
}

data class CompetenceUiState(
    val competences: List<Competence> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
