package com.universidad.reta2.ui.screens.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.local.RepasoDeSesion
import com.universidad.reta2.data.local.RespuestaDeSesion
import com.universidad.reta2.domain.LevelRules
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
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val repasoDeSesion: RepasoDeSesion
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

    /**
     * Si la sesión en curso es de práctica.
     *
     * Lo fija la pantalla desde la ruta. Cambia tres cosas: la respuesta se
     * revela al momento, el intento no se registra y el nivel no se completa
     * ni desbloquea nada.
     */
    private var esPractica: Boolean = false

    fun fijarModo(practica: Boolean) {
        esPractica = practica
        _uiState.update { it.copy(esPractica = practica) }
    }

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

                    // Registro limpio para el repaso de resultados: esta
                    // sesión no debe arrastrar las respuestas de la anterior.
                    repasoDeSesion.empezar(levelId)

                    // Queda constancia de que se estuvo aqui, en los dos
                    // modos: es lo que alimenta «Continuar practicando».
                    competenceRepository.marcarNivelPracticado(levelId)

                    _uiState.update {
                        it.copy(
                            questions = questions.shuffled(),
                            currentCompetence = competence,
                            isLoading = false,
                            currentQuestionIndex = 0,
                            // ... (resetear score, tiempo, etc. SÍ está bien aquí)
                            score = 0,
                            timeElapsed = 0,
                            timeAtQuestionStart = 0,
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
        val estado = _uiState.value

        // En práctica la respuesta se revela al tocarla, y a partir de ahí no
        // se puede cambiar: si se pudiera, bastaría con ir probando hasta ver
        // el verde y la explicación dejaría de explicar nada.
        if (estado.esPractica && estado.respuestaRevelada) return

        println("🎯 Opción seleccionada: $optionId")
        _uiState.update {
            it.copy(
                selectedOptionId = optionId,
                respuestaRevelada = it.esPractica
            )
        }
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

        // Se anota antes de avanzar, que es cuando todavía se sabe qué opción
        // se eligió: el estado la borra en la siguiente pregunta.
        repasoDeSesion.anotar(
            currentLevelId,
            RespuestaDeSesion(
                pregunta = currentQuestion,
                idElegido = currentState.selectedOptionId,
                acerto = isCorrect
            )
        )

        val timeSpentOnThisQuestion= currentState.timeElapsed - currentState.timeAtQuestionStart
        val nextQuestionStartTime=currentState.timeElapsed

        // Calcular nuevo estado
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score
        val newStreak = if (isCorrect) currentState.streak + 1 else 0

        //  DETERMINAR SI ES LA ÚLTIMA PREGUNTA
        val isLastQuestion = currentState.currentQuestionIndex == currentState.questions.size - 1
        val isActuallyLastQuestion = isLastQuestion // Para claridad



        println("🔄 QuestionViewModel.nextQuestion:")
        println("   - Pregunta actual: ${currentState.currentQuestionIndex + 1}/${currentState.questions.size}")
        println("   - isLastQuestion: $isActuallyLastQuestion")
        println("   - Score actual: $newScore/${currentState.questions.size}")
        println("   - Porcentaje: ${(newScore.toFloat() / currentState.questions.size * 100).toInt()}%")

        //  ACTUALIZAR PROGRESO SOLO PARA LA ÚLTIMA PREGUNTA
        viewModelScope.launch {
            try {
                if (isActuallyLastQuestion) {
                    println("📤 LLAMANDO a UpdateProgressUseCase para COMPLETAR NIVEL...")

                    updateProgressUseCase(
                        questionId = currentQuestion.id,
                        isCorrect = isCorrect,
                        timeSpent = timeSpentOnThisQuestion,
                        levelId = currentLevelId,
                        competenceId = currentCompetenceId,
                        isLevelCompleted = true, // 🔥 SOLO PARA LA ÚLTIMA PREGUNTA
                        levelScore = newScore,
                        totalQuestions = currentState.questions.size,
                        esPractica = esPractica
                    )

                    println("🏁 Proceso de completado de nivel $currentLevelId finalizado")
                    println("📈 Score final: $newScore/${currentState.questions.size}")
                    // Traza informativa del desbloqueo. Quien decide es
                    // ProgressRepository; aquí solo se imprime, y con el mismo
                    // umbral de LevelRules para que el log no contradiga al
                    // código: estaba escrito a 0.8 desde antes de TODO-A.
                    val progressPercentage = newScore * 100 / currentState.questions.size
                    val shouldUnlock = progressPercentage >= LevelRules.PASSING_PERCENTAGE
                    println("🔓 Condición desbloqueo: $progressPercentage% >= ${LevelRules.PASSING_PERCENTAGE}% → $shouldUnlock")
                } else {
                    // Para preguntas que NO son la última, solo registrar el intento
                    updateProgressUseCase(
                        questionId = currentQuestion.id,
                        isCorrect = isCorrect,
                        timeSpent = timeSpentOnThisQuestion,
                        levelId = currentLevelId,
                        competenceId = currentCompetenceId,
                        isLevelCompleted = false, //  IMPORTANTE: false para preguntas no finales
                        levelScore = 0,
                        totalQuestions = 0,
                        esPractica = esPractica
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
                respuestaRevelada = false,
                score = newScore,
                streak = newStreak,
                isQuizCompleted = isActuallyLastQuestion,
                timeAtQuestionStart = nextQuestionStartTime
            )
        }

        if (isActuallyLastQuestion) {
            completeQuiz()
        }
    }

    //  COMPLETAR QUIZ CON PROTECCIÓN
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

    //  MÉTODOS DE UTILIDAD SEGUROS
    fun isLastQuestion(): Boolean {
        val state = _uiState.value
        return state.currentQuestionIndex >= state.questions.size - 1
    }


    override fun onCleared() {
        super.onCleared()
        //isViewModelActive = false
        timerJob?.cancel()
    }

    data class QuestionUiState(
        val questions: List<Question> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val selectedOptionId: Int? = null,
        val score: Int = 0,
        val streak: Int = 0,
        val timeElapsed: Int = 0,
        val timeAtQuestionStart: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null,
        val esPractica: Boolean = false,
        /** En práctica, si ya se reveló la respuesta de la pregunta actual. */
        val respuestaRevelada: Boolean = false,
        val isQuizCompleted: Boolean = false,
        val currentCompetence: Competence? = null
    ) {
        val hasValidCurrentQuestion: Boolean
            get() = questions.isNotEmpty() && currentQuestionIndex < questions.size

        val currentQuestion: Question?
            get() = questions.getOrNull(currentQuestionIndex)
    }


}