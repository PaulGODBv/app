package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.mappers.ProgressMapper
import com.universidad.reta2.domain.models.LevelProgress
import com.universidad.reta2.domain.models.UserProgress
import com.universidad.reta2.domain.repositories.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
    private val userStatsDao: UserStatsDao
) : ProgressRepository {

    override suspend fun recordQuestionAttempt(
        questionId: Int,
        isCorrect: Boolean,
        timeSpentSeconds: Int
    ) {
        // Obtener username actual (podría venir de SessionManager)
        val username = "usuario_actual" // TODO: Obtener del usuario logueado

        progressDao.insertQuestionAttempt(
            questionId = questionId,
            username = username,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpentSeconds
        )
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
        val entity = ProgressMapper.toEntity(progress)
        progressDao.saveLevelProgress(entity)
    }

    override fun getUserProgress(): Flow<List<UserProgress>> {
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
