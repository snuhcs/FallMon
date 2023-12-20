package com.example.fallmon.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.fallmon.R
import com.example.fallmon.presentation.retrofit.FallMonService
import com.example.fallmon.presentation.retrofit.dto.FallHistoryDTO
import com.example.fallmon.presentation.retrofit.dto.HistoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat

class HistoryActivity: ComponentActivity() {

    private lateinit var textHistoryRecord : TextView

    /**
     * for data sending
     */
    private val BaseURL: String = "http://34.22.106.16:8080"
    private val TestUserID: String = "234532"
    private val FallHistoryURL: String = "$BaseURL/api/fall_history"
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BaseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private lateinit var fall: FallHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        textHistoryRecord = findViewById(R.id.activity_history_record)
        val buttonBack : ImageButton = findViewById(R.id.activity_history_back)

        buttonBack.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        request(TestUserID)
    }

    /**
     * request server to put fall data
     */
    private fun request(user_id: String){
        try{
            Log.d("history request", "user_id : $TestUserID")
            val server = retrofit.create(FallMonService::class.java)
            server.getFallHistory(user_id).enqueue(object :
                Callback<HistoryResponse> {
                override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                    requestFailed()
                }

                override fun onResponse(
                    call: Call<HistoryResponse>,
                    response: Response<HistoryResponse>
                ) {
                    if(response.isSuccessful){
                        Log.d("Retrofit", "Success: response code ${response.code()}")
                        requestSucceeded(response.body())
                    }else{
                        Log.d("Retrofit", "Failed: response code ${response.code()}")
                        requestFailed()
                    }
                }
            })
        }catch(e:Exception){
            Log.e("Retrofit", "Error: ${e.message}")
            requestFailed()
        }

    }

    private fun requestSucceeded(arrayHistory: HistoryResponse?) {
        if(arrayHistory == null) {
            textHistoryRecord.text = "최근 낙상 기록이 없습니다"
        } else {
            val lastRecentHistory = arrayHistory.last()
            val createAt = lastRecentHistory.created_at
            val createAtDate = createAt.subSequence(0, 10)
            val createAtTime = createAt.subSequence(11, 19)
            val fallType = lastRecentHistory.fall_type
            textHistoryRecord.text = "날짜 : ${createAtDate}\n시각 : ${createAtTime}\n낙상 종류 : ${fallType.name}"
        }
    }

    private fun requestFailed() {
        textHistoryRecord.text = "서버와의 연결에 실패했습니다"
    }
}