package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.UserStatsEntity
import com.universidad.reta2.domain.models.UserStats

object UserStatsMapper {

    fun toDomain(entity: UserStatsEntity): UserStats {
        return UserStats(
            totalQuestionsAnswered = entity.totalQuestionsAnswered,
            totalPracticeTimeSeconds = entity.totalPracticeTimeSeconds,
            currentStreakDays = entity.currentStreakDays,
            lastPracticeDate = entity.lastPracticeDate,
            dailyPracticeTime = entity.dailyPracticeTime,
            maxStreakDays = entity.maxStreakDays,
            maxDailyPracticeTime = entity.maxDailyPracticeTime
        )
    }

    fun toEntity(domain: UserStats, username: String): UserStatsEntity {
        return UserStatsEntity(
            username = username,
            totalQuestionsAnswered = domain.totalQuestionsAnswered,
            totalPracticeTimeSeconds = domain.totalPracticeTimeSeconds,
            currentStreakDays = domain.currentStreakDays,
            lastPracticeDate = domain.lastPracticeDate,
            dailyPracticeTime = domain.dailyPracticeTime,
            maxStreakDays = domain.maxStreakDays,
            maxDailyPracticeTime = domain.maxDailyPracticeTime
        )
    }

    // Para crear estadísticas iniciales
    fun createInitialStats(username: String): UserStatsEntity {
        return UserStatsEntity(
            username = username,
            totalQuestionsAnswered = 0,
            totalPracticeTimeSeconds = 0,
            currentStreakDays = 0,
            lastPracticeDate = "",
            dailyPracticeTime = 0,
            maxStreakDays = 0,
            maxDailyPracticeTime = 0
        )
    }
}