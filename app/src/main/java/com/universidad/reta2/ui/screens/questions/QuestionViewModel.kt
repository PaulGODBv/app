package com.universidad.reta2.ui.screens.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.usecases.GetRandomizedQuestionsUseCase
import com.universidad.reta2.domain.usecases.UpdateProgressUseCase
import com.universidad.reta2.domain.usecases.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val getRandomizedQuestionsUseCase: GetRandomizedQuestionsUseCase,
    private val updateProgressUseCase: UpdateProgressUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase
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

    private val _answeredQuestions = MutableStateFlow<List<Int>>(emptyList())
    val answeredQuestions: StateFlow<List<Int>> = _answeredQuestions.asStateFlow()

    private val _correctAnswersList = MutableStateFlow<List<Int>>(emptyList())
    val correctAnswersList: StateFlow<List<Int>> = _correctAnswersList.asStateFlow()

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

    // Cargar preguntas
    fun loadQuestions(competenceId: String, levelId: Int) {
        viewModelScope.launch {
            try {
                // 1. Obtener preguntas específicas del nivel
                val questions = getQuestionsUseCase(competenceId, levelId)

                if (questions.isNotEmpty()) {
                    // 2. Aleatorizar las preguntas
                    val randomizedQuestions = getRandomizedQuestionsUseCase(questions)
                    _questions.value = randomizedQuestions
                    resetQuizState()
                    startTimer()
                } else {
                    // Manejar caso de no encontrar preguntas
                    _questions.value = emptyList()
                }
            } catch (e: Exception) {
                // Manejar error
                _questions.value = emptyList()
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
        _answeredQuestions.value = emptyList()
        _correctAnswersList.value = emptyList()
    }

    // Seleccionar opción
    fun selectOption(optionId: Int) {
        _selectedOptionId.value = optionId
    }

    // Verificar respuesta y avanzar
    fun submitAnswerAndAdvance(): Boolean {
        val currentQuestion = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return false
        val selectedId = _selectedOptionId.value ?: return false

        val isCorrect = selectedId == currentQuestion.correctOptionId

        // Actualizar estadísticas
        if (isCorrect) {
            _streak.value += 1
            _correctAnswers.value += 1
            _correctAnswersList.value = _correctAnswersList.value + _currentQuestionIndex.value
        } else {
            _streak.value = 0
        }

        _answeredQuestions.value = _answeredQuestions.value + _currentQuestionIndex.value

        // Guardar progreso
        viewModelScope.launch {
            updateProgressUseCase(
                questionId = currentQuestion.id,
                isCorrect = isCorrect,
                timeSpent = 1 // O el tiempo real por pregunta si lo tienes
            )
        }

        // Avanzar a la siguiente pregunta
        advanceToNextQuestion()

        return isCorrect
    }

    private fun advanceToNextQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1

        if (nextIndex >= _questions.value.size) {
            // Quiz completado
            _isQuizCompleted.value = true
            stopTimer()
        } else {
            _currentQuestionIndex.value = nextIndex
            _selectedOptionId.value = null
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
}