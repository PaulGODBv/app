package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.source.CompetencyData
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.QuestionRepository
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor() : QuestionRepository {

    override suspend fun getQuestionsByCompetenceAndLevel(competenceId: Int, levelId: Int): List<Question> {
        return CompetencyData.getQuestionsByCompetenceAndLevel(competenceId, levelId)
    }

    override suspend fun getQuestionCount(competenceId: Int, levelId: Int): Int {
        return CompetencyData.getQuestionsByCompetenceAndLevel(competenceId, levelId).size
    }

    override suspend fun getRandomQuestions(
        competenceId: Int,
        levelId: Int,
        username: String,
        correctlyAnsweredIds: List<Int>
    ): List<Question> {
        val allQuestions = CompetencyData.getQuestionsByCompetenceAndLevel(competenceId, levelId)

        val count = calculateQuestionCount(allQuestions.size).coerceAtMost(allQuestions.size)

        val priorityPool = allQuestions.filter { it.id !in correctlyAnsweredIds }
        val secondaryPool = allQuestions.filter { it.id in correctlyAnsweredIds }

        val selected = mutableListOf<Question>()

        if (priorityPool.size >= count) {
            selected.addAll(priorityPool.shuffled().take(count))
        } else {
            selected.addAll(priorityPool)
            val remaining = count - priorityPool.size
            selected.addAll(secondaryPool.shuffled().take(remaining))
        }

        return selected.shuffled()
    }

    private fun calculateQuestionCount(total: Int): Int {
        return when {
            total <= 15 -> 5
            total <= 30 -> 8
            total <= 60 -> 10
            total <= 100 -> 12
            else -> 15
        }
    }
}
