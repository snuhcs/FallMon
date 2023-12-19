package com.example.fallmon.presentation

import android.util.Log
import com.example.fallmon.presentation.retrofit.FallMonService
import com.example.fallmon.presentation.retrofit.dto.UserDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val BaseURL: String = "http://34.22.106.16:8080"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BaseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}