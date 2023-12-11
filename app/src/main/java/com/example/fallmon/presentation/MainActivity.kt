/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.fallmon.presentation

import android.content.Intent
import android.os.Bundle
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
     * text_square for debugging
     */
    private lateinit var text_square: TextView

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
        text_square = findViewById(R.id.text_view1)

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
