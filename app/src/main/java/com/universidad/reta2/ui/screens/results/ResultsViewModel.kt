package com.universidad.reta2.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
    private val competenceRepository: CompetenceRepository
) : ViewModel() {

    /**
     * Actualiza el progreso del usuario en un nivel específico.
     */
    fun updateUserProgress(competenceId: Int, levelId: Int, score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            try {
                // Calcular porcentaje de progreso
                val progress = if (totalQuestions > 0) score.toFloat() / totalQuestions else 0f

                // Incrementar estadísticas globales
                userStatsRepository.addQuestionsAnswered(totalQuestions)
                userStatsRepository.addPracticeTime(60) // Simulamos 60s de práctica
                userStatsRepository.incrementStreak()

                // Actualizar progreso del nivel
                userStatsRepository.updateLevelProgress(competenceId, levelId, progress)

            } catch (e: Exception) {
                e.printStackTrace()
                println("⚠️ Error al actualizar progreso del usuario: ${e.message}")
            }
        }
    }


    /**
     * Devuelve la competencia con todos sus niveles.
     */
    fun getCompetency(competenceId: Int): Flow<Competence?> = flow {
        try {
            val competence = competenceRepository.getCompetenceById(competenceId)
            emit(competence)
        } catch (e: Exception) {
            emit(null)
        }
    }

    /**
     * Devuelve un nivel específico de una competencia.
     */
    fun getLevel(competenceId: Int, levelId: Int): Flow<Level?> = flow {
        try {
            val competence = competenceRepository.getCompetenceById(competenceId)
            val level = competence?.levels?.firstOrNull { it.id == levelId }
            emit(level)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
