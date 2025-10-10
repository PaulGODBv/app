package com.universidad.reta2.domain.models

data class LevelProgress(
    val competenceId: Int,
    val levelId: Int,
    val questionsCompleted: Int,
    val totalQuestions: Int,
    val isCompleted: Boolean,
    val score: Int,
    val timeSpent: Int,
    val currentQuestionIndex: Int,
    val answeredQuestions: List<Int>,
    val correctAnswers: List<Int>
)