package com.universidad.reta2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RankingResponse(
    @SerializedName("top10") val top10: List<RankingEntryDto>,
    @SerializedName("current_user") val currentUser: CurrentUserRankingDto?
)

data class RankingEntryDto(
    @SerializedName("position") val position: Int,
    @SerializedName("username") val username: String,
    @SerializedName("total_questions_answered") val totalQuestionsAnswered: Int,
    @SerializedName("is_current_user") val isCurrentUser: Boolean
)

data class CurrentUserRankingDto(
    @SerializedName("username") val username: String,
    @SerializedName("position") val position: Int?,
    @SerializedName("total_questions_answered") val totalQuestionsAnswered: Int?,
    @SerializedName("in_top10") val inTop10: Boolean
)
