package com.universidad.reta2.ui.screens.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.usecases.UpdateProgressUseCase
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
    private val updateProgressUseCase: UpdateProgressUseCase,
    private val competenceRepository: CompetenceRepository,
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {

    //  ESTADO SEGURO CON PROTECCIONES
    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var isViewModelActive = true

    private var _origin = MutableStateFlow("competencies") // Valor por defecto
    val origin: StateFlow<String> = _origin.asStateFlow()

    //  CONTROL DE NAVEGACIÓN SEGURO
    private var currentCompetenceId: Int = 0
    private var currentLevelId: Int = 0

    init {
        println("🔧 QuestionViewModel INIT con UpdateProgressUseCase y GetQuestionsUseCase")
    }

    fun setOrigin(origin: String) {
        println("🎯 QuestionViewModel - Origin set to: $origin")
        _origin.value = origin
    }
    //  CARGAR PREGUNTAS CON PROTECCIÓN
    fun loadQuestions(competenceId: Int, levelId: Int) {
        if (!isViewModelActive) {
            println("ViewModel no activo, ignorando carga")
            return
        }

        viewModelScope.launch {
            try {
                println(" Cargando preguntas para competence: $competenceId, level: $levelId")

                _uiState.update { it.copy(isLoading = true, error = null) }

                // Cargar competencia y preguntas USANDO EL USE CASE ✅
                val competence = competenceRepository.getCompetenceById(competenceId)
                val questions = getQuestionsUseCase(competenceId, levelId) // ✅ USAR USE CASE

                if (isViewModelActive && questions.isNotEmpty()) {
                    println(" ${questions.size} preguntas cargadas exitosamente")

                    currentCompetenceId = competenceId
                    currentLevelId = levelId

                    _uiState.update {
                        it.copy(
                            questions = questions.shuffled(),
                            currentCompetence = competence,
                            isLoading = false,
                            currentQuestionIndex = 0,
                            score = 0,
                            timeElapsed = 0,
                            selectedOptionId = null,
                            streak = 0, // STREAK INICIALIZADO EN 0
                            isQuizCompleted = false
                        )
                    }
                    startTimer()
                } else {
                    println(" No se pudieron cargar preguntas o ViewModel inactivo")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudieron cargar las preguntas"
                        )
                    }
                }

            } catch (e: Exception) {
                println(" Error cargando preguntas: ${e.message}")
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

    //  TEMPORIZADOR SEGURO
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isViewModelActive && !_uiState.value.isQuizCompleted) {
                delay(1000)
                if (isViewModelActive) {
                    _uiState.update { it.copy(timeElapsed = it.timeElapsed + 1) }
                }
            }
        }
    }

    // SELECCIONAR OPCIÓN CON PROTECCIÓN
    fun selectOption(optionId: Int) {
        if (!isViewModelActive) return

        println("🎯 Opción seleccionada: $optionId")
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    //  SIGUIENTE PREGUNTA CON ACTUALIZACIÓN DE PROGRESO
    fun nextQuestion() {
        if (!isViewModelActive) return

        val currentState = _uiState.value

        // Validación extra de seguridad
        if (currentState.questions.isEmpty() ||
            currentState.currentQuestionIndex >= currentState.questions.size) {
            println("❌ No se puede avanzar: estado inválido")
            return
        }

        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val isCorrect = currentState.selectedOptionId == currentQuestion.correctOptionId

        // Calcular nuevo estado ANTES DE ACTUALIZAR
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score
        val newStreak = if (isCorrect) currentState.streak + 1 else 0

        // DETERMINAR SI ES LA ÚLTIMA PREGUNTA ANTES DE INCREMENTAR
        val isLastQuestion = currentState.currentQuestionIndex == currentState.questions.size - 1

        println("🔄 QuestionViewModel.nextQuestion:")
        println("   - Pregunta actual: ${currentState.currentQuestionIndex + 1}/${currentState.questions.size}")
        println("   - isLastQuestion: $isLastQuestion")
        println("   - currentCompetenceId: $currentCompetenceId")
        println("   - currentLevelId: $currentLevelId")
        println("   - newScore: $newScore")
        println("   - totalQuestions: ${currentState.questions.size}")

        // ACTUALIZAR PROGRESO - INCLUYENDO LA ÚLTIMA PREGUNTA
        viewModelScope.launch {
            try {
                if (isViewModelActive) {
                    println("📤 LLAMANDO a UpdateProgressUseCase...")

                    updateProgressUseCase(
                        questionId = currentQuestion.id,
                        isCorrect = isCorrect,
                        timeSpent = 1,
                        levelId = currentLevelId,
                        competenceId = currentCompetenceId,
                        isLevelCompleted = isLastQuestion, // 🔥 ESTE DEBE SER TRUE PARA LA ÚLTIMA PREGUNTA
                        levelScore = if (isLastQuestion) newScore else 0,
                        totalQuestions = if (isLastQuestion) currentState.questions.size else 0
                    )

                    if (isLastQuestion) {
                        println("🏁 Proceso de completado de nivel $currentLevelId finalizado")
                        println("📈 Score final: $newScore/${currentState.questions.size}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error actualizando progreso: ${e.message}")
            }
        }

        _uiState.update {
            it.copy(
                currentQuestionIndex = it.currentQuestionIndex + 1,
                selectedOptionId = null,
                score = newScore,
                streak = newStreak,
                isQuizCompleted = isLastQuestion
            )
        }

        if (isLastQuestion) {
            completeQuiz()
        }
    }

    // 🔒 COMPLETAR QUIZ CON PROTECCIÓN
    private fun completeQuiz() {
        if (!isViewModelActive) return

        val currentState = _uiState.value
        val finalScore = currentState.score

        println("🎉 QUIZ COMPLETADO!")
        println("📊 Score final: $finalScore/${currentState.questions.size}")
        println("⏱️ Tiempo total: ${currentState.timeElapsed} segundos")
        println("🔥 Racha final: ${currentState.streak}")
        println("📍 Origin: ${_origin.value}")

        // Detener timer
        timerJob?.cancel()

        // 🔥 ACTUALIZAR ESTADO PARA INDICAR QUE ESTÁ LISTO PARA NAVEGAR
        _uiState.update { it.copy(isQuizCompleted = true) }
    }

    // 🔒 MÉTODOS DE UTILIDAD SEGUROS
    fun isLastQuestion(): Boolean {
        val state = _uiState.value
        return state.currentQuestionIndex >= state.questions.size - 1
    }

    fun getCurrentQuestion(): Question? {
        val state = _uiState.value
        return state.questions.getOrNull(state.currentQuestionIndex)
    }

    fun getTotalQuestions(): Int {
        return _uiState.value.questions.size
    }

    fun getProgress(): Float {
        val state = _uiState.value
        val total = state.questions.size
        return if (total > 0) {
            (state.currentQuestionIndex + 1).toFloat() / total
        } else 0f
    }

    // 🔒 ACTIVACIÓN/DESACTIVACIÓN SEGURA
    fun activate() {
        println("✅ Activando ViewModel")
        isViewModelActive = true
    }

    fun resetState() {
        println("🔄 Reseteando estado del ViewModel")
        isViewModelActive = false
        timerJob?.cancel()
        _uiState.value = QuestionUiState()
    }

    override fun onCleared() {
        println("🧹 ViewModel siendo destruido")
        super.onCleared()
        isViewModelActive = false
        timerJob?.cancel()
    }

    // 🔒 DATA CLASS DEL ESTADO CON STREAK ✅
    data class QuestionUiState(
        val questions: List<Question> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val selectedOptionId: Int? = null,
        val score: Int = 0,
        val streak: Int = 0, // ✅ STREAK AGREGADO
        val timeElapsed: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isQuizCompleted: Boolean = false,
        val currentCompetence: Competence? = null
    ) {
        val hasValidCurrentQuestion: Boolean
            get() = questions.isNotEmpty() && currentQuestionIndex < questions.size

        val currentQuestion: Question?
            get() = questions.getOrNull(currentQuestionIndex)
    }

    // 🔒 EVENTOS DE NAVEGACIÓN (opcional - si los necesitas)
    sealed class NavigationEvent {
        data class NavigateToResults(
            val competenceId: Int,
            val levelId: Int,
            val score: Int,
            val totalQuestions: Int,
            val timeSpent: Int
        ) : NavigationEvent()

        object NavigateBack : NavigationEvent()
    }
}