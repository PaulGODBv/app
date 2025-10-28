package com.universidad.reta2.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase,
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
                _competences.value = competenceRepository.getAllCompetences()
            } catch (e: Exception) {
                // Manejar error
                _competences.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                // Usar collectLatest para obtener solo el último valor
                getUserStatsUseCase().collectLatest { stats ->
                    _userStats.value = stats
                }
            } catch (e: Exception) {
                // En caso de error, usar stats por defecto
                _userStats.value = UserStats()
            }
        }
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