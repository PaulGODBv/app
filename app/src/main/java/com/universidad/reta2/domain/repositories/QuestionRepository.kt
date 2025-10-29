package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.Question

interface QuestionRepository {
    suspend fun getQuestionsByCompetenceAndLevel(competenceId: Int, levelId: Int): List<Question>
}