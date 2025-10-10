package com.universidad.reta2.domain.models

data class Competence (
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val levels: List<Level>,
    val totalProgress: Float = 0f
)
