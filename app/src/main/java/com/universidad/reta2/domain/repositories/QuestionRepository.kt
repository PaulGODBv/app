package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.Question

interface QuestionRepository {
    suspend fun getQuestionsByCompetenceAndLevel(competenceId: String, levelId: Int): List<Question>
}