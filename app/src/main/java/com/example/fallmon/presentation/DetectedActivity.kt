package com.example.fallmon.presentation

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
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

    /*
     * for countdown automatic data sending
     */
    private lateinit var countDownTimer: CountDownTimer
    private val totalTimeInMillis: Long = 20000  // 20 seconds

    /*
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

    /*
     * put data sending was confirmed (and sended) to MainActivity & intent finish
     */
    private val getActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            val resultIntent = Intent()
            resultIntent.putExtra("confirmed", true)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private var fallDetectionService: FallDetectionService? = null

    /*
     * Set layout, buttons, views
     * get classification result from MainActivity
     * Decide falltype by result and show them.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected)
        Log.d("Detected onCreate", "created")

        val serviceIntent = Intent(this, FallDetectionService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        val confirmButton: ImageButton = findViewById(R.id.activity_detected_Confirm)
        val disconfirmButton: ImageButton = findViewById(R.id.activity_detected_Disconfirm)
        val fallText: TextView = findViewById(R.id.activity_detected_FallText)
        val fallTypeText: TextView = findViewById(R.id.activity_detected_FallTypeText)

        val classificationResult = intent.getDoubleArrayExtra("classificationResult")

        var fallType = FallType.FALL    // default : simple fall

        fallType = when(classificationResult?.max()) {
            classificationResult?.get(0) -> FallType.DROP_ATTACK
            //classificationResult?.get(1) -> FallType.NON_FALL
            classificationResult?.get(2) -> FallType.SLIPPING
            classificationResult?.get(3) -> FallType.STAND_PUSH
            classificationResult?.get(4) -> FallType.SUNKEN_FLOOR
            else -> FallType.NON_FALL
        }

        fallText.text = "낙상 감지!!!"
        fallText.gravity = Gravity.CENTER
        fallTypeText.text = fallType.strFall
        fallTypeText.gravity = Gravity.CENTER

        fall = FallHistory(TestUserID, fallType, Date())

        /*
         * alarm even if the volume is 0 in watch setting.
         */
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val ringtone = RingtoneManager.getRingtone(applicationContext, alarmSound)
        ringtone.audioAttributes = audioAttributes
        ringtone.play()

        countDown()

        /*
         * confirm button : confirm the fall and send data to server
         * disconfirm button : disconfirm the fall, not send data to server
         */
        confirmButton.setOnClickListener {
            confirmed(fall)
        }

        disconfirmButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("confirmed", false)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as FallDetectionService.LocalBinder
            fallDetectionService = binder.getService()
            fallDetectionService?.notifyActivityCreated()
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            fallDetectionService = null
        }
    }

    /*
     * countdown automatic data sending
     * remaining second is continuously put in UI
     */
    private fun countDown() {
        val timerText: TextView = findViewById(R.id.activity_detected_TimerText)
        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerText.text = "$secondsRemaining 초 후에\n낙상 기록이 전송됩니다."
                timerText.gravity = Gravity.CENTER
            }

            override fun onFinish() {
                confirmed(fall)
            }
        }

        countDownTimer.start()
    }

    /*
     * request server to put fall data
     */
    private fun request(fallHistory: FallHistory){
        try{
            val createdStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fallHistory.createdAt)
            Log.d("request", "createdStr: ${createdStr}")
            val server = retrofit.create(FallMonService::class.java)
            server.createFallHistory(fallHistory.id, createdStr, fallHistory.fallType.strFall).enqueue(object : Callback<FallHistoryDTO>{
                override fun onFailure(call: Call<FallHistoryDTO>, t: Throwable) {
                    Log.e("Retrofit", t.toString())
                }

                override fun onResponse(
                    call: Call<FallHistoryDTO>,
                    response: Response<FallHistoryDTO>
                ) {
                    Log.d("Retrofit", "success")
                    val id = response.body()?.id
                    val createdAt = response.body()?.createdAt
                    val fallType = response.body()?.fallType
                }
            })
        }catch(e:Exception){
            Log.e("Retrofit", e.toString())
        }

    }

    /*
     * run when that fall is confirmed
     * request data sending, then intent ConfirmedActivity
     */
    private fun confirmed(fall: FallHistory) {
        Log.d("Detected confirmed", "confirmed")
        request(fall)
        Log.d("Detected confirmed", "request")
        val intent = Intent(this, ConfirmedActivity::class.java)
        getActivityResult.launch(intent)
    }

    override fun onDestroy() {
        Log.d("DetectedActivity", "Destroyed")
        super.onDestroy()
        fallDetectionService?.notifyActivityFinished()
        countDownTimer.cancel()
        unbindService(serviceConnection)
    }
}