package com.universidad.reta2.data.preferences

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class UserStats(
    val totalQuestionsAnswered: Int = 0,
    val totalPracticeTimeSeconds: Int = 0, // Tiempo total en segundos
    val currentStreakDays: Int = 0,
    val lastPracticeDate: String = "", // Fecha del último día de práctica
    val dailyPracticeTime: Int = 0 // Tiempo de práctica del día actual en segundos
)

class UserStatsManager(private val context: Context, private val username: String) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_stats", Context.MODE_PRIVATE)
    private val STATS_KEY = "user_stats_$username"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getUserStats(): UserStats {
        try {
            val statsJson = prefs.getString(STATS_KEY, null)
            if (statsJson != null) {
                val statsObj = JSONObject(statsJson)
                return UserStats(
                    totalQuestionsAnswered = statsObj.optInt("totalQuestionsAnswered", 0),
                    totalPracticeTimeSeconds = statsObj.optInt("totalPracticeTimeSeconds", 0),
                    currentStreakDays = statsObj.optInt("currentStreakDays", 0),
                    lastPracticeDate = statsObj.optString("lastPracticeDate", ""),
                    dailyPracticeTime = statsObj.optInt("dailyPracticeTime", 0)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return UserStats()
    }

    fun saveUserStats(stats: UserStats) {
        try {
            val statsObj = JSONObject().apply {
                put("totalQuestionsAnswered", stats.totalQuestionsAnswered)
                put("totalPracticeTimeSeconds", stats.totalPracticeTimeSeconds)
                put("currentStreakDays", stats.currentStreakDays)
                put("lastPracticeDate", stats.lastPracticeDate)
                put("dailyPracticeTime", stats.dailyPracticeTime)
            }
            prefs.edit().putString(STATS_KEY, statsObj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addQuestionAnswered() {
        val stats = getUserStats()
        val updatedStats = stats.copy(
            totalQuestionsAnswered = stats.totalQuestionsAnswered + 1
        )
        saveUserStats(updatedStats)
    }

    fun addPracticeTime(seconds: Int) {
        val stats = getUserStats()
        val today = dateFormat.format(Date())

        // Si es un nuevo día, reiniciar el tiempo diario y actualizar la racha
        val updatedStats = if (stats.lastPracticeDate != today) {
            val newStreakDays = if (isConsecutiveDay(stats.lastPracticeDate, today)) {
                stats.currentStreakDays + 1
            } else {
                1 // Reiniciar racha si no es consecutivo
            }

            stats.copy(
                totalPracticeTimeSeconds = stats.totalPracticeTimeSeconds + seconds,
                dailyPracticeTime = seconds,
                currentStreakDays = newStreakDays,
                lastPracticeDate = today
            )
        } else {
            // Mismo día, solo agregar tiempo
            stats.copy(
                totalPracticeTimeSeconds = stats.totalPracticeTimeSeconds + seconds,
                dailyPracticeTime = stats.dailyPracticeTime + seconds
            )
        }

        saveUserStats(updatedStats)
    }

    private fun isConsecutiveDay(lastDate: String, currentDate: String): Boolean {
        if (lastDate.isEmpty()) return true

        try {
            val last = dateFormat.parse(lastDate)
            val current = dateFormat.parse(currentDate)
            if (last != null && current != null) {
                val diffInDays = ((current.time - last.time) / (1000 * 60 * 60 * 24)).toInt()
                return diffInDays == 1
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    fun formatPracticeTime(seconds: Int): String {
        return when {
            seconds >= 3600 -> {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
            }
            seconds >= 60 -> {
                val minutes = seconds / 60
                "${minutes}m"
            }
            else -> "${seconds}s"
        }
    }

    fun getDailyPracticeTimeFormatted(): String {
        val stats = getUserStats()
        val today = dateFormat.format(Date())

        return if (stats.lastPracticeDate == today) {
            formatPracticeTime(stats.dailyPracticeTime)
        } else {
            "0s"
        }
    }

    fun getTotalPracticeTimeFormatted(): String {
        val stats = getUserStats()
        return formatPracticeTime(stats.totalPracticeTimeSeconds)
    }

    // Migrar estadísticas del username anterior al nuevo
    fun migrateStatsFromOldUsername(oldUsername: String) {
        try {
            val oldKey = "user_stats_$oldUsername"
            val oldStatsJson = prefs.getString(oldKey, null)

            if (oldStatsJson != null) {
                val oldStatsObj = JSONObject(oldStatsJson)
                val currentStats = getUserStats()

                // Combinar estadísticas (mantener las más altas)
                val combinedStats = UserStats(
                    totalQuestionsAnswered = maxOf(
                        currentStats.totalQuestionsAnswered,
                        oldStatsObj.optInt("totalQuestionsAnswered", 0)
                    ),
                    totalPracticeTimeSeconds = maxOf(
                        currentStats.totalPracticeTimeSeconds,
                        oldStatsObj.optInt("totalPracticeTimeSeconds", 0)
                    ),
                    currentStreakDays = maxOf(
                        currentStats.currentStreakDays,
                        oldStatsObj.optInt("currentStreakDays", 0)
                    ),
                    lastPracticeDate = if (currentStats.lastPracticeDate.isNotEmpty()) {
                        currentStats.lastPracticeDate
                    } else {
                        oldStatsObj.optString("lastPracticeDate", "")
                    },
                    dailyPracticeTime = if (currentStats.lastPracticeDate == dateFormat.format(Date())) {
                        currentStats.dailyPracticeTime
                    } else {
                        oldStatsObj.optInt("dailyPracticeTime", 0)
                    }
                )

                // Guardar estadísticas combinadas
                saveUserStats(combinedStats)

                // Eliminar estadísticas anteriores
                prefs.edit().remove(oldKey).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Verificar si existen estadísticas para el username anterior
    fun hasStatsForOldUsername(oldUsername: String): Boolean {
        val oldKey = "user_stats_$oldUsername"
        val oldStatsJson = prefs.getString(oldKey, null)
        return oldStatsJson != null
    }
}
