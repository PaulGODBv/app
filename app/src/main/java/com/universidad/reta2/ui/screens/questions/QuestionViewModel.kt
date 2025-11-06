package com.universidad.reta2.ui.screens.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.usecases.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var isViewModelActive = true

    init {
        println("🔧 QuestionViewModel INIT")
    }

    fun loadQuestions(competencyId: Int, levelId: Int) {
        if (!isViewModelActive) {
            println("⚠️ ViewModel no activo, ignorando carga")
            return
        }

        viewModelScope.launch {
            try {
                println("🔄 Cargando preguntas para competency: $competencyId, level: $levelId")

                _uiState.update { it.copy(isLoading = true, error = null) }

                val questions = getQuestionsUseCase(competencyId, levelId)

                if (isViewModelActive && questions.isNotEmpty()) {
                    println("✅ ${questions.size} preguntas cargadas exitosamente")

                    _uiState.update {
                        it.copy(
                            questions = questions,
                            isLoading = false,
                            currentQuestionIndex = 0,
                            score = 0,
                            timeElapsed = 0,
                            selectedOptionId = null
                        )
                    }
                    startTimer()
                } else {
                    println("❌ No se pudieron cargar preguntas o ViewModel inactivo")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudieron cargar las preguntas"
                        )
                    }
                }

            } catch (e: Exception) {
                println("❌ Error cargando preguntas: ${e.message}")
                if (isViewModelActive) {
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Error desconocido al cargar preguntas",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isViewModelActive) {
                delay(1000)
                if (isViewModelActive) {
                    _uiState.update { it.copy(timeElapsed = it.timeElapsed + 1) }
                }
            }
        }
    }

    fun selectOption(optionId: Int) {
        if (!isViewModelActive) return

        println("🎯 Opción seleccionada: $optionId")
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    fun nextQuestion() {
        if (!isViewModelActive) return

        val currentState = _uiState.value

        // Validación extra de seguridad
        if (currentState.questions.isEmpty() ||
            currentState.currentQuestionIndex >= currentState.questions.size) {
            println("⚠️ No se puede avanzar: estado inválido")
            return
        }

        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val isCorrect = currentState.selectedOptionId == currentQuestion.correctOptionId
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        println("➡️ Avanzando a siguiente pregunta. Score: $newScore")

        _uiState.update {
            it.copy(
                currentQuestionIndex = it.currentQuestionIndex + 1,
                selectedOptionId = null,
                score = newScore
            )
        }
    }

    fun isLastQuestion(): Boolean {
        val state = _uiState.value
        return state.currentQuestionIndex >= state.questions.size - 1
    }

    fun getCurrentQuestion(): Question? {
        val state = _uiState.value
        return state.questions.getOrNull(state.currentQuestionIndex)
    }

    fun resetState() {
        println("🔄 Reseteando estado del ViewModel")
        isViewModelActive = false
        timerJob?.cancel()
        _uiState.value = QuestionUiState()
    }

    fun activate() {
        println("✅ Activando ViewModel")
        isViewModelActive = true
    }

    override fun onCleared() {
        println("🧹 ViewModel siendo destruido")
        super.onCleared()
        isViewModelActive = false
        timerJob?.cancel()
    }

    data class QuestionUiState(
        val questions: List<Question> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val selectedOptionId: Int? = null,
        val score: Int = 0,
        val timeElapsed: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null
    ) {
        val hasValidCurrentQuestion: Boolean
            get() = questions.isNotEmpty() && currentQuestionIndex < questions.size
    }
}