package it.chiarani.beacon_detection.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
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
import it.chiarani.beacon_detection.adapters.BeaconDiscoveryAdapter;
import it.chiarani.beacon_detection.adapters.ItemClickListener;
import it.chiarani.beacon_detection.controllers.Helpers;
import it.chiarani.beacon_detection.controllers.ScannerController;
import it.chiarani.beacon_detection.databinding.FragmentDiscoveryListBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.models.BeaconDevice;
import it.chiarani.beacon_detection.services.BeaconDataCollectorService;


/**
 * Modal fort select wich beacons use in the data collection scan
 * View {@link DataCollectedFragment} for start the {@link BeaconDataCollectorService} service
 */
public class DiscoveryListFragment extends BottomSheetDialogFragment implements ItemClickListener {

    private FragmentDiscoveryListBinding binding;


    private BeaconDiscoveryAdapter adapterTags;
    private List<BeaconDevice> beaconList = new ArrayList<>();
    private List<String> filterAddr = new ArrayList<>(); // filter MAC address
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    public DiscoveryListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment with binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discovery_list, container, false);
        View view = binding.getRoot();

        if(Helpers.isMyServiceRunning(BeaconDataCollectorService.class, getActivity())) {

            // Launch DataCollectedFragment if service is already active for view real-time data
            DataCollectedFragment bottomSheetDialogFragment = new DataCollectedFragment(filterAddr);
            bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "bottom_nav_sheet_dialog_1");
            this.dismiss();
            return view;
        }

        AppDatabase appDatabase = ((BeaconDetectionApp)getActivity().getApplication()).getRepository().getDatabase();

        mDisposable.add(
            appDatabase.beaconDeviceDao().getAsList()
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

        // onclick handler
        binding.fragmentDiscoveryBtnCollectData.setOnClickListener( v -> startCollectingFragment());
        binding.fragmentDataCollectedEditextSessionDuration.setText(ScannerController.getCollectDataDuration()+"");
        return view;

    }

    private void setRecyclerViewBinding() {
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(getActivity());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        binding.fragmentDiscoveryListRv.setLayoutManager(linearLayoutManagerTags);

        adapterTags = new BeaconDiscoveryAdapter(beaconList, this::onItemClick);
        binding.fragmentDiscoveryListRv.setAdapter(adapterTags);
    }

    private void startCollectingFragment() {
        ScannerController.setCollectDataDuration(Long.parseLong(binding.fragmentDataCollectedEditextSessionDuration.getText().toString()));
        DataCollectedFragment bottomSheetDialogFragment = new DataCollectedFragment(filterAddr);
        bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "bottom_nav_sheet_dialog_1");
        this.dismiss();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mDisposable.dispose(); // prevent memory leak
    }

    /**
     * For filter the beacons MAC adresses
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        if(filterAddr.contains(beaconList.get(position).getAddress())) {
            this.filterAddr.remove(beaconList.get(position).getAddress());
        } else {
            this.filterAddr.add(beaconList.get(position).getAddress());
        }
    }

}
