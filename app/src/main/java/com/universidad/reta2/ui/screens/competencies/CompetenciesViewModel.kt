package com.universidad.reta2.ui.screens.competencies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.usecases.GetCompetencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetenciesViewModel @Inject constructor(
    private val getCompetencesUseCase: GetCompetencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetenciesUiState())
    val uiState: StateFlow<CompetenciesUiState> = _uiState.asStateFlow()

    fun loadCompetencies() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val competencies = getCompetencesUseCase()
                _uiState.value = _uiState.value.copy(
                    competencies = competencies,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar competencias"
                )
            }
        }
    }
}

data class CompetenciesUiState(
    val competencies: List<Competence> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)