package it.chiarani.beacon_detection.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.BeaconDataAdapter;
import it.chiarani.beacon_detection.controllers.Helpers;
import it.chiarani.beacon_detection.databinding.FragmentDataCollectedBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.models.BeaconData;
import it.chiarani.beacon_detection.services.BeaconDataCollectorService;
import it.chiarani.beacon_detection.services.BeaconDiscoverService;

/**
 * Modal for view real-time data collected
 */
public class DataCollectedFragment extends BottomSheetDialogFragment {

    private FragmentDataCollectedBinding binding;

    private BeaconDataAdapter adapterTags;
    private List<BeaconData> beaconList = new ArrayList<>();
    private ArrayList<String> filterAddr = new ArrayList<>(); // MAC address filters
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public DataCollectedFragment(List<String> filterAddr) {
        this.filterAddr.addAll(filterAddr);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment with binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_data_collected, container, false);
        View view = binding.getRoot();

        if (Helpers.isMyServiceRunning(BeaconDataCollectorService.class, getActivity())) {
            // service already running
            Toast.makeText(getActivity().getApplicationContext(), "Data collection service already in execution.", Toast.LENGTH_SHORT).show();
        } else {
            startBeaconDataCollectorService();
        }

        AppDatabase appDatabase = ((BeaconDetectionApp)getActivity().getApplication()).getRepository().getDatabase();

        // Get realtime collected data
        mDisposable.add(
            appDatabase.beaconDataDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities == null && entities.size() == 0) {
                        return;
                    }
                    beaconList.clear();
                    beaconList.addAll(entities);
                    adapterTags.notifyDataSetChanged();
                }, throwable -> {
                   // err
                })
        );

        setRecyclerViewBinding();


        // set modal title
        binding.fragmentDataCollectedTitle.setText(String.format("Real time data from %s device(s).", this.filterAddr.size()));

        return view;

    }

    private void setRecyclerViewBinding() {
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(getActivity());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        binding.fragmentDataCollectedRv.addItemDecoration(new DividerItemDecoration(binding.fragmentDataCollectedRv.getContext(), DividerItemDecoration.VERTICAL));
        binding.fragmentDataCollectedRv.setLayoutManager(linearLayoutManagerTags);

        adapterTags = new BeaconDataAdapter(beaconList);
        binding.fragmentDataCollectedRv.setAdapter(adapterTags);
    }

    private void startBeaconDataCollectorService () {
        Intent beaconDiscoveryService = new Intent(getActivity(), BeaconDataCollectorService.class);
        beaconDiscoveryService.putStringArrayListExtra("AVAILABLEADRESSES", this.filterAddr);
        beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.START.toString());
        getActivity().startService(beaconDiscoveryService);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mDisposable.dispose(); // dispose the observe, memory leak otherwise!!
        // Toast.makeText(getActivity().getApplicationContext(), "Data collection service will continue in background.", Toast.LENGTH_SHORT).show();
    }

}
