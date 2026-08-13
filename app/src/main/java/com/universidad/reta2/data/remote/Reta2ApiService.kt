package com.universidad.reta2.data.remote

import com.universidad.reta2.data.remote.dto.SyncReportRequest
import com.universidad.reta2.data.remote.dto.SyncReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Reta2ApiService {

    @POST("reports/sync/")
    suspend fun syncReport(
        @Body report: SyncReportRequest
    ): Response<SyncReportResponse>
}