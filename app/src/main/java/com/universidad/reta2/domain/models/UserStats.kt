package com.universidad.reta2.domain.models

data class UserStats(
    val totalQuestionsAnswered: Int = 0,
    val totalPracticeTimeSeconds: Int = 0, // Tiempo total en segundos
    val currentStreakDays: Int = 0,
    val lastPracticeDate: String = "", // Fecha del último día de práctica
    val dailyPracticeTime: Int = 0 // Tiempo de práctica del día actual en segundos
)