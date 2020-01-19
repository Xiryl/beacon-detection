package it.chiarani.beacon_detection.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.fragments.SettingsFragment;

/**
 * Wrapper for settings fragment
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings_fragment_fragment, new SettingsFragment())
                .commit();
    }
}
