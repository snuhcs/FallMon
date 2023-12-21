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
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.example.fallmon.R

class SettingActivity: ComponentActivity() {

    /**
     * Get or Init shared preference data
     * To get Application Setting from user setting
     * If not initialized, init by case
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
     * To change Application Setting
     * Save right away after change
     */
    private fun setSharedPreferences(preference:String, key: String, value: String): String{
        val userPreferences = getSharedPreferences(preference, MODE_PRIVATE)
        val editor = userPreferences.edit()
        editor.putString(key, value)
        editor.apply()
        return value
    }

    /**
     * Most of the code in this function is UI setting.
     * Color of button - #5096C0 : On, #5A5A5A : Off
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SettingActivity", "Created")
        setContentView(R.layout.activity_setting)

        val buttonConfirm : ImageButton = findViewById(R.id.activity_setting_confirm)
        val buttonSoundOn : Button = findViewById(R.id.activity_setting_sound_on)
        val buttonSoundOff : Button = findViewById(R.id.activity_setting_sound_off)

        val isSoundOn = getOrSetSharedPreferences("User", "IS_SOUND_ON", true.toString()).toBoolean()

        if(isSoundOn) {
            buttonSoundOn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5096C0"))
            buttonSoundOff.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5A5A5A"))
        } else {
            buttonSoundOn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5A5A5A"))
            buttonSoundOff.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5096C0"))
        }

        buttonConfirm.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        buttonSoundOn.setOnClickListener {
            Log.d("SettingActivity", "Sound On")
            setSharedPreferences("User", "IS_SOUND_ON", true.toString())
            buttonSoundOn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5096C0"))
            buttonSoundOff.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5A5A5A"))
        }

        buttonSoundOff.setOnClickListener {
            Log.d("SettingActivity", "Sound Off")
            setSharedPreferences("User", "IS_SOUND_ON", false.toString())
            buttonSoundOn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5A5A5A"))
            buttonSoundOff.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5096C0"))
        }
    }

    override fun onDestroy() {
        Log.d("SettingActivity", "Destroyed")
        super.onDestroy()
    }
}