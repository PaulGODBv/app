package com.universidad.reta2.data.repositories

import android.content.Context
import com.universidad.reta2.data.local.dao.ProgressDao
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.data.remote.Reta2ApiService
import com.universidad.reta2.data.remote.dto.LevelProgressDto
import com.universidad.reta2.data.remote.dto.SyncReportRequest
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncRepository(
    private val apiService: Reta2ApiService,
    private val userStatsRepository: UserStatsRepository,
    private val competenceRepository: CompetenceRepository,
    private val progressDao: ProgressDao,
    @ApplicationContext private val context: Context
) {
    suspend fun syncToServer(): Result<String> {
        return try {
            val username = SessionManager.getCurrentUsername(context) ?: return Result.failure(
                Exception("No hay usuario logueado")
            )
            val email = SessionManager.getCurrentEmail(context) ?: ""

            // Obtener estadísticas actuales
            val stats = userStatsRepository.getUserStats().first()

            // Obtener progreso por nivel
            val competences = competenceRepository.getAllCompetences()
            val levelProgressList = mutableListOf<LevelProgressDto>()

            competences.forEach { competence ->
                competence.levels.forEach { level ->
                    val levelStats = progressDao.getLevelStats(username, level.id)
                    if (levelStats != null && levelStats.totalAttempts > 0) {
                        levelProgressList.add(
                            LevelProgressDto(
                                competenceName = competence.name,
                                levelName = level.name,
                                competenceId = competence.id,
                                levelId = level.id,
                                score = levelStats.correctAttempts,
                                totalQuestions = levelStats.totalAttempts,
                                isCompleted = level.isCompleted,
                                timeSpentSeconds = levelStats.averageTime.toInt() * levelStats.totalAttempts
                            )
                        )
                    }
                }
            }

            val request = SyncReportRequest(
                username = username,
                email = email,
                totalQuestionsAnswered = stats.totalQuestionsAnswered,
                totalPracticeTimeSeconds = stats.totalPracticeTimeSeconds,
                currentStreakDays = stats.currentStreakDays,
                dailyPracticeTimeSeconds = stats.dailyPracticeTime,
                levelProgress = levelProgressList
            )

            println("📤 Sincronizando con servidor...")
            println("   Usuario: $username")
            println("   Preguntas: ${stats.totalQuestionsAnswered}")
            println("   Niveles con progreso: ${levelProgressList.size}")

            val response = apiService.syncReport(request)

            if (response.isSuccessful) {
                val body = response.body()
                println("✅ Sync exitoso: ${body?.message}")
                Result.success(body?.message ?: "Sincronizado correctamente")
            } else {
                println("❌ Error en sync: ${response.code()} ${response.errorBody()?.string()}")
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }

        } catch (e: Exception) {
            println("❌ Error de conexión en sync: ${e.message}")
            Result.failure(e)
        }
    }
}