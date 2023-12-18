/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.fallmon.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.fallmon.R

interface ActivityResultListener {
    fun onActivityFinished()
    fun onActivityCreated()
}

class MainActivity : ComponentActivity(), ActivityResultListener {

    /*
     * Check if service is running
     */
    private var isRunning : Boolean = false

    /*
     * intented : check if intented DetectedActivity
     * getActivityResult : to get result from DetectedActivity
     */
    private var intented: Boolean = false
    private val getActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            val confirmed = it.data?.getBooleanExtra("confirmed", false)
            intented = false
        }
    }

    /*
     * Constructor
     * Start FallDetectionService
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textDetecting : TextView = findViewById(R.id.activity_main_detecting)
        val buttonPower : ImageButton = findViewById(R.id.activity_main_power)
        val buttonHistory : ImageButton = findViewById(R.id.activity_main_history)
        val buttonSetting : ImageButton = findViewById(R.id.activity_main_setting)

        textDetecting.gravity = Gravity.CENTER
        if(!isRunning) {
            textDetecting.text = "낙상 감지 꺼짐"
        } else {
            textDetecting.text = "낙상 감지 켜짐"
        }


        buttonPower.setOnClickListener {
            isRunning = !isRunning
            if(isRunning) {
                //runFallDetectionService()
                buttonPower.setColorFilter(Color.parseColor("#FF0000"))
                textDetecting.text = "낙상 감지 켜짐"
            } else {
                stopFallDetectionService()
                buttonPower.setColorFilter(Color.parseColor("#22E531"))
                textDetecting.text = "낙상 감지 꺼짐"
            }
        }

        buttonHistory.setOnClickListener {

        }

        buttonSetting.setOnClickListener {

        }

    }

    private fun stopFallDetectionService() {

    }

    private fun runFallDetectionService() {
        val fallDetectionService = FallDetectionService()
        fallDetectionService.setActivityResultListener(this)
        val serviceIntent = Intent(this, FallDetectionService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onActivityFinished() {
    }

    override fun onActivityCreated() {
    }
}
