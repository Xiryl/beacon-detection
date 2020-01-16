package it.chiarani.beacon_detection.fragments;

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

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.BeaconAdapter;
import it.chiarani.beacon_detection.adapters.BeaconDataAdapter;
import it.chiarani.beacon_detection.adapters.BeaconDiscoveryAdapter;
import it.chiarani.beacon_detection.databinding.FragmentDataCollectedBinding;
import it.chiarani.beacon_detection.databinding.FragmentDiscoveryListBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.models.BeaconData;
import it.chiarani.beacon_detection.models.BeaconDevice;
import it.chiarani.beacon_detection.services.BeaconDataCollectorService;
import it.chiarani.beacon_detection.services.BeaconDiscoverService;


public class DataCollectedFragment extends BottomSheetDialogFragment {

    FragmentDataCollectedBinding binding;
    private List<BeaconData> beaconList = new ArrayList<>();
    BeaconDataAdapter adapterTags;
    private ArrayList<String> filterAddr = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

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

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_data_collected, container, false);
        View view = binding.getRoot();

        Intent beaconDiscoveryService = new Intent(getActivity(), BeaconDataCollectorService.class);
        beaconDiscoveryService.putStringArrayListExtra("AVAILABLEADRESSES", this.filterAddr);
        beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.START.toString());
        getActivity().startService(beaconDiscoveryService);
        Toast.makeText(getActivity().getApplicationContext(), "Data collection service STARTED.", Toast.LENGTH_SHORT).show();


        AppDatabase appDatabase = ((BeaconDetectionApp)getActivity().getApplication()).getRepository().getDatabase();

        appDatabase.beaconDataDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }
                    beaconList.clear();
                    beaconList.addAll(entities);
                    Collections.reverse(beaconList);
                    adapterTags.notifyDataSetChanged();
                }, throwable -> {
                    // Toast.makeText(this, getString(R.string.txtGenericError), Toast.LENGTH_LONG).show();
                });


        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(getActivity());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        binding.fragmentDataCollectedRv.addItemDecoration(new DividerItemDecoration(binding.fragmentDataCollectedRv.getContext(), DividerItemDecoration.VERTICAL));
        binding.fragmentDataCollectedRv.setLayoutManager(linearLayoutManagerTags);

        adapterTags = new BeaconDataAdapter(beaconList);
        binding.fragmentDataCollectedRv.setAdapter(adapterTags);
        binding.fragmentDataCollectedTitle.setText("Real time data from "+this.filterAddr.size()+" device(s).");
        return view;

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Toast.makeText(getActivity().getApplicationContext(), "Data collection service STOPPED.", Toast.LENGTH_SHORT).show();
        Intent beaconDiscoveryService = new Intent(getActivity(), BeaconDataCollectorService.class);
        beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.STOP.toString());
        getActivity().stopService(beaconDiscoveryService);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
