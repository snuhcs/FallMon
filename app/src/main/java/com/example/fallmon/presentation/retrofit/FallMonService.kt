package com.example.fallmon.presentation.retrofit

import com.example.fallmon.presentation.retrofit.dto.FallHistoryDTO
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

public interface FallMonService{
    @POST("/api/fall_history")
    fun createFallHistory(
        @Query("user_id") id: String,
        @Query("created_at", encoded = true) createdAt: String,
        @Query("fall_type") fallType: String
    ) : Call<FallHistoryDTO>

}