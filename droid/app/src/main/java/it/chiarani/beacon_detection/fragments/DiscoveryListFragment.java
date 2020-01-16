package it.chiarani.beacon_detection.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.BeaconAdapter;
import it.chiarani.beacon_detection.adapters.BeaconDiscoveryAdapter;
import it.chiarani.beacon_detection.adapters.ItemClickListener;
import it.chiarani.beacon_detection.databinding.FragmentDiscoveryListBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.models.BeaconDevice;


public class DiscoveryListFragment extends BottomSheetDialogFragment implements ItemClickListener {

    FragmentDiscoveryListBinding binding;
    private List<BeaconDevice> beaconList = new ArrayList<>();
    BeaconDiscoveryAdapter adapterTags;
    private List<String> filterAddr = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public DiscoveryListFragment() {
        // Required empty public constructor
    }
    public static DiscoveryListFragment newInstance(String param1, String param2) {
        DiscoveryListFragment fragment = new DiscoveryListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discovery_list, container, false);
        View view = binding.getRoot();


        AppDatabase appDatabase = ((BeaconDetectionApp)getActivity().getApplication()).getRepository().getDatabase();

        appDatabase.beaconDeviceDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }
                    beaconList.clear();
                    beaconList.addAll(entities);
                    adapterTags.notifyDataSetChanged();
                }, throwable -> {
                    // Toast.makeText(this, getString(R.string.txtGenericError), Toast.LENGTH_LONG).show();
                });


        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(getActivity());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        binding.fragmentDiscoveryListRv.setLayoutManager(linearLayoutManagerTags);

        adapterTags = new BeaconDiscoveryAdapter(beaconList, this::onItemClick);
        binding.fragmentDiscoveryListRv.setAdapter(adapterTags);

        binding.fragmentDiscoveryBtnCollectData.setOnClickListener( v -> startCollectingFragment());

        return view;

    }

    private void startCollectingFragment() {
        DataCollectedFragment bottomSheetDialogFragment = new DataCollectedFragment(filterAddr);
        bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "bottom_nav_sheet_dialog_1");
        this.dismiss();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(int position) {
        if(filterAddr.contains(beaconList.get(position).getAddress())) {
            this.filterAddr.remove(beaconList.get(position).getAddress());
        } else {
            this.filterAddr.add(beaconList.get(position).getAddress());
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
