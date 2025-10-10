package com.universidad.reta2.domain.models

data class LevelStats(
    val levelId: Int,
    val totalAttempts: Int,
    val correctAttempts: Int,
    val averageTime: Double
) {
    val accuracy: Double
        get() = if (totalAttempts > 0) correctAttempts.toDouble() / totalAttempts else 0.0
}