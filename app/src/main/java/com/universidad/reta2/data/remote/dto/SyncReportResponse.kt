package com.universidad.reta2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SyncReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("report_id") val reportId: Int? = null
)