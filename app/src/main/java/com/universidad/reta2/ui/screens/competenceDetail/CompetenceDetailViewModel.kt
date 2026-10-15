package com.universidad.reta2.ui.screens.competenceDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.LevelRules
import com.universidad.reta2.domain.models.Level
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
            // Tambien al recargar: si se vuelve a esta pantalla con otra
            // competencia, el estado trae la anterior y sin esto se enseñaria
            // la vieja mientras llega la nueva.
            _uiState.update { it.copy(isLoading = true, error = null) }
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
                val tieneItems = practica != null &&
                    runCatching {
                        questionRepository.getQuestionCount(competenceId, practica.id)
                    }.getOrDefault(0) > 0

                _uiState.update {
                    it.copy(
                        competence = competence,
                        // El nivel entero y no un booleano: la tarjeta necesita
                        // su id y su formato para abrir la práctica igual que
                        // lo hace la hoja de modos.
                        nivelDePractica = practica.takeIf { tieneItems },
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
    /**
     * El nivel de calentamiento, **solo si tiene preguntas**; si no, null.
     *
     * Los cuatro existen en el panel desde que se creó el mecanismo, pero
     * poblarlos es trabajo de contenido que va aparte. Así el que esté vacío
     * no aparece, y aparece en cuanto lo llenen desde el panel, sin publicar
     * una versión nueva de la app.
     */
    val nivelDePractica: Level? = null,
    /**
     * Arranca en `true`, y no es un detalle.
     *
     * Empezaba en `false`, asi que en la primera composicion —antes de que
     * `loadCompetenceDetail` trajera nada— el estado era «no estoy cargando y
     * no tengo competencia», que la pantalla lee como **«Competencia no
     * encontrada»**. Se veia poco mas de medio segundo al entrar en cualquier
     * competencia: el tiempo justo para que parezca que algo va mal.
     *
     * Ademas nadie lo ponia nunca en `true` —solo en `false`, en dos sitios—,
     * asi que la rama del indicador de carga era codigo muerto que no podia
     * ejecutarse. Entrar aqui en `true` la devuelve a la vida y hace que el
     * «no encontrada» signifique lo que dice: se busco y no estaba.
     */
    val isLoading: Boolean = true,
    val error: String? = null
)

