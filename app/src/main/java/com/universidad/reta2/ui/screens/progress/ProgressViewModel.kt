package com.universidad.reta2.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val isLoading: Boolean = true,
    val userStats: UserStats? = null,
    val progressList: List<LevelProgress> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Flujos
                val progressFlow: Flow<List<LevelProgress>> = progressRepository.getUserProgress()
                val statsFlow: Flow<UserStats> = userStatsRepository.getUserStats()

                // Combina ambos flujos: cada vez que cualquiera emita, recibimos la pareja (progressList, stats)
                progressFlow
                    .combine(statsFlow) { progressList, stats ->
                        Pair(progressList, stats)
                    }
                    .collect { (progressList, stats) ->
                        _uiState.value = ProgressUiState(
                            isLoading = false,
                            userStats = stats,
                            progressList = progressList,
                            error = null
                        )
                    }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar progreso"
                )
            }
        }
    }
}
