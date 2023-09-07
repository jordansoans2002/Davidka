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
        final SwitchPreference showText = findPreference("showText");
        final SwitchPreference scrollable = findPreference("scrollable");
        volSetting.setEnabled(preferences.getBoolean("appVolume", false));
        appVol.setOnPreferenceChangeListener((preference, newValue) -> {
            volSetting.setEnabled((Boolean) newValue);
            preferences.edit().putBoolean("appVolume",(boolean) newValue)
                    .apply();
            return true;
        });
        if(showText.isChecked()) {
            scrollable.setChecked(true);
            scrollable.setEnabled(false);
            preferences.edit().putBoolean("scrollable",true)
                    .apply();
        }
        showText.setOnPreferenceChangeListener((preference, newValue) -> {
            if((Boolean) newValue)
                scrollable.setChecked(true);
            scrollable.setEnabled(!(Boolean)newValue);
            preferences.edit().putBoolean("scrollable",((boolean) newValue))
                    .apply();
            return true;
        });

    }
}