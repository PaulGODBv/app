package com.universidad.reta2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SyncReportRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("total_questions_answered") val totalQuestionsAnswered: Int,
    @SerializedName("total_practice_time_seconds") val totalPracticeTimeSeconds: Int,
    @SerializedName("current_streak_days") val currentStreakDays: Int,
    // La racha viva se reinicia; esta no. El panel la necesita para mostrar
    // los logros de racha en el detalle del estudiante, que de otro modo
    // tendria que adivinarla del historico de sincronizaciones.
    @SerializedName("max_streak_days") val maxStreakDays: Int,
    @SerializedName("daily_practice_time_seconds") val dailyPracticeTimeSeconds: Int,
    @SerializedName("academic_program") val academicProgram: String = "",
    @SerializedName("app_version") val appVersion: String = "1.0",
    @SerializedName("level_progress") val levelProgress: List<LevelProgressDto> = emptyList()
)

data class LevelProgressDto(
    @SerializedName("competence_name") val competenceName: String,
    @SerializedName("level_name") val levelName: String,
    @SerializedName("competence_id") val competenceId: Int,
    @SerializedName("level_id") val levelId: Int,
    @SerializedName("score") val score: Int,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("time_spent_seconds") val timeSpentSeconds: Int
)