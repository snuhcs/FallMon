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
import com.example.fallmon.R

/**
 *
 *  This whole activity is for just showing that the fall data is successfully sent to data
 *
 */


class ConfirmedActivity: ComponentActivity() {

    private lateinit var countDownTimer: CountDownTimer
    private val totalTimeInMillis: Long = 5000  // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmed) // Set your layout here

        val confirmButton: ImageButton = findViewById(R.id.activity_confirmed_Confirm)

        countDown()

        confirmButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    /**
     * Show to user 5 seconds that data is sent to server
     */
    private fun countDown() {
        val timerText: TextView = findViewById(R.id.activity_detected_TimerText)
        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                if(intent.getBooleanExtra("isSuccessful", false)){
                    timerText.text = "기록이 전송되었습니다.\n$secondsRemaining 초 후에 창을 닫습니다."
                    Log.d("Confirmed", "success")
                }else{
                    timerText.text = "기록 전송에 실패했습니다.\n$secondsRemaining 초 후에 창을 닫습니다."
                    Log.d("Confirmed", "fail")
                }
                timerText.gravity = Gravity.CENTER
            }

            override fun onFinish() {
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        countDownTimer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}