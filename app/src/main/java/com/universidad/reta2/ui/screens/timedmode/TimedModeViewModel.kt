package com.universidad.reta2.ui.screens.timedmode

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.usecases.UpdateProgressUseCase
import com.universidad.reta2.domain.repositories.QuestionRepository
import com.universidad.reta2.data.local.dao.ProgressDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimedModeStep {
    SETUP_SOURCE,
    SETUP_COMPETENCE,
    SETUP_LEVEL,
    SETUP_TIME,
    PLAYING,
    FINISHED
}

enum class QuestionSource {
    ALL,
    COMPETENCE,
    LEVEL
}

data class TimedModeUiState(
    val step: TimedModeStep = TimedModeStep.SETUP_SOURCE,
    val questionSource: QuestionSource = QuestionSource.ALL,
    val competences: List<Competence> = emptyList(),
    val selectedCompetenceId: Int? = null,
    val selectedLevelId: Int? = null,
    val selectedMinutes: Int = 3,

    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionId: Int? = null,
    val timeRemainingSeconds: Int = 0,
    val isLoadingQuestions: Boolean = false,
    val showFeedback: Boolean = false,
    val lastAnswerCorrect: Boolean = false,

    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val error: String? = null
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentQuestionIndex)

    val selectedCompetence: Competence?
        get() = competences.firstOrNull { it.id == selectedCompetenceId }

    val progressFraction: Float
        get() = if (timeRemainingSeconds <= 0 || selectedMinutes <= 0) 0f
                else timeRemainingSeconds.toFloat() / (selectedMinutes * 60).toFloat()
}

@HiltViewModel
class TimedModeViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository,
    private val questionRepository: QuestionRepository,
    private val progressDao: ProgressDao,
    private val updateProgressUseCase: UpdateProgressUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimedModeUiState())
    val uiState: StateFlow<TimedModeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var feedbackJob: Job? = null

    init {
        loadCompetences()
    }

    private fun loadCompetences() {
        viewModelScope.launch {
            try {
                val competences = competenceRepository.getAllCompetences()
                _uiState.update { it.copy(competences = competences) }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun selectSource(source: QuestionSource) {
        _uiState.update {
            it.copy(
                questionSource = source,
                step = when (source) {
                    QuestionSource.ALL        -> TimedModeStep.SETUP_TIME
                    QuestionSource.COMPETENCE -> TimedModeStep.SETUP_COMPETENCE
                    QuestionSource.LEVEL      -> TimedModeStep.SETUP_COMPETENCE
                }
            )
        }
    }

    fun selectCompetence(competenceId: Int) {
        _uiState.update {
            it.copy(
                selectedCompetenceId = competenceId,
                step = when (it.questionSource) {
                    QuestionSource.LEVEL -> TimedModeStep.SETUP_LEVEL
                    else                 -> TimedModeStep.SETUP_TIME
                }
            )
        }
    }

    fun selectLevel(levelId: Int) {
        _uiState.update {
            it.copy(
                selectedLevelId = levelId,
                step = TimedModeStep.SETUP_TIME
            )
        }
    }

    fun selectMinutes(minutes: Int) {
        _uiState.update { it.copy(selectedMinutes = minutes) }
    }

    fun goBack() {
        _uiState.update {
            it.copy(
                step = when (it.step) {
                    TimedModeStep.SETUP_COMPETENCE -> TimedModeStep.SETUP_SOURCE
                    TimedModeStep.SETUP_LEVEL      -> TimedModeStep.SETUP_COMPETENCE
                    TimedModeStep.SETUP_TIME       -> when (it.questionSource) {
                        QuestionSource.ALL        -> TimedModeStep.SETUP_SOURCE
                        QuestionSource.COMPETENCE -> TimedModeStep.SETUP_COMPETENCE
                        QuestionSource.LEVEL      -> TimedModeStep.SETUP_LEVEL
                    }
                    else -> it.step
                }
            )
        }
    }

    fun startTimedMode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingQuestions = true) }

            try {
                // Preparar timer; si no hay minutos válidos, abortar
                val totalSeconds = _uiState.value.selectedMinutes * 60
                if (totalSeconds <= 0) {
                    _uiState.update { it.copy(isLoadingQuestions = false, error = "Tiempo no válido") }
                    return@launch
                }

                // Guardar el tiempo restante antes de cargar preguntas para evitar carreras
                _uiState.update { it.copy(timeRemainingSeconds = totalSeconds) }

                val questions = loadQuestions()

                if (questions.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoadingQuestions = false,
                            error = "No hay preguntas disponibles para la selección actual"
                        )
                    }
                    return@launch
                }

                val totalSeconds = _uiState.value.selectedMinutes * 60

                _uiState.update {
                    it.copy(
                        step = TimedModeStep.PLAYING,
                        questions = questions.shuffled(),
                        currentQuestionIndex = 0,
                        selectedOptionId = null,
                        timeRemainingSeconds = totalSeconds,
                        totalAnswered = 0,
                        totalCorrect = 0,
                        isLoadingQuestions = false,
                        error = null
                    )
                }

                startTimer()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingQuestions = false,
                        error = "Error al cargar preguntas: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadQuestions(): List<Question> {
        val state = _uiState.value
        val username = SessionManager.getCurrentUsername(context) ?: "usuario_invitado"
        val allQuestions = mutableListOf<Question>()

        when (state.questionSource) {
            QuestionSource.ALL -> {
                state.competences.forEach { competence ->
                    competence.levels.forEach { level ->
                        val correctIds = progressDao.getCorrectlyAnsweredQuestionIds(username, level.id)
                        val questions = questionRepository.getRandomQuestions(
                            competenceId = competence.id,
                            levelId = level.id,
                            username = username,
                            correctlyAnsweredIds = correctIds
                        )
                        allQuestions.addAll(questions)
                    }
                }
            }
            QuestionSource.COMPETENCE -> {
                val competence = state.competences.firstOrNull { it.id == state.selectedCompetenceId }
                competence?.levels?.forEach { level ->
                    val correctIds = progressDao.getCorrectlyAnsweredQuestionIds(username, level.id)
                    val questions = questionRepository.getRandomQuestions(
                        competenceId = competence.id,
                        levelId = level.id,
                        username = username,
                        correctlyAnsweredIds = correctIds
                    )
                    allQuestions.addAll(questions)
                }
            }
            QuestionSource.LEVEL -> {
                val competenceId = state.selectedCompetenceId ?: return emptyList()
                val levelId = state.selectedLevelId ?: return emptyList()
                val correctIds = progressDao.getCorrectlyAnsweredQuestionIds(username, levelId)
                val questions = questionRepository.getRandomQuestions(
                    competenceId = competenceId,
                    levelId = levelId,
                    username = username,
                    correctlyAnsweredIds = correctIds
                )
                allQuestions.addAll(questions)
            }
        }

        return allQuestions.shuffled()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0) {
                delay(1000)
                _uiState.update {
                    it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1)
                }
            }
            finishMode()
        }
    }

    fun selectOption(optionId: Int) {
        val state = _uiState.value
        if (state.showFeedback || state.selectedOptionId != null) return

        val currentQuestion = state.currentQuestion ?: return
        val isCorrect = optionId == currentQuestion.correctOptionId

        _uiState.update {
            it.copy(
                selectedOptionId = optionId,
                showFeedback = true,
                lastAnswerCorrect = isCorrect,
                totalAnswered = it.totalAnswered + 1,
                totalCorrect = if (isCorrect) it.totalCorrect + 1 else it.totalCorrect
            )
        }

        viewModelScope.launch {
            try {
                updateProgressUseCase(
                    questionId = currentQuestion.id,
                    isCorrect = isCorrect,
                    timeSpent = 0,
                    levelId = getLevelIdForQuestion(currentQuestion),
                    competenceId = getCompetenceIdForQuestion(currentQuestion),
                    isLevelCompleted = false,
                    levelScore = 0,
                    totalQuestions = 0
                )
            } catch (e: Exception) {
                // ignore
            }
        }

        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            delay(1000)
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        val state = _uiState.value

        if (state.currentQuestionIndex >= state.questions.size - 1) {
            viewModelScope.launch {
                // Evitar cargar más preguntas si el tiempo ya terminó
                if (_uiState.value.timeRemainingSeconds <= 0) {
                    finishMode()
                    return@launch
                }

                val moreQuestions = loadQuestions()

                // Si no hay nuevas preguntas o el tiempo se agotó durante la carga, terminar
                if (moreQuestions.isEmpty() || _uiState.value.timeRemainingSeconds <= 0) {
                    finishMode()
                } else {
                    _uiState.update {
                        it.copy(
                            questions = it.questions + moreQuestions.shuffled(),
                            currentQuestionIndex = it.currentQuestionIndex + 1,
                            selectedOptionId = null,
                            showFeedback = false
                        )
                    }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedOptionId = null,
                    showFeedback = false
                )
            }
        }
    }

    private fun finishMode() {
        timerJob?.cancel()
        feedbackJob?.cancel()
        _uiState.update { it.copy(step = TimedModeStep.FINISHED) }
    }

    private fun getLevelIdForQuestion(question: Question): Int {
        val state = _uiState.value
        state.competences.forEach { competence ->
            competence.levels.forEach { level ->
                val questions = try {
                    com.universidad.reta2.data.source.CompetencyData
                        .getQuestionsByCompetenceAndLevel(competence.id, level.id)
                } catch (e: Exception) { emptyList() }
                if (questions.any { it.id == question.id }) return level.id
            }
        }
        return state.selectedLevelId ?: 0
    }

    private fun getCompetenceIdForQuestion(question: Question): Int {
        val state = _uiState.value
        state.competences.forEach { competence ->
            competence.levels.forEach { level ->
                val questions = try {
                    com.universidad.reta2.data.source.CompetencyData
                        .getQuestionsByCompetenceAndLevel(competence.id, level.id)
                } catch (e: Exception) { emptyList() }
                if (questions.any { it.id == question.id }) return competence.id
            }
        }
        return state.selectedCompetenceId ?: 0
    }

    fun resetMode() {
        timerJob?.cancel()
        feedbackJob?.cancel()
        _uiState.update {
            TimedModeUiState(competences = it.competences)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        feedbackJob?.cancel()
    }
}
