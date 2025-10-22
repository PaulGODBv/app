package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.source.CompetencyData
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.QuestionRepository
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor() : QuestionRepository {

    override suspend fun getQuestionsByCompetenceAndLevel(competenceId: String, levelId: Int): List<Question> {
        return CompetencyData.getQuestionsByCompetenceAndLevel(competenceId, levelId)
    }
}