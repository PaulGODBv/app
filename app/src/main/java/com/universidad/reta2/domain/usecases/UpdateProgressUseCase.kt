package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.repositories.ProgressRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val userStatsRepository: UserStatsRepository
) {

    suspend operator fun invoke(
        questionId: Int,
        isCorrect: Boolean,
        timeSpent: Int,
        levelId: Int,
        competenceId: Int? = null,
        isLevelCompleted: Boolean = false,
        levelScore: Int = 0,
        totalQuestions: Int = 0
    ) {
        try {
            // 🔥 DIAGNÓSTICO CRÍTICO - VER QUÉ PARÁMETROS LLEGAN
            println("🎯 UpdateProgressUseCase INVOCADO")
            println("   - questionId: $questionId")
            println("   - isCorrect: $isCorrect")
            println("   - levelId: $levelId")
            println("   - competenceId: $competenceId")
            println("   - isLevelCompleted: $isLevelCompleted")
            println("   - levelScore: $levelScore")
            println("   - totalQuestions: $totalQuestions")

             //1. Registrar la respuesta individual
            progressRepository.recordQuestionAttempt(
                questionId = questionId,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpent,
                levelId = levelId
            )

            // 2. Actualizar estadísticas del usuario
            val currentStats = userStatsRepository.getUserStats().first()

            val questionsToAdd= if (isCorrect) 1 else 0
            val updatedStats = currentStats.copy(
                totalQuestionsAnswered = currentStats.totalQuestionsAnswered + questionsToAdd,
                totalPracticeTimeSeconds = currentStats.totalPracticeTimeSeconds + timeSpent,
                dailyPracticeTime = currentStats.dailyPracticeTime + timeSpent
            )
            userStatsRepository.updateUserStats(updatedStats)

            // 3. Actualizar racha
            if (isCorrect) {
                userStatsRepository.incrementStreak()
            } else {
                userStatsRepository.resetStreak()
            }

            // 🔥 DIAGNÓSTICO ANTES DE COMPLETAR NIVEL
            println("🔍 ANTES de completeLevelAndUnlockNext:")
            println("   - isLevelCompleted: $isLevelCompleted")
            println("   - competenceId: $competenceId")
            println("   - Condición: ${isLevelCompleted && competenceId != null}")

            // USAR ProgressRepository PARA COMPLETAR NIVEL Y DESBLOQUEAR SIGUIENTE
            if (isLevelCompleted && competenceId != null) {
                println("🚀 EJECUTANDO completeLevelAndUnlockNext...")
                println("🎯 Completando nivel $levelId con score: $levelScore/$totalQuestions")

                val success = progressRepository.completeLevelAndUnlockNext(
                    competenceId = competenceId,
                    levelId = levelId,
                    score = levelScore,
                    totalQuestions = totalQuestions,
                    timeSpent = timeSpent
                )

                if (success) {
                    println("🎉 Nivel completado y siguiente nivel desbloqueado exitosamente")
                } else {
                    println("⚠️ Nivel completado pero no se pudo desbloquear siguiente nivel")
                }
            } else {
                println("❌ NO se ejecutó completeLevelAndUnlockNext porque:")
                println("   - isLevelCompleted es: $isLevelCompleted")
                println("   - competenceId es: $competenceId")
            }

            println("SUCCESS - UpdateProgressUseCase ejecutado correctamente")

        } catch (e: Exception) {
            println("ERROR - Error en UpdateProgressUseCase: ${e.message}")
            e.printStackTrace()
        }
    }
}
