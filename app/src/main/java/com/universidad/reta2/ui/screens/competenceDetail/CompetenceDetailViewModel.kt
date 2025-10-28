package com.universidad.reta2.ui.screens.competenceDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetenceDetailViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetenceDetailUiState())
    val uiState: StateFlow<CompetenceDetailUiState> = _uiState.asStateFlow()

    fun loadCompetenceDetail(competenceId: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val competence = competenceRepository.getCompetenceById(competenceId)
                _uiState.value = _uiState.value.copy(
                    competence = competence,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar la competencia"
                )
            }
        }
    }
}

data class CompetenceDetailUiState(
    val competence: Competence? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
