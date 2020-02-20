package it.chiarani.beacon_detection.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import it.chiarani.beacon_detection.R;

/**
 * Settings fragment modal
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private EditTextPreference mDiscoveryFrequency;
    private EditTextPreference mDiscoveryTime;

    public SettingsFragment() {
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

        mDiscoveryTime = (EditTextPreference)getPreferenceManager().findPreference("pref_key_discovery_time");
        mDiscoveryTime.setOnPreferenceChangeListener(this::onPreferenceChange);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference.getKey().equals("pref_key_discovery_time")) {
            long x = Long.parseLong(newValue.toString());

            mDiscoveryTime.setSummary("Actual:" + x);
        } else if (preference.getKey().equals("pref_key_discovery_frequency")) {
            long x = Long.parseLong(newValue.toString());
            mDiscoveryFrequency.setSummary("Actual:" + x);
        }

        return false;
    }
}
