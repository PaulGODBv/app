package com.universidad.reta2.data.local.dao

import androidx.room.*
import com.universidad.reta2.data.local.entities.UserStatsEntity
import com.universidad.reta2.domain.models.DailyProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    // Obtener estadísticas del usuario (Flow para observación reactiva)
    @Query("SELECT * FROM user_stats WHERE username = :username")
    fun getUserStats(username: String): Flow<UserStatsEntity?>

    // Obtener estadísticas de forma síncrona (para operaciones de actualización)
    @Query("SELECT * FROM user_stats WHERE username = :username")
    suspend fun getUserStatsSync(username: String): UserStatsEntity

    // Insertar o actualizar estadísticas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserStats(stats: UserStatsEntity)

    // Operaciones específicas para optimización
    @Query("UPDATE user_stats SET total_questions_answered = total_questions_answered + :count WHERE username = :username")
    suspend fun incrementQuestionsAnswered(username: String, count: Int = 1)

    @Query("UPDATE user_stats SET total_practice_time_seconds = total_practice_time_seconds + :seconds, daily_practice_time = daily_practice_time + :seconds WHERE username = :username")
    suspend fun addPracticeTime(username: String, seconds: Int)

    @Query("UPDATE user_stats SET current_streak_days = current_streak_days + 1, last_practice_date = :date WHERE username = :username")
    suspend fun incrementStreak(username: String, date: String)

    @Query("UPDATE user_stats SET current_streak_days = 0, last_practice_date = :date WHERE username = :username")
    suspend fun resetStreak(username: String, date: String)

    // Obtener progreso semanal
    @Query("""
        SELECT 
            date(attempted_at / 1000, 'unixepoch') as date,
            COUNT(*) as questionsAnswered,
            SUM(time_spent_seconds) as practiceTime
        FROM question_attempts 
        WHERE username = :username 
        AND attempted_at >= strftime('%s', 'now', '-7 days') * 1000
        GROUP BY date(attempted_at / 1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getWeeklyProgress(username: String): List<DailyProgress>

    // Crear estadísticas iniciales si no existen
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createInitialStats(stats: UserStatsEntity)
}