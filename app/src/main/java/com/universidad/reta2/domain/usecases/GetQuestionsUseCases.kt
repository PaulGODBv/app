package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.QuestionRepository
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(competenceId: Int, levelId: Int): List<Question> {
        println("🎯 GetQuestionsUseCase INVOCADO:")
        println("   - competenceId: $competenceId")
        println("   - levelId: $levelId")

        val questions = questionRepository.getQuestionsByCompetenceAndLevel(competenceId, levelId)

        println("📊 GetQuestionsUseCase RESULTADO:")
        println("   - Preguntas retornadas: ${questions.size}")
        println("   - IDs: ${questions.map { it.id }}")

        return questions
    }
}
