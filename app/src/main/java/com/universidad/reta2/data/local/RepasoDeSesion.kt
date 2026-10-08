package com.universidad.reta2.data.local

import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.models.QuestionOption
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lo que se respondió en la sesión que acaba de terminar.
 *
 * La pantalla de resultados recibe por la ruta solo números —aciertos, total y
 * tiempo—, y `QuestionUiState` borra `selectedOptionId` en cada pregunta. Con
 * eso no se puede explicar nada: no queda ni qué se preguntó ni qué se
 * respondió. `question_attempts` guarda acierto o fallo, pero tampoco la opción
 * elegida, así que leer de Room daría «la correcta era X» y nunca «elegiste Y».
 *
 * De ahí este registro en memoria, igual que [CatalogoCache] y
 * [EstadisticasCache]: el cuestionario lo va llenando y resultados lo lee. No
 * es un dato que haya que persistir —el repaso solo tiene sentido en la
 * pantalla inmediatamente posterior— y guardarlo obligaría a una migración
 * para algo que caduca en treinta segundos.
 *
 * Va marcado con el nivel al que pertenece: si se llega a resultados por otro
 * camino, el repaso de otro nivel no se cuela.
 */
@Singleton
class RepasoDeSesion @Inject constructor() {

    private data class Entrada(val nivel: Int, val respuestas: List<RespuestaDeSesion>)

    private val entrada = AtomicReference<Entrada?>(null)

    /** Abre un registro nuevo y descarta el anterior. */
    fun empezar(nivel: Int) {
        entrada.set(Entrada(nivel, emptyList()))
    }

    /**
     * Anota una respuesta.
     *
     * Solo se anota si hay un registro abierto para ese nivel. Sin la
     * comprobación, una respuesta rezagada de un cuestionario anterior podría
     * aparecer en el repaso del siguiente.
     */
    fun anotar(nivel: Int, respuesta: RespuestaDeSesion) {
        entrada.updateAndGet { actual ->
            if (actual == null || actual.nivel != nivel) actual
            else actual.copy(respuestas = actual.respuestas + respuesta)
        }
    }

    /** El repaso de [nivel], o vacío si el registro es de otro o no hay. */
    fun obtener(nivel: Int): List<RespuestaDeSesion> =
        entrada.get()?.takeIf { it.nivel == nivel }?.respuestas.orEmpty()
}

/** Una pregunta de la sesión con lo que se respondió en ella. */
data class RespuestaDeSesion(
    val pregunta: Question,
    val idElegido: Int?,
    val acerto: Boolean
) {
    val opcionElegida: QuestionOption? get() = pregunta.options.firstOrNull { it.id == idElegido }

    val opcionCorrecta: QuestionOption?
        get() = pregunta.options.firstOrNull { it.id == pregunta.correctOptionId }

    /**
     * La letra con que se enseña una opción, deducida de su posición.
     *
     * A propósito no se calcula desde `QuestionOption.id`: en el banco que
     * viene del panel el id es la posición más uno, pero en el contenido de
     * arranque no está garantizado. La posición en la lista sí lo está, y es lo
     * que el estudiante ve en pantalla.
     */
    fun letraDe(opcion: QuestionOption?): String? {
        if (opcion == null) return null
        val i = pregunta.options.indexOfFirst { it.id == opcion.id }
        return if (i >= 0) ('A' + i).toString() else null
    }
}
