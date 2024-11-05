package com.alexrcq.tvpicturesettings

import android.app.Application
import androidx.preference.PreferenceManager
import com.alexrcq.tvpicturesettings.adblib.AdbClient
import com.alexrcq.tvpicturesettings.adblib.AdbShellCommandExecutor
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsWrapper
import com.alexrcq.tvpicturesettings.storage.MtkPictureSettings
import com.alexrcq.tvpicturesettings.storage.MtkTvSettings
import com.alexrcq.tvpicturesettings.storage.PicturePreferences
import com.alexrcq.tvpicturesettings.storage.TvSettings
import kotlinx.coroutines.MainScope
import timber.log.Timber

class App : Application() {

    lateinit var adbClient: AdbClient
    lateinit var darkModePreferences: DarkModePreferences
    lateinit var picturePreferences: PicturePreferences
    lateinit var tvSettings: TvSettings

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        darkModePreferences = DarkModePreferences(sharedPreferences)
        picturePreferences = PicturePreferences(sharedPreferences)
        adbClient = AdbShellCommandExecutor(this)
        val globalSettings = GlobalSettingsWrapper(contentResolver)
        tvSettings = MtkTvSettings(contentResolver, globalSettings, MtkPictureSettings(this, globalSettings))
    }

    companion object {
        val applicationScope = MainScope()
    }
}