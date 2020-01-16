package it.chiarani.beacon_detection.fragments;

import android.os.Bundle;
import android.preference.Preference;

import androidx.preference.PreferenceFragmentCompat;

import it.chiarani.beacon_detection.R;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {


    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

    }

}
