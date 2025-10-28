package com.universidad.reta2.domain.models

data class Question(
    val id: Int,
    val text: String,
    val options: List<QuestionOption>,
    val correctOptionId: Int,
    val explanation: String="",
    val readingText: String="",
    val contextImage: String?=null
)

data class QuestionOption(
    val id: Int,
    val text: String,
    val originalOrder: Int
)