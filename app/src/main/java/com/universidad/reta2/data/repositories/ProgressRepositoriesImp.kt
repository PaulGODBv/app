package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.mappers.ProgressMapper
import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.models.LevelStats
import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.data.local.entities.QuestionAttemptEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.content.Context
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext


class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
    private val userStatsDao: UserStatsDao,
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) : ProgressRepository {


    override suspend fun recordQuestionAttempt(
        questionId: Int,
        isCorrect: Boolean,
        timeSpentSeconds: Int
    ) {
        val username = sessionManager.getCurrentUsername(context) ?: "invitado"
        val levelId = 1 // TODO: determina el nivel actual si lo tienes disponible

        val attempt = QuestionAttemptEntity(
            username = username,
            questionId = questionId,
            levelId = levelId,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpentSeconds
        )

        progressDao.insertQuestionAttempt(attempt)
    }

    override suspend fun getLevelProgress(
        competenceId: Int,
        levelId: Int
    ): LevelProgress? {
        val username = "usuario_actual"
        return progressDao.getLevelProgress(username, competenceId, levelId)?.let {
            ProgressMapper.toDomain(it)
        }
    }

    override suspend fun saveLevelProgress(progress: LevelProgress) {
        val username = sessionManager.getCurrentUsername(context) ?: "invitado"
        val entity = ProgressMapper.toEntity(progress, username)
        progressDao.saveLevelProgress(entity)
    }


    override fun getUserProgress(): Flow<List<LevelProgress>> {
        val username = "usuario_actual"
        return progressDao.getUserProgress(username).map { entities ->
            entities.map { ProgressMapper.toDomain(it) }
        }
    }

    override suspend fun getLevelStats(levelId: Int): LevelStats {
        val username = "usuario_actual"
        return progressDao.getLevelStats(username, levelId)
    }

    override suspend fun resetLevelProgress(competenceId: Int, levelId: Int) {
        val username = "usuario_actual"
        progressDao.deleteLevelProgress(username, competenceId, levelId)
    }
}
