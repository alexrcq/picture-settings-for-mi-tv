package com.alexrcq.tvpicturesettings.storage

import kotlinx.coroutines.flow.StateFlow

interface BacklightSettings {
    var backlight: Int
    var isAutoBacklightEnabled: Boolean
    val backlightAdjustAllowedFlow: StateFlow<Boolean>

    fun isBacklightAdjustAllowed(): Boolean = backlightAdjustAllowedFlow.value
}