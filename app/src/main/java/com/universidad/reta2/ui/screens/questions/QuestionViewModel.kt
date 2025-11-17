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
    //private var isViewModelActive = true

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
        if (_uiState.value.isLoading ||
            (_uiState.value.questions.isNotEmpty() &&
                    currentCompetenceId == competenceId &&
                    currentLevelId == levelId)) {

            println("Ignorando carga: isLoading=${_uiState.value.isLoading} o preguntas ya cargadas.")
            return
        }

        viewModelScope.launch {
            try {
                println(" Cargando preguntas para competence: $competenceId, level: $levelId")
                _uiState.update { it.copy(isLoading = true, error = null) }

                val competence = competenceRepository.getCompetenceById(competenceId)
                val questions = getQuestionsUseCase(competenceId, levelId)

                if (questions.isNotEmpty()) {
                    println(" ${questions.size} preguntas cargadas exitosamente")

                    currentCompetenceId = competenceId // Guardar los IDs actuales
                    currentLevelId = levelId

                    _uiState.update {
                        it.copy(
                            questions = questions.shuffled(),
                            currentCompetence = competence,
                            isLoading = false,
                            currentQuestionIndex = 0,
                            // ... (resetear score, tiempo, etc. SÍ está bien aquí)
                            score = 0,
                            timeElapsed = 0,
                            selectedOptionId = null,
                            streak = 0,
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
            }
        }
    }

    //  TEMPORIZADOR SEGURO
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_uiState.value.isQuizCompleted) {
                delay(1000)
                _uiState.update { it.copy(timeElapsed = it.timeElapsed + 1) }
            }
        }
    }

    // SELECCIONAR OPCIÓN CON PROTECCIÓN
    fun selectOption(optionId: Int) {
        //if (!isViewModelActive) return

        println("🎯 Opción seleccionada: $optionId")
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    //  SIGUIENTE PREGUNTA CON ACTUALIZACIÓN DE PROGRESO
    fun nextQuestion() {
        //if (!isViewModelActive) return

        val currentState = _uiState.value

        // Validación de seguridad
        if (currentState.questions.isEmpty() ||
            currentState.currentQuestionIndex >= currentState.questions.size) {
            println("❌ No se puede avanzar: estado inválido")
            return
        }

        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
        val isCorrect = currentState.selectedOptionId == currentQuestion.correctOptionId

        // Calcular nuevo estado
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score
        val newStreak = if (isCorrect) currentState.streak + 1 else 0

        // 🔥 DETERMINAR SI ES LA ÚLTIMA PREGUNTA CORRECTAMENTE
        val isLastQuestion = currentState.currentQuestionIndex == currentState.questions.size - 1
        val isActuallyLastQuestion = isLastQuestion // Para claridad

        println("🔄 QuestionViewModel.nextQuestion:")
        println("   - Pregunta actual: ${currentState.currentQuestionIndex + 1}/${currentState.questions.size}")
        println("   - isLastQuestion: $isActuallyLastQuestion")
        println("   - Score actual: $newScore/${currentState.questions.size}")
        println("   - Porcentaje: ${(newScore.toFloat() / currentState.questions.size * 100).toInt()}%")

        // 🔥 ACTUALIZAR PROGRESO SOLO PARA LA ÚLTIMA PREGUNTA
        viewModelScope.launch {
            try {
                if (isActuallyLastQuestion) {
                    println("📤 LLAMANDO a UpdateProgressUseCase para COMPLETAR NIVEL...")

                    updateProgressUseCase(
                        questionId = currentQuestion.id,
                        isCorrect = isCorrect,
                        timeSpent = 1,
                        levelId = currentLevelId,
                        competenceId = currentCompetenceId,
                        isLevelCompleted = true, // 🔥 SOLO PARA LA ÚLTIMA PREGUNTA
                        levelScore = newScore,
                        totalQuestions = currentState.questions.size
                    )

                    println("🏁 Proceso de completado de nivel $currentLevelId finalizado")
                    println("📈 Score final: $newScore/${currentState.questions.size}")
                        // 🔥 VERIFICAR SI SE DEBE DESBLOQUEAR (mínimo 80%
                    val progressPercentage = newScore.toFloat() / currentState.questions.size
                    val shouldUnlock = progressPercentage >= 0.8f
                    println("🔓 Condición desbloqueo: $progressPercentage >= 0.8 → $shouldUnlock")
                } else {
                    // Para preguntas que NO son la última, solo registrar el intento
                    updateProgressUseCase(
                        questionId = currentQuestion.id,
                        isCorrect = isCorrect,
                        timeSpent = 1,
                        levelId = currentLevelId,
                        competenceId = currentCompetenceId,
                        isLevelCompleted = false, // 🔥 IMPORTANTE: false para preguntas no finales
                        levelScore = 0,
                        totalQuestions = 0
                    )
                }
            } catch (e: Exception) {
                println("❌ Error actualizando progreso: ${e.message}")
            }
        }

        // Actualizar estado de la UI
        _uiState.update {
            it.copy(
                currentQuestionIndex = it.currentQuestionIndex + 1,
                selectedOptionId = null,
                score = newScore,
                streak = newStreak,
                isQuizCompleted = isActuallyLastQuestion
            )
        }

        if (isActuallyLastQuestion) {
            completeQuiz()
        }
    }

    // 🔒 COMPLETAR QUIZ CON PROTECCIÓN
    private fun completeQuiz() {
        //if (!isViewModelActive) return

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


    override fun onCleared() {
        println("🧹 ViewModel siendo destruido")
        super.onCleared()
        //isViewModelActive = false
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