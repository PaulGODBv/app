package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.mappers.UserStatsMapper
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.repositories.UserStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UserStatsRepositoriesImp @Inject constructor(
    private val userStatsDao: UserStatsDao
) : UserStatsRepository {

        override fun getUserStats(): Flow<UserStats> {
            val username = getCurrentUsername()
            return userStatsDao.getUserStats(username).map { entity ->
                entity?.let { UserStatsMapper.toDomain(it) } ?: UserStats() // Devuelve UserStats vacío si no hay datos
            }
        }

    override suspend fun updateUserStats(stats: UserStats) {
        val username = getCurrentUsername()
        val entity = UserStatsMapper.toEntity(stats, username)
        userStatsDao.updateUserStats(entity)
    }

    override suspend fun addQuestionsAnswered(count: Int) {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val updatedStats = currentStats.copy(
            totalQuestionsAnswered = currentStats.totalQuestionsAnswered + count
        )
        userStatsDao.updateUserStats(updatedStats)
    }

    override suspend fun addPracticeTime(seconds: Int) {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val updatedStats = currentStats.copy(
            totalPracticeTimeSeconds = currentStats.totalPracticeTimeSeconds + seconds,
            dailyPracticeTime = currentStats.dailyPracticeTime + seconds
        )
        userStatsDao.updateUserStats(updatedStats)
    }

    override suspend fun incrementStreak() {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val today = getCurrentDate()

        val updatedStats = if (shouldIncrementStreak(currentStats.lastPracticeDate, today)) {
            currentStats.copy(
                currentStreakDays = currentStats.currentStreakDays + 1,
                lastPracticeDate = today
            )
        } else {
            currentStats.copy(lastPracticeDate = today)
        }

        userStatsDao.updateUserStats(updatedStats)
    }

    override suspend fun resetStreak() {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val updatedStats = currentStats.copy(
            currentStreakDays = 0,
            lastPracticeDate = getCurrentDate()
        )
        userStatsDao.updateUserStats(updatedStats)
    }

    override suspend fun updateLastPracticeDate(date: String) {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val updatedStats = currentStats.copy(lastPracticeDate = date)
        userStatsDao.updateUserStats(updatedStats)
    }

    override suspend fun resetDailyStats() {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val updatedStats = currentStats.copy(dailyPracticeTime = 0)
        userStatsDao.updateUserStats(updatedStats)
    }

    override suspend fun getWeeklyProgress(): List<DailyProgress> {
        val username = getCurrentUsername()
        return userStatsDao.getWeeklyProgress(username)
    }

    override suspend fun getAchievementsProgress(): Map<String, Float> {
        val username = getCurrentUsername()
        val stats = userStatsDao.getUserStatsSync(username)

        return mapOf(
            "questions_100" to (stats.totalQuestionsAnswered / 100f).coerceAtMost(1f),
            "practice_5_hours" to (stats.totalPracticeTimeSeconds / (5 * 3600f)).coerceAtMost(1f),
            "streak_7_days" to (stats.currentStreakDays / 7f).coerceAtMost(1f),
            "daily_30_min" to (stats.dailyPracticeTime / 1800f).coerceAtMost(1f)
        )
    }

    // Métodos auxiliares privados
    private fun getCurrentUsername(): String {
        // TODO: Implementar lógica para obtener el usuario actual
        // Por ahora retornamos un valor por defecto
        return "usuario_actual"
    }

    private fun getCurrentDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }

    private fun shouldIncrementStreak(lastPracticeDate: String, currentDate: String): Boolean {
        return if (lastPracticeDate.isEmpty()) {
            true // Primera vez que practica
        } else {
            val lastDate = LocalDate.parse(lastPracticeDate)
            val current = LocalDate.parse(currentDate)
            lastDate.plusDays(1) == current // Solo incrementa si practica días consecutivos
        }
    }
}
