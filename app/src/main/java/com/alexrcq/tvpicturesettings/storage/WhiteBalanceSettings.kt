package com.alexrcq.tvpicturesettings.storage

interface WhiteBalanceSettings {
    fun setWhiteBalance(redGain: Int, greenGain: Int, blueGain: Int)

    fun resetWhiteBalance()
}