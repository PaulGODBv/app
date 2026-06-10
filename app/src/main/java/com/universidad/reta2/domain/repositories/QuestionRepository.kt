package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.Question

interface QuestionRepository {
    suspend fun getQuestionsByCompetenceAndLevel(
        competenceId: Int,
        levelId: Int
    ): List<Question>

    suspend fun getRandomQuestions(
        competenceId: Int,
        levelId: Int,
        username: String,
        correctlyAnsweredIds: List<Int>
    ): List<Question>

    suspend fun getQuestionCount(
        competenceId: Int,
        levelId: Int
    ): Int
}
