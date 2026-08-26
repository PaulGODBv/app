package com.universidad.reta2.data.remote

import com.universidad.reta2.data.remote.dto.SyncReportRequest
import com.universidad.reta2.data.remote.dto.SyncReportResponse
import com.universidad.reta2.data.remote.dto.RankingResponse
import retrofit2.Response
import retrofit2.http.*

interface Reta2ApiService {

    @POST("reports/sync/")
    suspend fun syncReport(
        @Body report: SyncReportRequest
    ): Response<SyncReportResponse>

    @GET("ranking/")
    suspend fun getRanking(
        @Query("username") username: String
    ): Response<RankingResponse>
}
