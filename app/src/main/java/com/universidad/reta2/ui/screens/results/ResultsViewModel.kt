package com.universidad.reta2.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
    private val competenceRepository: CompetenceRepository
) : ViewModel() {

    private val _competenceState = MutableStateFlow<Competence?>(null)
    val competenceState: StateFlow<Competence?> = _competenceState.asStateFlow()

    private val _levelState = MutableStateFlow<Level?>(null)
    val levelState: StateFlow<Level?> = _levelState.asStateFlow()
    private var hasUpdatedProgress = false
    private var lastUpdateKey=""
    /**
     * Actualiza el progreso del usuario en un nivel específico.
     */

    fun loadData(competenceId: Int, levelId: Int) {
        // Evitar recargar si ya los tenemos
        if (_competenceState.value != null && _levelState.value != null) return

        viewModelScope.launch {
            try {
                val competence = competenceRepository.getCompetenceById(competenceId)
                val level = competence?.levels?.firstOrNull { it.id == levelId }
                _competenceState.update { competence }
                _levelState.update { level }
            } catch (e: Exception) {
                _competenceState.update { null }
                _levelState.update { null }
            }
        }
    }
    fun updateUserProgress(competenceId: Int, levelId: Int, score: Int, totalQuestions: Int) {
        val updateKey = "$competenceId-$levelId-$score-$totalQuestions"

        // La lógica de 'hasUpdatedProgress' útil para
        // evitar cualquier lógica futura que se ejecute varias veces.
        if (hasUpdatedProgress && lastUpdateKey == updateKey) {
            println("ResultViewModel: updateUserProgress ya ejecutado, omitiendo.")
            return
        }

        println("ResultViewModel: updateUserProgress ejecutado (no hace nada).")


        hasUpdatedProgress = true
        lastUpdateKey = updateKey
    }


}
