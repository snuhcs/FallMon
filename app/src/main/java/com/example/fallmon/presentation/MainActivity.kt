/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

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

interface ActivityResultListener {
    fun onActivityFinished()
    fun onActivityCreated()
}

class MainActivity : ComponentActivity(), ActivityResultListener {

    /**
     * intented : check if intented DetectedActivity
     * getActivityResult : to get result from DetectedActivity
     */
    /*
    private var intented: Boolean = false
    private val getActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            val confirmed = it.data?.getBooleanExtra("confirmed", false)
            intented = false
        }
    }

     */

    /**
     * Check if service is running
     */

    private var fallDetectionService : FallDetectionService? = null
    /*
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FallDetectionService.LocalBinder
            fallDetectionService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            //fallDetectionService = null
            isBound = false
        }
    }

     */

    private var isRunning: Boolean = false

    /**
     * Constructor
     * Start FallDetectionService
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Created")
        setContentView(R.layout.activity_main)

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
