package com.universidad.reta2.domain.services

import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.mappers.UserStatsMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsInitializer @Inject constructor(
    private val userStatsDao: UserStatsDao
) {
    suspend fun initializeUserStats(username: String) {
        val existingStats = userStatsDao.getUserStatsSync(username)
        if (existingStats == null) {
            val initialStats = UserStatsMapper.createInitialStats(username)
            userStatsDao.updateUserStats(initialStats)
        }
    }
}