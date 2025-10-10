package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.services.QuestionRandomizer
import javax.inject.Inject

class GetRandomizedQuestionsUseCase @Inject constructor() {

    /**
     * Obtiene preguntas aleatorizadas para un cuestionario
     */
    suspend operator fun invoke(questions: List<Question>): List<Question> {
        return QuestionRandomizer.randomizeQuestions(questions)
    }

    /**
     * Obtiene una sola pregunta aleatorizada
     */
    suspend operator fun invoke(question: Question): Question {
        return QuestionRandomizer.randomizeQuestion(question)
    }
}