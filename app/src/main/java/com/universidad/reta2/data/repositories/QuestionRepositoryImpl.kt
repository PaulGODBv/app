package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.dao.QuestionDao
import com.universidad.reta2.data.local.mappers.QuestionMapper
import com.universidad.reta2.data.source.CompetencyData
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.QuestionRepository
import javax.inject.Inject

/**
 * De donde salen las preguntas que se juegan.
 *
 * **Siempre de Room.** La red no entra aqui: la actualiza `ContentSyncRepository`
 * por su cuenta. Asi una practica nunca depende de que haya cobertura en ese
 * momento, que es lo que se pedia del modo offline.
 *
 * Mientras Room este vacio —primer arranque antes de la primera
 * sincronizacion— se sirve `CompetencyData`, que pasa a ser contenido de
 * arranque: lo que la app trae de fabrica para que se pueda practicar desde el
 * minuto cero. En cuanto el panel contesta una vez, deja de usarse.
 */
class QuestionRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionRepository {

    override suspend fun getQuestionsByCompetenceAndLevel(
        competenceId: Int,
        levelId: Int
    ): List<Question> = desdeRoom(levelId) ?: CompetencyData.getQuestionsByCompetenceAndLevel(
        competenceId, levelId
    )

    override suspend fun getQuestionCount(competenceId: Int, levelId: Int): Int =
        getQuestionsByCompetenceAndLevel(competenceId, levelId).size

    override suspend fun getRandomQuestions(
        competenceId: Int,
        levelId: Int,
        username: String,
        correctlyAnsweredIds: List<Int>
    ): List<Question> {
        val todas = getQuestionsByCompetenceAndLevel(competenceId, levelId)
        val cuantas = calculateQuestionCount(todas.size).coerceAtMost(todas.size)

        // Primero las que aun no se han acertado: repasar lo que ya se domina
        // aporta menos que insistir en lo que falla.
        val prioritarias = todas.filter { it.id !in correctlyAnsweredIds }
        val resto = todas.filter { it.id in correctlyAnsweredIds }

        val elegidas = mutableListOf<Question>()
        if (prioritarias.size >= cuantas) {
            elegidas.addAll(prioritarias.shuffled().take(cuantas))
        } else {
            elegidas.addAll(prioritarias)
            elegidas.addAll(resto.shuffled().take(cuantas - prioritarias.size))
        }

        return elegidas.shuffled()
    }

    /** Las preguntas del nivel en Room, o `null` si aun no hay ninguna. */
    private suspend fun desdeRoom(levelId: Int): List<Question>? {
        return try {
            val preguntas = questionDao.getQuestionsByLevel(levelId)
            if (preguntas.isEmpty()) return null

            val opciones = questionDao
                .getOptionsForQuestions(preguntas.map { it.id })
                .groupBy { it.questionId }

            preguntas.map { pregunta ->
                QuestionMapper.toDomain(pregunta, opciones[pregunta.id].orEmpty())
            }
        } catch (e: Exception) {
            // Un fallo leyendo la base no debe dejar al estudiante sin
            // practicar: se cae al contenido de arranque.
            println("⚠️ No se pudo leer el banco de Room: ${e.message}")
            null
        }
    }

    private fun calculateQuestionCount(total: Int): Int = when {
        total <= 15 -> 5
        total <= 30 -> 8
        total <= 60 -> 10
        total <= 100 -> 12
        else -> 15
    }
}
