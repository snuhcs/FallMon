package com.example.fallmon.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fallmon.R
import com.example.fallmon.presentation.retrofit.FallMonService
import com.example.fallmon.presentation.retrofit.dto.FallHistoryDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date

class DetectedActivity : ComponentActivity() {

    private lateinit var countDownTimer: CountDownTimer
    private val totalTimeInMillis: Long = 20000  // 20 seconds
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

    private val getActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            val resultIntent = Intent()
            resultIntent.putExtra("confirmed", true)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected) // Set your layout here
        Log.d("Detected onCreate", "created")
        val confirmButton: ImageButton = findViewById(R.id.activity_detected_Confirm)
        val disconfirmButton: ImageButton = findViewById(R.id.activity_detected_Disconfirm)
        val fallText: TextView = findViewById(R.id.activity_detected_FallText)
        val fallTypeText: TextView = findViewById(R.id.activity_detected_FallTypeText)

        val classificationResult = intent.getDoubleArrayExtra("classificationResult")

        var fallType = FallType.FALL

        fallType = when(classificationResult?.max()) {
            classificationResult?.get(0) -> FallType.DROP_ATTACK
            //classificationResult?.get(1) -> FallType.NON_ATTACK
            classificationResult?.get(2) -> FallType.SLIPPING
            classificationResult?.get(3) -> FallType.STAND_PUSH
            classificationResult?.get(4) -> FallType.SUNKEN_FLOOR
            else -> FallType.NON_FALL
        }
        Log.d("Detected onCreate", "fall Type ${fallType.strFall}")

        fallText.text = "낙상 감지!!!"
        fallText.gravity = Gravity.CENTER
        fallTypeText.text = fallType.strFall
        fallTypeText.gravity = Gravity.CENTER

        fall = FallHistory(TestUserID, fallType, Date())
        countDown()

        confirmButton.setOnClickListener {
            sendRequest(fall)
        }

        disconfirmButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("confirmed", false)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun countDown() {
        val timerText: TextView = findViewById(R.id.activity_detected_TimerText)
        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerText.text = "$secondsRemaining 초 후에\n낙상 기록이 전송됩니다."
                timerText.gravity = Gravity.CENTER
            }

            override fun onFinish() {
                sendRequest(fall)
            }
        }

        countDownTimer.start()
    }

    private fun request(fallHistory: FallHistory){
        try{
            val createdStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fallHistory.createdAt)
            Log.d("request", "createdStr: ${createdStr}")
            val server = retrofit.create(FallMonService::class.java)
            server.createFallHistory(fallHistory.id, createdStr, fallHistory.fallType.strFall).enqueue(object : Callback<FallHistoryDTO>{
                override fun onFailure(call: Call<FallHistoryDTO>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                    requestFailed()
                }

                override fun onResponse(
                    call: Call<FallHistoryDTO>,
                    response: Response<FallHistoryDTO>
                ) {
                    if(response.isSuccessful){
                        Log.d("Retrofit", "Success: response code ${response.code()}")
                        requestSucceeded()
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
    private fun sendRequest(fall: FallHistory) {
        Log.d("Detected confirmed", "confirmed")
        request(fall)
        Log.d("Detected confirmed", "request")
    }

    private fun requestSucceeded(){
        val intent = Intent(this, ConfirmedActivity::class.java)
        intent.putExtra("isSuccessful", true)
        getActivityResult.launch(intent)
    }

    private fun requestFailed(){
        val intent = Intent(this, ConfirmedActivity::class.java)
        intent.putExtra("isSuccessful", false)
        getActivityResult.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}