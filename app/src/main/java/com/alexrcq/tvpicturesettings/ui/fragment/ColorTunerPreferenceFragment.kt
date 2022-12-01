package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.COLOR_TUNER_BLUE_GAIN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.COLOR_TUNER_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.COLOR_TUNER_GAIN_RESET
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.COLOR_TUNER_GREEN_GAIN
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.COLOR_TUNER_RED_GAIN
import com.alexrcq.tvpicturesettings.storage.ColorTuner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ColorTunerPreferenceFragment : BasePreferenceFragment(R.xml.color_tuner_prefs) {

    private var colorTunerEnabledPref: SwitchPreference? = null
    private var redGainPref: SeekBarPreference? = null
    private var greenGainPref: SeekBarPreference? = null
    private var blueGainPref: SeekBarPreference? = null

    @Inject
    lateinit var colorTuner: ColorTuner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorTunerEnabledPref = findPreference(COLOR_TUNER_ENABLED)
        redGainPref = findPreference(COLOR_TUNER_RED_GAIN)
        greenGainPref = findPreference(COLOR_TUNER_GREEN_GAIN)
        blueGainPref = findPreference(COLOR_TUNER_BLUE_GAIN)
        findPreference<Preference>(COLOR_TUNER_GAIN_RESET)?.setOnPreferenceClickListener {
            colorTuner.resetGain()
            updateUi()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        colorTunerEnabledPref?.isChecked = colorTuner.isColorTuneEnabled
        redGainPref?.value = colorTuner.redGain
        greenGainPref?.value = colorTuner.greenGain
        blueGainPref?.value = colorTuner.blueGain
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            COLOR_TUNER_ENABLED -> {
                val isColorTuneEnabled = newValue as Boolean
                if (!isColorTuneEnabled) {
                    Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
                }
                colorTuner.isColorTuneEnabled = isColorTuneEnabled
            }
            COLOR_TUNER_RED_GAIN -> {
                colorTuner.redGain = newValue as Int
            }
            COLOR_TUNER_GREEN_GAIN -> {
                colorTuner.greenGain = newValue as Int
            }
            COLOR_TUNER_BLUE_GAIN -> {
                colorTuner.blueGain = newValue as Int
            }
        }
        return true
    }
}