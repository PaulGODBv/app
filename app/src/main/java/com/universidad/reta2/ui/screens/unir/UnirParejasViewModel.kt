package com.universidad.reta2.ui.screens.unir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.QuestionRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unir parejas: el modo práctica de los niveles marcados como `unir`.
 *
 * **De dónde salen las parejas.** De las preguntas de siempre, sin datos
 * nuevos. En «Feelings» las cinco preguntas comparten las mismas ocho opciones
 * y cada una tiene una correcta distinta: enunciados a un lado, respuestas al
 * otro, y las opciones que no son correctas de nadie hacen de distractores.
 *
 * **Un ejercicio, varias preguntas.** Es la diferencia de fondo con el modo de
 * siempre: aquí la sesión no avanza pregunta a pregunta, sino que el nivel
 * entero es un tablero. Por eso no reutiliza `QuestionViewModel`.
 *
 * Como toda práctica, no registra intentos ni desbloquea nada; sí suma tiempo
 * y racha, porque practicar es practicar.
 */
@HiltViewModel
class UnirParejasViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userStatsRepository: UserStatsRepository,
    private val competenceRepository: CompetenceRepository
) : ViewModel() {

    private companion object {
        /**
         * Lo que dura el rojo de un fallo.
         *
         * Corto a propósito: el usuario lo pidió de un segundo o menos. Lo
         * justo para que se vea que no era, sin castigar ni cortar el ritmo.
         */
        const val MILIS_DE_FALLO = 700L
    }

    private val _uiState = MutableStateFlow(UnirUiState())
    val uiState: StateFlow<UnirUiState> = _uiState.asStateFlow()

    private var trabajoDelFallo: Job? = null
    private var cronometro: Job? = null
    private var segundos = 0
    private var yaContabilizado = false

    fun cargar(competenceId: Int, levelId: Int) {
        if (_uiState.value.parejas.isNotEmpty()) return

        viewModelScope.launch {
            // Queda constancia de que se estuvo aqui: alimenta
            // «Continuar practicando».
            competenceRepository.marcarNivelPracticado(levelId)
            try {
                val preguntas = questionRepository
                    .getQuestionsByCompetenceAndLevel(competenceId, levelId)

                val parejas = preguntas.mapNotNull { pregunta ->
                    val correcta = pregunta.options
                        .firstOrNull { it.id == pregunta.correctOptionId }
                        ?: return@mapNotNull null
                    ParejaUi(
                        questionId = pregunta.id,
                        enunciado = pregunta.text,
                        correcta = correcta.text,
                        explicacion = pregunta.explanation
                    )
                }

                // El banco son todas las opciones distintas del nivel. Las que
                // no son correctas de ninguna pareja se quedan como
                // distractores, que es lo que hace que haya que pensar.
                val banco = preguntas
                    .flatMap { it.options }
                    .map { it.text }
                    .distinct()
                    .shuffled()

                _uiState.update {
                    it.copy(
                        parejas = parejas.shuffled(),
                        banco = banco,
                        cargando = false,
                        error = if (parejas.isEmpty()) "Este nivel no tiene parejas que unir" else null
                    )
                }
                arrancarCronometro()
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    private fun arrancarCronometro() {
        cronometro?.cancel()
        cronometro = viewModelScope.launch {
            while (!_uiState.value.terminado) {
                delay(1000)
                segundos++
            }
        }
    }

    /** Toca un enunciado. Vuelve a tocarlo y se deselecciona. */
    fun seleccionarFila(questionId: Int) {
        val estado = _uiState.value
        if (estado.parejas.any { it.questionId == questionId && it.resuelta }) return
        _uiState.update {
            it.copy(filaSeleccionada = if (it.filaSeleccionada == questionId) null else questionId)
        }
    }

    /**
     * Toca una palabra del banco.
     *
     * Sin fila seleccionada no hace nada: el gesto es elegir primero el
     * enunciado y después la respuesta, como se pidió.
     */
    fun elegirPalabra(palabra: String) {
        val estado = _uiState.value
        val questionId = estado.filaSeleccionada ?: return
        val pareja = estado.parejas.firstOrNull { it.questionId == questionId } ?: return
        if (pareja.resuelta) return

        if (palabra == pareja.correcta) {
            val parejas = estado.parejas.map {
                if (it.questionId == questionId) it.copy(resuelta = true) else it
            }
            val terminado = parejas.all { it.resuelta }
            _uiState.update {
                it.copy(
                    parejas = parejas,
                    // La palabra acertada sale del banco: ya está colocada.
                    banco = it.banco - palabra,
                    filaSeleccionada = null,
                    fallo = null,
                    aciertos = it.aciertos + 1,
                    terminado = terminado
                )
            }
            if (terminado) cerrar()
        } else {
            // Rojo breve y las dos siguen en juego: un fallo aquí informa, no
            // elimina. Es lo que separa practicar de evaluarse.
            _uiState.update { it.copy(fallo = questionId to palabra, fallos = it.fallos + 1) }
            trabajoDelFallo?.cancel()
            trabajoDelFallo = viewModelScope.launch {
                delay(MILIS_DE_FALLO)
                _uiState.update { it.copy(fallo = null) }
            }
        }
    }

    /**
     * Cierra el ejercicio: suma tiempo, racha y preguntas, nunca intentos.
     *
     * Practicar cuenta como actividad —es la decision del usuario— y solo deja
     * fuera lo que mueve el progreso del nivel: el intento registrado, que es
     * lo que alimenta el porcentaje y el desbloqueo.
     *
     * `yaContabilizado` evita contar dos veces si la pantalla se recompone o
     * el usuario vuelve atrás y entra de nuevo sin que el ViewModel muera.
     */
    private fun cerrar() {
        if (yaContabilizado) return
        yaContabilizado = true
        cronometro?.cancel()
        viewModelScope.launch {
            runCatching {
                userStatsRepository.addPracticeTime(segundos)
                // Cada pareja resuelta es una pregunta acertada. Faltaba:
                // unir sumaba tiempo y racha pero no preguntas.
                userStatsRepository.addQuestionsAnswered(_uiState.value.aciertos)
                userStatsRepository.registerActivityToday()
            }.onFailure { println("⚠️ No se pudo anotar la práctica de unir: ${it.message}") }
        }
    }

    data class ParejaUi(
        val questionId: Int,
        val enunciado: String,
        val correcta: String,
        val explicacion: String = "",
        val resuelta: Boolean = false
    )

    data class UnirUiState(
        val parejas: List<ParejaUi> = emptyList(),
        val banco: List<String> = emptyList(),
        /** Enunciado elegido, a la espera de su palabra. */
        val filaSeleccionada: Int? = null,
        /** Pareja marcada en rojo ahora mismo: enunciado y palabra. */
        val fallo: Pair<Int, String>? = null,
        val aciertos: Int = 0,
        val fallos: Int = 0,
        val cargando: Boolean = true,
        val terminado: Boolean = false,
        val error: String? = null
    ) {
        val resueltas: Int get() = parejas.count { it.resuelta }
    }
}
