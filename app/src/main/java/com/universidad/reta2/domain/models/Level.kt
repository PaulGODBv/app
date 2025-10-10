package com.universidad.reta2.domain.models

data class Level(
    val id: Int,
    val name: String,
    val description: String,
    val questions: List<Question>,
    val isLocked: Boolean=false,
    val isCompleted: Boolean=false,
    val progress: Float=0f
)