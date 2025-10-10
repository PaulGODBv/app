package com.universidad.reta2.domain.models

data class DailyProgress(
    val date: String,
    val questionsAnswered: Int,
    val practiceTime: Int // en segundos
)