package com.universidad.reta2.ui.screens.competenceDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.LevelRules
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CompetenceDetailViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompetenceDetailUiState())
    val uiState: StateFlow<CompetenceDetailUiState> = _uiState.asStateFlow()

    fun loadCompetenceDetail(competenceId: Int) {
        viewModelScope.launch {
            try {
                println("🔄 Cargando detalle de competencia $competenceId")
                val competence = competenceRepository.getCompetenceById(competenceId)

                // 🔥 DIAGNÓSTICO: Ver estado actual de niveles
                competence?.levels?.forEach { level ->
                    println("   🔍 Nivel ${level.id}: '${level.name}'")
                    println("      - Locked: ${level.isLocked}")
                    println("      - Completed: ${level.isCompleted}")
                    println("      - Progress: ${level.progress}")
                }

                // El nivel de práctica solo se enseña si tiene ítems. Los
                // cuatro existen desde que se creó el mecanismo, pero poblarlos
                // es trabajo de contenido que va aparte: así el que esté vacío
                // no aparece, y aparece solo en cuanto lo llenen desde el
                // panel, sin publicar una versión nueva de la app.
                val practica = competence?.levels?.firstOrNull { LevelRules.esDePractica(it.id) }
                val practicaConItems = practica != null &&
                    runCatching {
                        questionRepository.getQuestionCount(competenceId, practica.id)
                    }.getOrDefault(0) > 0

                _uiState.update {
                    it.copy(
                        competence = competence,
                        practicaConItems = practicaConItems,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                println("❌ Error cargando competencia: ${e.message}")
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}

data class CompetenceDetailUiState(
    val competence: Competence? = null,
    /** Si hay nivel de práctica y además tiene preguntas que ofrecer. */
    val practicaConItems: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

