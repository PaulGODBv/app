package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.UserEntity
import com.universidad.reta2.data.local.entities.UserStatsEntity
import com.universidad.reta2.domain.models.User
import com.universidad.reta2.domain.models.UserStats

object UserMapper {

    fun toDomain(entity: UserEntity, stats: UserStatsEntity? = null): User {
        return User(
            username = entity.username,
            email = entity.email,
            passwordHash = entity.passwordHash,
            studentCode = entity.studentCode,
            studentProgram = entity.studentProgram
        )
    }

    fun toEntity(domain: User): UserEntity {
        return UserEntity(
            username = domain.username,
            email = domain.email,
            passwordHash = domain.passwordHash,
            studentCode = domain.studentCode,
            studentProgram = domain.studentProgram
        )
    }

    fun statsToDomain(entity: UserStatsEntity): UserStats {
        return UserStats(
            totalQuestionsAnswered = entity.totalQuestionsAnswered,
            totalPracticeTimeSeconds = entity.totalPracticeTimeSeconds,
            currentStreakDays = entity.currentStreakDays,
            lastPracticeDate = entity.lastPracticeDate,
            dailyPracticeTime = entity.dailyPracticeTime
        )
    }

    fun statsToEntity(username: String, domain: UserStats): UserStatsEntity {
        return UserStatsEntity(
            username = username,
            totalQuestionsAnswered = domain.totalQuestionsAnswered,
            totalPracticeTimeSeconds = domain.totalPracticeTimeSeconds,
            currentStreakDays = domain.currentStreakDays,
            lastPracticeDate = domain.lastPracticeDate,
            dailyPracticeTime = domain.dailyPracticeTime
        )
    }
}
