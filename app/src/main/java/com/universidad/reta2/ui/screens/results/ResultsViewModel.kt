package com.universidad.reta2.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.local.RepasoDeSesion
import com.universidad.reta2.data.local.RespuestaDeSesion
import com.universidad.reta2.data.repositories.SyncRepository
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
    private val competenceRepository: CompetenceRepository,
    private val syncRepository: SyncRepository,
    private val repasoDeSesion: RepasoDeSesion
) : ViewModel() {

    private val _competenceState = MutableStateFlow<Competence?>(null)
    val competenceState: StateFlow<Competence?> = _competenceState.asStateFlow()

    private val _levelState = MutableStateFlow<Level?>(null)
    val levelState: StateFlow<Level?> = _levelState.asStateFlow()

    /**
     * Las preguntas de la sesion con lo que se respondio en cada una.
     *
     * Es lo que alimenta el repaso con explicacion. Viene de un registro en
     * memoria porque a esta pantalla solo llegan numeros por la ruta; si se
     * entra por otro camino la lista sale vacia y el bloque no se pinta.
     */
    private val _repaso = MutableStateFlow<List<RespuestaDeSesion>>(emptyList())
    val repaso: StateFlow<List<RespuestaDeSesion>> = _repaso.asStateFlow()

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    private var hasUpdatedProgress = false
    private var lastUpdateKey = ""

    fun loadData(competenceId: Int, levelId: Int) {
        // El repaso se lee siempre, aunque el catalogo ya estuviera cargado:
        // es lo unico que cambia entre dos visitas seguidas a esta pantalla.
        _repaso.value = repasoDeSesion.obtener(levelId)

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

    fun syncProgress() {
        // Evitar sincronizar múltiples veces
        if (_syncState.value is SyncUiState.Loading) return

        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading

            val result = syncRepository.syncToServer()

            _syncState.value = if (result.isSuccess) {
                println("✅ Sync desde ResultsScreen exitoso")
                SyncUiState.Success(result.getOrNull() ?: "Sincronizado")
            } else {
                println("⚠️ Sync desde ResultsScreen falló: ${result.exceptionOrNull()?.message}")
                SyncUiState.Error(result.exceptionOrNull()?.message ?: "Error de sincronización")
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

sealed class SyncUiState {
    object Idle : SyncUiState()
    object Loading : SyncUiState()
    data class Success(val message: String) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}
