package com.alexrcq.tvpicturesettings.ui.view

import android.app.Service
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

class ScreenFilter(context: Context): View(context) {

    companion object {
        const val MIN_POWER = 0
        const val MAX_POWER = 98
        const val SCALE_FACTOR = 100
        const val DEFAULT_ALPHA = 0.5f
    }

    init {
        alpha = DEFAULT_ALPHA
        setBackgroundColor(Color.BLACK)
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled) enable() else disable()
    }

    override fun isEnabled(): Boolean = parent != null

    fun setPower(power: Int) {
        alpha = power.coerceIn(MIN_POWER, MAX_POWER) / SCALE_FACTOR.toFloat()
    }

    private fun enable() {
        if (isEnabled) return
        val layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        windowManager.addView(this, layoutParams)
    }

    private fun disable() {
        if (isEnabled) {
            val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(this)
        }
    }
}