package com.universidad.reta2.domain.models

import androidx.annotation.DrawableRes

data class Competence (
    val id: Int,
    val name: String,
    val description: String,
    @DrawableRes val iconResId: Int,
    val levels: List<Level>,
    val totalProgress: Float = 0f
)
