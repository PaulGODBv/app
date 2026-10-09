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
import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.utils.Sonidos
import com.universidad.reta2.utils.Vibracion
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val updateProgressUseCase: UpdateProgressUseCase,
    private val competenceRepository: CompetenceRepository,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val repasoDeSesion: RepasoDeSesion,
    private val userStatsRepository: UserStatsRepository,
    @ApplicationContext private val context: Context
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
    private companion object {
        /**
         * Lo que «Siguiente pregunta» tarda en habilitarse tras revelar la
         * respuesta, en práctica.
         *
         * **No es un adorno: sin esto la aplicación se cierra.** Revelar la
         * respuesta añade el bloque del porqué debajo de las opciones, y
         * avanzar quita y vuelve a crear todos los hijos del contenedor
         * —cada bloque va dentro de un `key(...)` que incluye el índice de la
         * pregunta—. Si las dos cosas caen en el mismo fotograma, Compose
         * intenta retirar un nodo que todavía no había llegado a engancharse y
         * revienta con un `NullPointerException` en
         * `LayoutNode.onChildRemoved`. La pila no trae ni una línea nuestra,
         * así que desde el informe de fallo no hay forma de verlo.
         *
         * Medido en dispositivo el 09/10/2026: con 200 ms entre tocar la
         * opción y tocar «Siguiente» se cierra; con 500 ms aguanta. 400 ms
         * deja margen por encima del umbral sin que se note como un frenazo.
         *
         * **Es un cerrojo, no la cura.** La reparación de fondo es que el
         * contenido deje de recrearse entero en cada pregunta, quitando el
         * índice de esas claves para que Compose actualice en vez de
         * reconstruir. Eso toca el corazón de la pantalla y no se hace a dos
         * semanas de la entrega.
         *
         * Y de paso hace lo que debe: impedir que se salte de un golpe la
         * explicación que se acaba de ganar.
         */
        const val MILIS_ANTES_DE_AVANZAR = 400L
    }

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
                respuestaRevelada = it.esPractica,
                // En práctica, «Siguiente» queda bloqueado un instante. Ver
                // MILIS_ANTES_DE_AVANZAR.
                puedeAvanzar = !it.esPractica
            )
        }

        if (estado.esPractica) {
            viewModelScope.launch {
                delay(MILIS_ANTES_DE_AVANZAR)
                _uiState.update { it.copy(puedeAvanzar = true) }
            }
        }

        // La vibración va AQUÍ y solo en práctica, no en nextQuestion(), y la
        // razón importa: en evaluación el estudiante no sabe si acertó hasta
        // el repaso final, así que una vibración distinta por acierto y por
        // fallo le estaría cantando la respuesta pregunta por pregunta. En
        // práctica la respuesta se revela al tocarla, y entonces la vibración
        // no añade información: confirma la que ya está en pantalla.
        if (estado.esPractica) {
            val pregunta = estado.questions.getOrNull(estado.currentQuestionIndex)
            if (pregunta != null) {
                if (optionId == pregunta.correctOptionId) {
                    Vibracion.acierto(context)
                    Sonidos.acierto(context)
                } else {
                    Vibracion.fallo(context)
                    Sonidos.fallo(context)
                }
            }
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

                    // La racha se mide antes y después porque sube **una vez
                    // al día**, en la primera actividad, y de eso depende si
                    // se enseña la pantalla de racha o se va directo a
                    // resultados. `getUserStatsOnce` y no el flujo: la caché
                    // en memoria adelanta un valor y aquí hace falta el de la
                    // base.
                    val rachaAntes = runCatching {
                        userStatsRepository.getUserStatsOnce().currentStreakDays
                    }.getOrDefault(-1)

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

                    // Veredicto de cierre, y en los DOS modos. Antes solo
                    // sonaba al superar en evaluación, y eso dejaba dos
                    // huecos: no superar terminaba en silencio —lo peor, porque
                    // el silencio no se distingue de un fallo de la app— y una
                    // sesión de práctica acababa sin remate.
                    //
                    // Aquí no se chiva nada: la sesión ya ha terminado y la
                    // pantalla de resultados dice lo mismo un instante
                    // después. Lo que no puede sonar, y no suena, es el
                    // veredicto de cada respuesta en evaluación.
                    //
                    // En práctica el porcentaje no desbloquea, pero informa
                    // igual: es el mismo 70 % de LevelRules en todas partes.
                    if (shouldUnlock) {
                        Vibracion.nivelSuperado(context)
                        Sonidos.nivelSuperado(context)
                    } else {
                        Vibracion.nivelNoSuperado(context)
                        Sonidos.nivelNoSuperado(context)
                    }

                    val rachaDespues = runCatching {
                        userStatsRepository.getUserStatsOnce().currentStreakDays
                    }.getOrDefault(rachaAntes)
                    val subio = rachaAntes >= 0 && rachaDespues > rachaAntes
                    println("🔥 Racha: $rachaAntes → $rachaDespues (sube: $subio)")
                    _uiState.update { it.copy(subioLaRacha = subio) }
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
            } finally {
                // Abre la puerta a navegar, y solo al terminar de escribir.
                //
                // Antes la pantalla navegaba al ver `isQuizCompleted`, que se
                // pone FUERA de esta corrutina, y lo que evitaba la carrera
                // era un `delay(100)` en la pantalla: si la escritura tardaba
                // mas, resultados se abria sobre datos a medias. Con esta
                // marca el orden esta garantizado.
                //
                // Va en `finally` a proposito: si la escritura falla, el
                // estudiante tiene que llegar a resultados igualmente, no
                // quedarse encallado en la ultima pregunta.
                if (isActuallyLastQuestion) {
                    _uiState.update { it.copy(listoParaResultados = true) }
                }
            }
        }

        // Actualizar estado de la UI
        _uiState.update {
            it.copy(
                currentQuestionIndex = it.currentQuestionIndex + 1,
                selectedOptionId = null,
                respuestaRevelada = false,
                puedeAvanzar = true,
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
        /** Si «Siguiente pregunta» acepta pulsaciones (ver MILIS_ANTES_DE_AVANZAR). */
        val puedeAvanzar: Boolean = true,
        val isQuizCompleted: Boolean = false,
        /** Si la racha subió con esta sesión: decide si hay pantalla de racha. */
        val subioLaRacha: Boolean = false,
        /** El progreso ya está escrito y se puede navegar a resultados. */
        val listoParaResultados: Boolean = false,
        val currentCompetence: Competence? = null
    ) {
        val hasValidCurrentQuestion: Boolean
            get() = questions.isNotEmpty() && currentQuestionIndex < questions.size

        val currentQuestion: Question?
            get() = questions.getOrNull(currentQuestionIndex)
    }


}