package com.example.davidka;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SwitchPreference appVol = findPreference("appVolume");
        final SeekBarPreference volSetting = findPreference("volumeSetting");
        volSetting.setEnabled(preferences.getBoolean("appVolume", false));
        appVol.setOnPreferenceChangeListener((preference, newValue) -> {
            volSetting.setEnabled((Boolean) newValue);
            preferences.edit().putBoolean("appVolume",(boolean) newValue)
                    .apply();
            return true;
        });
    }
}