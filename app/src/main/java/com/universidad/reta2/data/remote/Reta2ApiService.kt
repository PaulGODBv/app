package com.universidad.reta2.data.remote

import com.universidad.reta2.data.remote.dto.LevelContentResponse
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

    /**
     * Contenido de un nivel tal y como lo publica el panel.
     *
     * Se baja nivel a nivel y no todo de golpe porque son trece llamadas
     * pequenas en lugar de una grande: si una falla, lo demas ya esta guardado
     * y el estudiante puede practicar igual.
     */
    @GET("questions/{levelId}/")
    suspend fun getLevelContent(
        @Path("levelId") levelId: Int
    ): Response<LevelContentResponse>
}
