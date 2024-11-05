package com.alexrcq.tvpicturesettings.storage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.alexrcq.tvpicturesettings.App.Companion.applicationScope
import com.alexrcq.tvpicturesettings.util.TvUtils.isLocalDimmingSupported
import com.alexrcq.tvpicturesettings.util.TvUtils.isOled
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

private const val DEFAULT_COLOR_GAIN = 1024
private const val ACTION_NOTIFY_DOLBY_VISION =
    "com.android.tv.settings.partnercustomizer.tvsettingservice.NOTIFY_DOLBY_VISION"

open class MtkPictureSettings(context: Context, private val global: GlobalSettings) : PictureSettings {

    override var backlight: Int by global.intSetting(MtkGlobalKeys.PICTURE_BACKLIGHT)
    override var isHdrEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.PICTURE_LIST_HDR)
    override var isColorTuneEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.TV_PICTURE_COLOR_TUNE_ENABLE)
    override var isAutoBacklightEnabled: Boolean by global.booleanSetting(MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT)

    override var isLocalContrastEnabled: Boolean
        get() = global.getInt(MtkGlobalKeys.PICTURE_LOCAL_CONTRAST) == 2
        set(enabled) {
            val value = if (enabled) 2 else 0
            global.putInt(MtkGlobalKeys.PICTURE_LOCAL_CONTRAST, value)
        }

    override var isAdaptiveLumaEnabled: Boolean
        get() = global.getInt(MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL) == 2
        set(enabled) {
            val value = if (enabled) 2 else 0
            global.putInt(MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL, value)
        }

    override var pictureMode: Int
        get() = global.getInt(MtkGlobalKeys.PICTURE_MODE)
        set(pictureMode) {
            global.putInt(MtkGlobalKeys.PICTURE_MODE, pictureMode)
            setTemperatureByMode(pictureMode)
        }

    override val backlightAdjustAllowedFlow: StateFlow<Boolean> = callbackFlow {
        fun updateBacklightAdjustAllowed(isDolbyVision: Boolean, isAutoBacklight: Boolean) {
            trySend(!(isAutoBacklight || (isDolbyVision && !isLocalDimmingSupported() && !isOled(context))))
        }
        var isDolbyVision = false
        val dolbyVisionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_NOTIFY_DOLBY_VISION) {
                    isDolbyVision = intent.getBooleanExtra("DOLBY", false)
                    updateBacklightAdjustAllowed(isDolbyVision, isAutoBacklightEnabled)
                }
            }
        }
        val autoBacklightObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (uri?.lastPathSegment == MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT) {
                    updateBacklightAdjustAllowed(isDolbyVision, isAutoBacklightEnabled)
                }
            }
        }
        updateBacklightAdjustAllowed(isDolbyVision, isAutoBacklightEnabled)
        context.registerReceiver(dolbyVisionReceiver, IntentFilter(ACTION_NOTIFY_DOLBY_VISION))
        global.registerContentObserver(autoBacklightObserver)
        awaitClose {
            context.unregisterReceiver(dolbyVisionReceiver)
            global.unregisterContentObserver(autoBacklightObserver)
        }
    }.stateIn(applicationScope, SharingStarted.Eagerly, false)

    override fun setWhiteBalance(redGain: Int, greenGain: Int, blueGain: Int) = with(global) {
        putInt(MtkGlobalKeys.PICTURE_RED_GAIN, redGain)
        putInt(MtkGlobalKeys.PICTURE_GREEN_GAIN, greenGain)
        putInt(MtkGlobalKeys.PICTURE_BLUE_GAIN, blueGain)
    }

    override fun resetWhiteBalance() {
        setWhiteBalance(DEFAULT_COLOR_GAIN, DEFAULT_COLOR_GAIN, DEFAULT_COLOR_GAIN)
    }

    override fun resetToDefault() = with(global) {
        // same behaviour as the system app
        putInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT, getInt(MtkGlobalKeys.PICTURE_RESET_TO_DEFAULT) + 1)
        putInt(MtkGlobalKeys.PICTURE_AUTO_BACKLIGHT, 0)
    }

    private fun setTemperatureByMode(pictureMode: Int) {
        val temperature: Int? = when (pictureMode) {
            PICTURE_MODE_DEFAULT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_BRIGHT -> PICTURE_TEMPERATURE_WARM
            PICTURE_MODE_SPORT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_MOVIE -> PICTURE_TEMPERATURE_COLD
            else -> null
        }
        if (temperature != null) {
            global.putInt(MtkGlobalKeys.PICTURE_TEMPERATURE, temperature)
        }
    }

    companion object {
        const val PICTURE_MODE_DEFAULT = 7
        const val PICTURE_MODE_BRIGHT = 3
        const val PICTURE_MODE_SPORT = 2
        const val PICTURE_MODE_MOVIE = 9
        const val PICTURE_MODE_USER = 0
        const val PICTURE_TEMPERATURE_WARM = 1
        const val PICTURE_TEMPERATURE_COLD = 2
        const val PICTURE_TEMPERATURE_DEFAULT = 3
    }
}