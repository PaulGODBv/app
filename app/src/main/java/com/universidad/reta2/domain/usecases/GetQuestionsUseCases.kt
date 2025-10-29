package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.QuestionRepository
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val questionRepository: QuestionRepository
) {
    suspend operator fun invoke(competenceId: Int, levelId: Int): List<Question> {
        return questionRepository.getQuestionsByCompetenceAndLevel(competenceId, levelId)
    }
}
