package com.universidad.reta2.domain.usecases

import android.content.Context
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.repositories.QuestionRepository
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class GetQuestionsUseCase @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val progressDao: ProgressDao,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        competenceId: Int,
        levelId: Int
    ): List<Question> {
        return try {
            val username = SessionManager.getCurrentUsername(context) ?: "usuario_invitado"

            val correctlyAnsweredIds = progressDao.getCorrectlyAnsweredQuestionIds(username, levelId)

            questionRepository.getRandomQuestions(
                competenceId = competenceId,
                levelId = levelId,
                username = username,
                correctlyAnsweredIds = correctlyAnsweredIds
            )
        } catch (e: Exception) {
            questionRepository.getQuestionsByCompetenceAndLevel(
                competenceId,
                levelId
            )
        }
    }
}
