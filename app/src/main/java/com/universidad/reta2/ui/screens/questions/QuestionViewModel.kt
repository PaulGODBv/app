package com.universidad.reta2.ui.screens.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.source.CompetencyData
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.usecases.GetRandomizedQuestionsUseCase
import com.universidad.reta2.domain.usecases.UpdateProgressUseCase
import com.universidad.reta2.domain.usecases.GetQuestionsUseCase
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val updateProgressUseCase: UpdateProgressUseCase,
    private val competenceRepository: CompetenceRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedOptionId = MutableStateFlow<Int?>(null)
    val selectedOptionId: StateFlow<Int?> = _selectedOptionId.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _timeElapsed = MutableStateFlow(0)
    val timeElapsed: StateFlow<Int> = _timeElapsed.asStateFlow()

    private val _correctAnswers = MutableStateFlow(0)
    val correctAnswers: StateFlow<Int> = _correctAnswers.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted: StateFlow<Boolean> = _isQuizCompleted.asStateFlow()

    private val _currentCompetence = MutableStateFlow<Competence?>(null)
    val currentCompetence: StateFlow<Competence?> = _currentCompetence.asStateFlow()

    private val _currentLevelId = MutableStateFlow<Int>(1)
    val currentLevelId: StateFlow<Int> = _currentLevelId.asStateFlow()

    // 🔹 Navegación
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    // Temporizador
    private var isTimerRunning = false

    fun startTimer() {
        if (isTimerRunning) return

        isTimerRunning = true
        viewModelScope.launch {
            while (isTimerRunning && !_isQuizCompleted.value) {
                delay(1000)
                _timeElapsed.value += 1
            }
        }
    }

    fun stopTimer() {
        isTimerRunning = false
    }

    // 🔹 CARGAR PREGUNTAS DESDE COMPETENCY DATA
    fun loadQuestions(competenceId: Int, levelId: Int) {
        viewModelScope.launch {
            try {
                // Guardar el levelId
                _currentLevelId.value = levelId

                // 1. Cargar la competencia completa
                _currentCompetence.value = competenceRepository.getCompetenceById(competenceId)

                // 2. Obtener preguntas directamente desde CompetencyData
                val questions = CompetencyData.getQuestionsByCompetenceAndLevel(competenceId, levelId)

                if (questions.isNotEmpty()) {
                    // 3. Aleatorizar las preguntas (opcional)
                    val randomizedQuestions = questions.shuffled()
                    _questions.value = randomizedQuestions
                    resetQuizState()
                    startTimer()
                } else {
                    _questions.value = emptyList()
                }
            } catch (e: Exception) {
                // Manejar error
                _questions.value = emptyList()
                println("Error loading questions from CompetencyData: ${e.message}")
            }
        }
    }

    private fun resetQuizState() {
        _currentQuestionIndex.value = 0
        _selectedOptionId.value = null
        _streak.value = 0
        _correctAnswers.value = 0
        _timeElapsed.value = 0
        _isQuizCompleted.value = false
    }

    // Seleccionar opción
    fun selectOption(optionId: Int) {
        _selectedOptionId.value = optionId
    }

    // Verificar respuesta y avanzar
    fun submitAnswerAndAdvance() {
        val currentQuestion = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val selectedId = _selectedOptionId.value ?: return

        val isCorrect = selectedId == currentQuestion.correctOptionId

        // Actualizar estadísticas
        if (isCorrect) {
            _streak.value += 1
            _correctAnswers.value += 1
        } else {
            _streak.value = 0
        }

        // Avanzar a la siguiente pregunta
        advanceToNextQuestion()
    }

    private fun advanceToNextQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1

        if (nextIndex >= _questions.value.size) {
            // Quiz completado - disparar evento de navegación
            completeQuiz()
        } else {
            _currentQuestionIndex.value = nextIndex
            _selectedOptionId.value = null
        }
    }

    // 🔹 Completar quiz y disparar navegación
    private fun completeQuiz() {
        _isQuizCompleted.value = true
        stopTimer()

        viewModelScope.launch {
            _navigationEvent.emit(
                NavigationEvent.NavigateToResults(
                    competenceId = _currentCompetence.value?.id ?: 0,
                    levelId = _currentLevelId.value,
                    score = _correctAnswers.value,
                    totalQuestions = _questions.value.size,
                    timeSpent = _timeElapsed.value
                )
            )
        }
    }

    // 🔹 Navegación manual
    fun navigateBack() {
        stopTimer()
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavigateBack)
        }
    }

    // Obtener pregunta actual
    fun getCurrentQuestion(): Question? {
        return _questions.value.getOrNull(_currentQuestionIndex.value)
    }

    // Verificar si es la última pregunta
    fun isLastQuestion(): Boolean {
        return _currentQuestionIndex.value == _questions.value.size - 1
    }

    // Obtener progreso para la barra
    fun getProgress(): Float {
        val total = _questions.value.size
        return if (total > 0) {
            (_currentQuestionIndex.value + 1).toFloat() / total
        } else 0f
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    // 🔹 Sellado para eventos de navegación
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