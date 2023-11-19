package com.example.fallmon.presentation

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.fallmon.R

class DetectedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected) // Set your layout here

        val confirmButton: ImageButton = findViewById(R.id.activity_detected_Confirm)
        val disconfirmButton: ImageButton = findViewById(R.id.activity_detected_Disconfirm)

        // Assume you have buttons with IDs 'functionButton1' and 'functionButton2'
        confirmButton.setOnClickListener {
            // Add functionality for button 1
        }

        disconfirmButton.setOnClickListener {
            // Add functionality for button 2
        }
    }
}