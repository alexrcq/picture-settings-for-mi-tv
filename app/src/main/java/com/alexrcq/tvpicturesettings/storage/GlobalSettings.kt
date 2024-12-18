package com.alexrcq.tvpicturesettings.storage

import android.database.ContentObserver
import com.alexrcq.tvpicturesettings.util.toBoolean
import com.alexrcq.tvpicturesettings.util.toInt
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface GlobalSettings {
    fun putInt(key: String, value: Int)

    fun getInt(key: String): Int

    fun getInt(key: String, defValue: Int): Int

    fun putBoolean(key: String, value: Boolean) = putInt(key, value.toInt())

    fun getBoolean(key: String): Boolean = getInt(key).toBoolean()

    fun getBoolean(key: String, defValue: Boolean): Boolean = getInt(key, defValue.toInt()).toBoolean()

    fun registerContentObserver(observer: ContentObserver)

    fun unregisterContentObserver(observer: ContentObserver)

    fun intSetting(key: String) = object : ReadWriteProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int = getInt(key)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int): Unit = putInt(key, value)
    }

    fun booleanSetting(key: String) = object : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = getBoolean(key, false)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean): Unit = putBoolean(key, value)
    }
}