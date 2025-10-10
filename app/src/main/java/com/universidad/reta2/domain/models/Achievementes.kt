package com.universidad.reta2.domain.models

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentProgress: Float,
    val isUnlocked: Boolean = currentProgress >= 1f
)