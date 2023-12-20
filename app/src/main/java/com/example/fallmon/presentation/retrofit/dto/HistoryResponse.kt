package com.example.fallmon.presentation.retrofit.dto

class HistoryResponse : ArrayList<HistoryResponse.HistoryResponseItem>(){
    data class HistoryResponseItem(
        val created_at: String,
        val fall_type: FallType,
        val id: Int
    ) {
        data class FallType(
            val id: Int,
            val name: String
        )
    }
}