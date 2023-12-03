package com.example.fallmon.presentation

import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.fallmon.R

class DetectedActivity : ComponentActivity() {

    private lateinit var countDownTimer: CountDownTimer
    private val totalTimeInMillis: Long = 20000  // 20 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected) // Set your layout here

        val confirmButton: ImageButton = findViewById(R.id.activity_detected_Confirm)
        val disconfirmButton: ImageButton = findViewById(R.id.activity_detected_Disconfirm)
        val fallText: TextView = findViewById(R.id.activity_detected_FallText)
        val fallTypeText: TextView = findViewById(R.id.activity_detected_FallTypeText)

        val classificationResult = intent.getDoubleArrayExtra("classificationResult")

        var fallType = "단순 낙상"

        fallType = when(classificationResult?.max()) {
            classificationResult?.get(0) -> "쓰러짐 (Drop attack)"
            //classificationResult?.get(1) -> "비낙상 (Nonfall)"
            classificationResult?.get(2) -> "미끄러짐 (Slipping)"
            classificationResult?.get(3) -> "넘어짐 (Stand push)"
            classificationResult?.get(4) -> "헛디딤 (Sunken floor)"
            else -> "비낙상 (Nonfall)"
        }

        fallText.text = "낙상 감지!!!"
        fallText.gravity = Gravity.CENTER
        fallTypeText.text = "$fallType"
        fallTypeText.gravity = Gravity.CENTER

        countDown()

        // Assume you have buttons with IDs 'functionButton1' and 'functionButton2'
        confirmButton.setOnClickListener {
            // Add functionality for button 1
            confirmed()
        }

        disconfirmButton.setOnClickListener {
            // Add functionality for button 2
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
                confirmed()
            }
        }

        countDownTimer.start()
    }

    private fun confirmed() {

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}