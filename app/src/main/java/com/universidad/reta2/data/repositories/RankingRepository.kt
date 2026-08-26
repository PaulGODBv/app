package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.remote.Reta2ApiService
import com.universidad.reta2.data.remote.dto.RankingResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepository @Inject constructor(
    private val apiService: Reta2ApiService
) {
    suspend fun getGlobalRanking(username: String): Result<RankingResponse> {
        return try {
            val response = apiService.getRanking(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Error al obtener ranking: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
