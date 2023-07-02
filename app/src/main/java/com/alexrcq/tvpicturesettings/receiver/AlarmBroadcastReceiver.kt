package com.alexrcq.tvpicturesettings.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.App.Companion.applicationScope
import com.alexrcq.tvpicturesettings.helper.AlarmScheduler
import com.alexrcq.tvpicturesettings.helper.AlarmScheduler.Companion.ACTION_ALARM_TRIGGERED
import com.alexrcq.tvpicturesettings.helper.AlarmScheduler.Companion.EXTRA_ALARM_TYPE
import com.alexrcq.tvpicturesettings.helper.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class AlarmBroadcastReceiver : BroadcastReceiver() {

    private lateinit var appSettings: AppSettings

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("intent action: ${intent.action}")
        appSettings = AppSettings(context)
        if (intent.action == ACTION_ALARM_TRIGGERED) {
            val alarmType =
                intent.getSerializableExtra(EXTRA_ALARM_TYPE) as AlarmScheduler.AlarmType?
            handleAlarm(alarmType)
        }
    }

    private fun handleAlarm(alarmType: AlarmScheduler.AlarmType?) {
        Timber.d("handling the alarm: $alarmType")
        when (alarmType) {
            AlarmScheduler.AlarmType.DARK_MODE_ALARM -> onDarkModeAlarmTriggered()
            AlarmScheduler.AlarmType.DAY_MODE_ALARM -> onDayModeAlarmTriggered()
            else -> Unit
        }
    }

    private fun onDarkModeAlarmTriggered() {
        applicationScope.launch {
            mutex.withLock {
                appSettings.isDarkModeEnabled = true
            }
        }
    }

    private fun onDayModeAlarmTriggered() {
        applicationScope.launch {
            mutex.withLock {
                // waiting for the system settings service wake up
                delay(DAY_MODE_DELAY)
                appSettings.isDarkModeEnabled = false
            }
        }
    }

    companion object {
        private const val DAY_MODE_DELAY = 2000L
        private val mutex = Mutex()
    }
}