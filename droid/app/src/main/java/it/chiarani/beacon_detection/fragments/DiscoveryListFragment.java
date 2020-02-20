package it.chiarani.beacon_detection.fragments;

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
import it.chiarani.beacon_detection.adapters.FragmentCallback;
import it.chiarani.beacon_detection.adapters.ItemClickListener;
import it.chiarani.beacon_detection.controllers.FragmentCallbackType;
import it.chiarani.beacon_detection.controllers.Helpers;
import it.chiarani.beacon_detection.databinding.FragmentDiscoveryListBinding;
import it.chiarani.beacon_detection.db.AppDatabase;


/**
 * Modal fort select wich beacons use in the data collection scan
 * View {@link DataCollectedFragment} for start the {@link BeaconDataCollectorService} service
 */
public class DiscoveryListFragment extends BottomSheetDialogFragment implements ItemClickListener {

    private FragmentDiscoveryListBinding binding;

    private List<String> filterAddr = new ArrayList<>(); // filter MAC address
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private FragmentCallback callback;

    public DiscoveryListFragment(FragmentCallback callback) {
        this.callback = callback;
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



        setRecyclerViewBinding();

        // onclick handler
        binding.fragmentDiscoveryBtnCollectData.setOnClickListener( v -> startCollectingFragment());
        return view;
    }

    private void setRecyclerViewBinding() {
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(getActivity());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        binding.fragmentDiscoveryListRv.setLayoutManager(linearLayoutManagerTags);

    }

    private void startCollectingFragment() {
        // set the duration of the scan

        // callback to activity
        callback.onFragmentCallback(1, FragmentCallbackType.START_COLLECT_SERVICE, filterAddr);
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

    }

}
