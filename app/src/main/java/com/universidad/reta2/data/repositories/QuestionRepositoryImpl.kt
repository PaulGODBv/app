package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.source.CompetencyData
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.QuestionRepository
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor() : QuestionRepository {

    override suspend fun getQuestionsByCompetenceAndLevel(competenceId: Int, levelId: Int): List<Question> {
        println("🔍 QuestionRepositoryImpl.getQuestionsByCompetenceAndLevel:")
        println("   - competenceId: $competenceId")
        println("   - levelId: $levelId")

        val questions = CompetencyData.getQuestionsByCompetenceAndLevel(competenceId, levelId)

        println("📊 QuestionRepositoryImpl RESULTADO:")
        println("   - Preguntas desde CompetencyData: ${questions.size}")
        questions.forEachIndexed { index, question ->
            println("   ${index + 1}. ID: ${question.id}, Texto: '${question.text.take(30)}...'")
        }

        return questions
    }
}