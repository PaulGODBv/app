package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.DailyProgress
import kotlinx.coroutines.flow.Flow

interface UserStatsRepository {

    // Obtener estadísticas del usuario actual
    fun getUserStats(): Flow<UserStats>

    // Actualizar estadísticas completas
    suspend fun updateUserStats(stats: UserStats)

    // Incrementar contadores específicos
    suspend fun addQuestionsAnswered(count: Int = 1)
    suspend fun addPracticeTime(seconds: Int)
    suspend fun incrementStreak()
    suspend fun resetStreak()

    // Actualizar fecha de última práctica
    suspend fun updateLastPracticeDate(date: String)

    // Reiniciar estadísticas diarias
    suspend fun resetDailyStats()

    // Obtener ranking o comparativas
    suspend fun getWeeklyProgress(): List<DailyProgress>
    suspend fun getAchievementsProgress(): Map<String, Float>
}
