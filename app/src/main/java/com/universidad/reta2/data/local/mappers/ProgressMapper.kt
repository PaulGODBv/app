package com.universidad.reta2.data.mappers

import com.google.gson.Gson
import com.universidad.reta2.data.local.entities.LevelProgressEntity
import com.universidad.reta2.domain.models.LevelProgress

object ProgressMapper {
    private val gson = Gson()

    fun toDomain(entity: LevelProgressEntity): LevelProgress {
        return LevelProgress(
            competenceId = entity.competenceId,
            levelId = entity.levelId,
            questionsCompleted = entity.questionsCompleted,
            totalQuestions = entity.totalQuestions,
            isCompleted = entity.isCompleted,
            score = entity.score,
            timeSpent = entity.timeSpent,
            currentQuestionIndex = entity.currentQuestionIndex,
            answeredQuestions = gson.fromJson(entity.answeredQuestions, Array<Int>::class.java).toList(),
            correctAnswers = gson.fromJson(entity.correctAnswers, Array<Int>::class.java).toList()
        )
    }

    fun toEntity(domain: LevelProgress, username: String): LevelProgressEntity {
        return LevelProgressEntity(
            username = username,
            competenceId = domain.competenceId,
            levelId = domain.levelId,
            questionsCompleted = domain.questionsCompleted,
            totalQuestions = domain.totalQuestions,
            isCompleted = domain.isCompleted,
            score = domain.score,
            timeSpent = domain.timeSpent,
            currentQuestionIndex = domain.currentQuestionIndex,
            answeredQuestions = gson.toJson(domain.answeredQuestions),
            correctAnswers = gson.toJson(domain.correctAnswers)
        )
    }
}
