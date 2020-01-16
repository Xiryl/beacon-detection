package it.chiarani.beacon_detection.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Map;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.controllers.ScannerController;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {


    private EditTextPreference mDiscoveryFrequency;
    private EditTextPreference mDiscoveryTime;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        mDiscoveryFrequency = (EditTextPreference)getPreferenceManager().findPreference("pref_key_discovery_frequency");
        mDiscoveryFrequency.setOnPreferenceChangeListener(this::onPreferenceChange);
       //  mDiscoveryFrequency.setSummary("Actual:" + ScannerController.getScanFrequencyPeriod());

        mDiscoveryTime = (EditTextPreference)getPreferenceManager().findPreference("pref_key_discovery_time");
        mDiscoveryTime.setOnPreferenceChangeListener(this::onPreferenceChange);
      //   mDiscoveryTime.setSummary("Actual:" + ScannerController.getScanTime());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference.getKey().equals("pref_key_discovery_time")) {
            long x = Long.parseLong(newValue.toString());
            ScannerController.setScanTime(x);
            mDiscoveryTime.setSummary("Actual:" + x);
        } else if (preference.getKey().equals("pref_key_discovery_frequency")) {
            long x = Long.parseLong(newValue.toString());
            ScannerController.setScanFrequencyPeriod(x);
            mDiscoveryFrequency.setSummary("Actual:" + x);
        }

        return false;
    }
}
