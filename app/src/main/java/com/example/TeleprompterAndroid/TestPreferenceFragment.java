package com.example.TeleprompterAndroid;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class TestPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.test_preference);

        /*findPreference("color2").setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(ColorPickerPreference.convertToARGB(Integer.parseInt(String.valueOf(newValue))));
            return true;
        });*/
//        ((ColorPickerPreference) findPreference("color2")).setAlphaSliderEnabled(true);
        findPreference("color3").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                System.out.println(newValue.toString());
                return false;
            }
        });
    }
}