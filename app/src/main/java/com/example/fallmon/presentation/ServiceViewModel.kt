package com.example.fallmon.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class ServiceViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val IS_ALARM_SOUND_ON = "isAlarmSoundOn"

    private var isAlarmSoundOn = savedStateHandle.get<Boolean>(IS_ALARM_SOUND_ON) ?: true
        set(value) {
            savedStateHandle[IS_ALARM_SOUND_ON] = value
            field = value
        }

    val isAlarmSoundOnLiveData: LiveData<Boolean> = savedStateHandle.getLiveData(IS_ALARM_SOUND_ON, true)

    fun getIsAlarmSoundOn(): Boolean {
        return isAlarmSoundOn
    }
    fun setIsAlarmSoundOn(value: Boolean) {
        isAlarmSoundOn = value
    }
}