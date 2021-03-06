package it.chiarani.beacon_detection.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.views.SettingsActivity;

/**
 * Application bottom menu fragment
 *
 * Extends {@link BottomSheetDialogFragment} for modal bottom-up effect
 */
public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    public BottomNavigationDrawerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        NavigationView nv = view.findViewById(R.id.navigation_view);
        nv.setNavigationItemSelectedListener( v-> {
            if (v.getItemId() == R.id.bottom_nav_drawer_menu_settings) {
                Intent intent = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                this.dismiss();
            }
            return true;
        });

        return  view;
    }
}
