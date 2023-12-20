package com.example.fallmon.presentation.retrofit.dto

import com.google.gson.annotations.SerializedName

data class FallHistoryDTO(
    val id: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("fall_type")
    val fallType: FallTypeDTO
)