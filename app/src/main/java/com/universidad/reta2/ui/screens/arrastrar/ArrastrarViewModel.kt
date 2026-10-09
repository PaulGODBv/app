package com.universidad.reta2.ui.screens.arrastrar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Question
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
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.universidad.reta2.domain.LevelRules
import com.universidad.reta2.utils.Sonidos
import com.universidad.reta2.utils.Vibracion
import javax.inject.Inject

/**
 * Completar el texto arrastrando: el modo práctica de los niveles `arrastrar`.
 *
 * **De dónde salen los huecos.** De las preguntas de siempre. Cada una es una
 * frase del texto con un espacio en blanco —«Complete: 'Sadly, many people
 * today ___ know the differences.'»— y su opción correcta es lo que va en ese
 * espacio. El banco de palabras son esas correctas, barajadas.
 *
 * **Por qué cada frase va en su línea y no dentro del párrafo.** El pasaje
 * completo tiene los huecos numerados del (11) al (18), pero **nada en los
 * datos une una pregunta con su número de hueco**: habría que localizar el
 * trozo citado dentro del pasaje comparando texto, y una comilla o un espacio
 * de más rompería el ejercicio en silencio. Además, un hueco en medio de un
 * párrafo es una diana de unos 40 dp, por debajo de los 48 que pide Material
 * para algo que se toca. Con una frase por línea el hueco es ancho y no hay
 * nada que adivinar. El pasaje entero sigue a un toque, en el desplegable.
 *
 * Como toda práctica, no registra intentos ni desbloquea; sí suma tiempo,
 * racha y preguntas.
 */
@HiltViewModel
class ArrastrarViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userStatsRepository: UserStatsRepository,
    private val competenceRepository: CompetenceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArrastrarUiState())
    val uiState: StateFlow<ArrastrarUiState> = _uiState.asStateFlow()

    private var cronometro: Job? = null
    private var segundos = 0
    private var yaContabilizado = false

    fun cargar(competenceId: Int, levelId: Int) {
        if (_uiState.value.huecos.isNotEmpty()) return

        viewModelScope.launch {
            // Queda constancia de que se estuvo aqui: alimenta
            // «Continuar practicando».
            competenceRepository.marcarNivelPracticado(levelId)
            try {
                val preguntas = questionRepository
                    .getQuestionsByCompetenceAndLevel(competenceId, levelId)

                val huecos = preguntas.mapNotNull { aHueco(it) }

                _uiState.update {
                    it.copy(
                        huecos = huecos,
                        banco = huecos.map { h -> h.correcta }.shuffled(),
                        pasaje = preguntas.firstOrNull()?.readingText.orEmpty(),
                        cargando = false,
                        error = if (huecos.isEmpty())
                            "Este nivel no tiene frases con huecos" else null
                    )
                }
                arrancarCronometro()
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.message) }
            }
        }
    }

    /**
     * Parte el enunciado en lo que va antes y después del hueco.
     *
     * Quita el «Complete:» y las comillas que envuelven la frase, que son del
     * formato del banco y no del texto. Si una pregunta no trae hueco, se
     * descarta en vez de romper el ejercicio: mejor siete frases que ninguna.
     */
    private fun aHueco(pregunta: Question): HuecoUi? {
        val correcta = pregunta.options
            .firstOrNull { it.id == pregunta.correctOptionId }?.text ?: return null

        val limpio = pregunta.text.trim()
            .removePrefix("Complete:")
            .trim()
            .trim('\'', '"', '“', '”')
            .trim()

        val partes = limpio.split(Regex("_{2,}"), limit = 2)
        if (partes.size != 2) return null

        return HuecoUi(
            questionId = pregunta.id,
            antes = partes[0].trim(),
            despues = partes[1].trim(),
            correcta = correcta
        )
    }

    private fun arrancarCronometro() {
        cronometro?.cancel()
        cronometro = viewModelScope.launch {
            while (!_uiState.value.comprobado) {
                delay(1000)
                segundos++
            }
        }
    }

    /** Suelta una palabra en un hueco. Si el hueco ya tenía otra, la devuelve al banco. */
    fun colocar(questionId: Int, palabra: String) {
        val estado = _uiState.value
        if (estado.comprobado) return

        val anterior = estado.huecos.firstOrNull { it.questionId == questionId }?.colocada

        _uiState.update { s ->
            s.copy(
                huecos = s.huecos.map {
                    when {
                        it.questionId == questionId -> it.copy(colocada = palabra)
                        // La misma palabra no puede estar en dos huecos.
                        it.colocada == palabra -> it.copy(colocada = null)
                        else -> it
                    }
                },
                banco = (s.banco - palabra + listOfNotNull(anterior)).distinct()
            )
        }

        // Confirma que la palabra se quedó donde se soltó. Solo sonido, sin
        // vibración: ver el porqué en Sonidos.colocar().
        Sonidos.colocar(context)
    }

    /** Toca un hueco lleno y la palabra vuelve al banco. */
    fun quitar(questionId: Int) {
        val estado = _uiState.value
        if (estado.comprobado) return
        val palabra = estado.huecos.firstOrNull { it.questionId == questionId }?.colocada ?: return

        _uiState.update { s ->
            s.copy(
                huecos = s.huecos.map {
                    if (it.questionId == questionId) it.copy(colocada = null) else it
                },
                banco = (s.banco + palabra).distinct()
            )
        }
    }

    /** Comprueba todo de golpe, que es lo que distingue a este formato. */
    fun comprobar() {
        if (_uiState.value.comprobado) return
        cronometro?.cancel()
        _uiState.update { it.copy(comprobado = true) }

        // Un solo veredicto para una sola acción: aquí no se responde hueco
        // por hueco, se comprueba todo de golpe, así que un aviso por acierto
        // sería un traqueteo sin significado.
        //
        // Se usa el mismo 70 % que el resto de la aplicación, en lugar de
        // exigir el pleno, para que «superado» signifique lo mismo en todas
        // partes. Practicar no desbloquea nada, pero el veredicto sí informa.
        val estado = _uiState.value
        val porcentaje = if (estado.huecos.isEmpty()) {
            0
        } else {
            estado.aciertos * 100 / estado.huecos.size
        }
        if (porcentaje >= LevelRules.PASSING_PERCENTAGE) {
            Vibracion.nivelSuperado(context)
            Sonidos.nivelSuperado(context)
        } else {
            Vibracion.nivelNoSuperado(context)
            Sonidos.nivelNoSuperado(context)
        }

        contabilizar()
    }

    private fun contabilizar() {
        if (yaContabilizado) return
        yaContabilizado = true
        val aciertos = _uiState.value.aciertos
        viewModelScope.launch {
            runCatching {
                userStatsRepository.addPracticeTime(segundos)
                userStatsRepository.addQuestionsAnswered(aciertos)
                userStatsRepository.registerActivityToday()
            }.onFailure { println("⚠️ No se pudo anotar la práctica de arrastrar: ${it.message}") }
        }
    }

    data class HuecoUi(
        val questionId: Int,
        val antes: String,
        val despues: String,
        val correcta: String,
        val colocada: String? = null
    ) {
        val acertado: Boolean get() = colocada == correcta
    }

    data class ArrastrarUiState(
        val huecos: List<HuecoUi> = emptyList(),
        val banco: List<String> = emptyList(),
        val pasaje: String = "",
        val comprobado: Boolean = false,
        val cargando: Boolean = true,
        val error: String? = null
    ) {
        val todosLlenos: Boolean get() = huecos.isNotEmpty() && huecos.all { it.colocada != null }
        val aciertos: Int get() = huecos.count { it.acertado }
    }
}
