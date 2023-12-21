package com.example.fallmon.presentation

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fallmon.R
import com.example.fallmon.presentation.retrofit.FallMonService
import com.example.fallmon.presentation.retrofit.dto.UserDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

interface ActivityResultListener {
    fun onActivityFinished()
    fun onActivityCreated()
}

class MainActivity : ComponentActivity(), ActivityResultListener {


    /**
     * Check if service is running
     */
    private var fallDetectionService : FallDetectionService? = null
    private var isRunning: Boolean = false

    /**
     * Get or Init shared preference data
     * To get User ID/PW
     * If not initialized, randomly init.
     */
    private fun getOrSetSharedPreferences(preference:String, key: String, value: String): String{
        val userPreferences = getSharedPreferences(preference, MODE_PRIVATE)
        val prevVal = userPreferences.getString(key, "").toString()
        // if value has already set in sharedPreferences, just return value
        if (prevVal.isNotBlank()) return prevVal
        // else set key to value passed by argument
        val editor = userPreferences.edit()
        editor.putString(key, value)
        editor.apply()
        return value
    }

    /**
     *  initialize local user ID and PW
     */
    private fun setupLocalUser(){
        val userID: String = getOrSetSharedPreferences("User", "ID", UUID.randomUUID().toString())
        val userPW: String = getOrSetSharedPreferences("User", "PW", UUID.randomUUID().toString())
    }

    /**
     * initialize remote user (server) ID and PW
     */
    private fun setupRemoteUser(){
        val preferences = getSharedPreferences("User", MODE_PRIVATE)
        val userID = preferences.getString("ID", "") ?: ""
        val userPW = preferences.getString("PW", "") ?: ""
        Log.d("Main", "setup remote user $userID $userPW")
        request(userID, userPW)
    }

    private fun requestSucceeded(){
        Log.d("Main", "User Created")
    }

    private  fun requestFailed(){
        Log.d("Main", "User creation failed")
    }

    /**
     * Request server to create User by ID & PW
     * Nothing will be changed when already exists.
     */
    private fun request(userID: String, userPW: String){
        try{
            val retrofit = RetrofitClient.instance
            val api = retrofit.create(FallMonService::class.java)
            api.createUser(userID, userPW).enqueue(object :
                Callback<UserDTO> {
                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                    requestFailed()
                }
                override fun onResponse(
                    call: Call<UserDTO>,
                    response: Response<UserDTO>
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

    /**
     * Constructor
     * Start FallDetectionService
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Created")
        setContentView(R.layout.activity_main)
        setupLocalUser()
        setupRemoteUser()

        val textDetecting : TextView = findViewById(R.id.activity_main_detecting)
        val buttonPower : ImageButton = findViewById(R.id.activity_main_power)
        val buttonHistory : ImageButton = findViewById(R.id.activity_main_history)
        val buttonSetting : ImageButton = findViewById(R.id.activity_main_setting)

        fallDetectionService = FallDetectionService()

        textDetecting.gravity = Gravity.CENTER
        if(!isRunning) {
            textDetecting.text = "낙상 감지 꺼짐"
        } else {
            textDetecting.text = "낙상 감지 켜짐"
        }

        buttonPower.setOnClickListener {
            isRunning = !isRunning
            if(isRunning) {
                runFallDetectionService()
                buttonPower.setColorFilter(Color.parseColor("#FF0000"))
                textDetecting.text = "낙상 감지 켜짐"
            } else {
                stopFallDetectionService()
                buttonPower.setColorFilter(Color.parseColor("#22E531"))
                textDetecting.text = "낙상 감지 꺼짐"
            }
        }

        buttonHistory.setOnClickListener {
            runHistoryActivity()
        }

        buttonSetting.setOnClickListener {
            runSettingActivity()
        }

    }

    /**
     * Run / Stop Activities or Service
     */
    private fun runHistoryActivity() {
        val intent = Intent(this, HistoryActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun runSettingActivity() {
        val intent = Intent(this, SettingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun stopFallDetectionService() {
        fallDetectionService?.stopService()
        val serviceIntent = Intent(this, FallDetectionService::class.java)
        stopService(serviceIntent)
    }

    private fun runFallDetectionService() {
        fallDetectionService?.setActivityResultListener(this)
        val serviceIntent = Intent(this, FallDetectionService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onActivityFinished() {
    }

    override fun onActivityCreated() {
    }

    override fun onDestroy() {
        Log.d("MainActivity", "Destroyed")
        super.onDestroy()
    }
}
