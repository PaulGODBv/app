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
    //
    // `'localtime'` no es opcional. Sin él, date(..., 'unixepoch') agrupa por
    // fecha UTC, y en Colombia (UTC-5) todo lo practicado a partir de las 19:00
    // se contaba en el día siguiente. Dos consecuencias medidas el 29/09/2026:
    // la gráfica semanal enseñaba las barras corridas un día respecto a la
    // realidad —una sesión del viernes por la noche aparecía en sábado— y
    // discrepaba de `last_practice_date`, que sí usa la fecha local del
    // dispositivo. Para un estudiante que repasa de noche, que es lo normal,
    // casi todas las barras caían en el día equivocado.
    //
    // La ventana pasa a 8 días por el mismo motivo: el día local más antiguo
    // que la pantalla pinta empieza hasta 14 horas antes del instante «hace 7
    // días», así que con 7 se recortaba. Las filas de más no molestan: la
    // tarjeta solo busca las siete fechas locales que dibuja.
    @Query("""
        SELECT
            date(attempted_at / 1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as questionsAnswered,
            SUM(time_spent_seconds) as practiceTime
        FROM question_attempts
        WHERE username = :username
        AND attempted_at >= strftime('%s', 'now', '-8 days') * 1000
        GROUP BY date(attempted_at / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """)
    suspend fun getWeeklyProgress(username: String): List<DailyProgress>

    // Crear estadísticas iniciales si no existen
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createInitialStats(stats: UserStatsEntity)
}