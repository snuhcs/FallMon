package com.example.fallmon.presentation

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import android.graphics.Color
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.example.fallmon.R
import com.example.fallmon.databinding.ActivitySettingBinding

class SettingActivity: ComponentActivity() {

    private val serviceViewModel: ServiceViewModel by viewModels()
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SettingActivity", "Created")
        setContentView(R.layout.activity_setting)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        binding.user = serviceViewModel

        Log.d("SettingActivity", "${serviceViewModel.getIsAlarmSoundOn()}")

        serviceViewModel.isAlarmSoundOnLiveData.observe(this, Observer{
            if(it) {
                binding.activitySettingSoundOn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5096C0"))
                binding.activitySettingSoundOff.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5A5A5A"))
            } else {
                binding.activitySettingSoundOff.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5096C0"))
                binding.activitySettingSoundOn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5A5A5A"))
            }
        })

        binding.activitySettingConfirm.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.activitySettingSoundOn.setOnClickListener {
            Log.d("SettingActivity", "Sound On")
            serviceViewModel.setIsAlarmSoundOn(true)
            Log.d("SettingActivity", "${serviceViewModel.getIsAlarmSoundOn()}")
        }

        binding.activitySettingSoundOff.setOnClickListener {
            Log.d("SettingActivity", "Sound Off")
            serviceViewModel.setIsAlarmSoundOn(false)
            Log.d("SettingActivity", "${serviceViewModel.getIsAlarmSoundOn()}")
        }
    }

    override fun onDestroy() {
        Log.d("SettingActivity", "Destroyed")
        super.onDestroy()
    }
}