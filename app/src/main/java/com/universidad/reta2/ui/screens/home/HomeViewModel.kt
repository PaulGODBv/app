package com.universidad.reta2.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val progressRepository: ProgressRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _competences = MutableStateFlow<List<Competence>>(emptyList())
    val competences: StateFlow<List<Competence>> = _competences.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
        loadCompetences()
        loadUserStats()
        // 🔥 OBSERVAR CAMBIOS EN PROGRESO
        observeProgressUpdates()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _userName.value = SessionManager.getCurrentUsername(context) ?: "Usuario"
        }
    }

    private fun loadCompetences() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val loadedCompetences = competenceRepository.getAllCompetences()

                val competencesWithProgress = updateCompetencesWithRealProgress(loadedCompetences)

                _competences.value = competencesWithProgress
            } catch (e: Exception) {
                _competences.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                getUserStatsUseCase().collectLatest { stats ->
                    _userStats.value = stats
                }
            } catch (e: Exception) {
                _userStats.value = UserStats()
            }
        }
    }

    // 🔥 NUEVO: OBSERVAR ACTUALIZACIONES DE PROGRESO
    private fun observeProgressUpdates() {
        viewModelScope.launch {
            progressRepository.getUserProgress().collect { progressList ->
                // Actualizar competencias con progreso actualizado
                val currentCompetences = _competences.value
                if (currentCompetences.isNotEmpty()) {
                    _competences.value = updateCompetencesWithRealProgress(currentCompetences)
                }

                // Actualizar estadísticas si es necesario
                loadUserStats()
            }
        }
    }

    // 🔥 NUEVO: ACTUALIZAR COMPETENCIAS CON PROGRESO
    private suspend fun updateCompetencesWithRealProgress(competences: List<Competence>): List<Competence> {
        return try {
            val progressList = progressRepository.getUserProgress().first()

            competences.map { competence ->
                val competenceProgress = progressList.filter { it.competenceId == competence.id }

                val totalProgress = calculateTotalProgress(competence, competenceProgress)

                println("🏠 Competencia ${competence.id} - Progreso: ${(totalProgress * 100).toInt()}%")

                competence.copy(
                    totalProgress = totalProgress.coerceIn(0f, 1f)
                )
            }
        } catch (e: Exception) {
            println("❌ Home - Error calculando progreso: ${e.message}")
            competences.map { it.copy(totalProgress = 0f) }
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

    fun getCompetencesWithProgress(): List<Competence> {
        return _competences.value.filter { competence ->
            competence.levels.any { level ->
                !level.isLocked && (level.isCompleted || level.progress > 0f)
            }
        }
    }

    fun getCompletedCompetencesCount(): Int {
        return _competences.value.count { it.totalProgress >= 1f }
    }
}