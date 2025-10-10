package com.universidad.reta2.ui.screens.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.usecases.GetRandomizedQuestionsUseCase
import com.universidad.reta2.domain.usecases.UpdateProgressUseCase
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
    private val updateProgressUseCase: UpdateProgressUseCase
) : ViewModel() {

    // Estado de las preguntas (orden fijo durante este intento)
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    // Estado actual
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

    // Cargar preguntas aleatorizadas (una sola vez por intento)
    fun loadQuestions(originalQuestions: List<Question>) {
        viewModelScope.launch {
            val randomizedQuestions = getRandomizedQuestionsUseCase(originalQuestions)
            _questions.value = randomizedQuestions
            resetQuizState()
            startTimer()
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

    // Verificar respuesta y avanzar automáticamente
    fun submitAnswerAndAdvance(): Boolean {
        val currentQuestion = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return false
        val selectedId = _selectedOptionId.value ?: return false

        val isCorrect = selectedId == currentQuestion.correctOptionId

        // Actualizar estadísticas
        if (isCorrect) {
            _streak.value += 1
            _correctAnswers.value += 1
        } else {
            _streak.value = 0
        }

        // Guardar progreso
        viewModelScope.launch {
            updateProgressUseCase(
                questionId = currentQuestion.id,
                isCorrect = isCorrect,
                timeSpent = 1
            )
        }

        // Avanzar automáticamente a la siguiente pregunta
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
            _selectedOptionId.value = null // Resetear selección para nueva pregunta
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