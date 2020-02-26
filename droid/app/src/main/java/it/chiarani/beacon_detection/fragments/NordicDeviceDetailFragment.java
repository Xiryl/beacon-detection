package it.chiarani.beacon_detection.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.FragmentCallback;
import it.chiarani.beacon_detection.adapters.ItemClickListener;
import it.chiarani.beacon_detection.adapters.ItemPropsClickListener;
import it.chiarani.beacon_detection.adapters.NordicDevicesPropsAdapter;
import it.chiarani.beacon_detection.controllers.FragmentCallbackType;
import it.chiarani.beacon_detection.databinding.FragmentDiscoveryListBinding;
import it.chiarani.beacon_detection.databinding.FragmentNordicDeviceDetailBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.models.NordicEvents;


public class NordicDeviceDetailFragment extends BottomSheetDialogFragment implements ItemPropsClickListener {

    private FragmentNordicDeviceDetailBinding binding;

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private AppDatabase appDatabase;
    private AppExecutors mAppExecutors;
    private List<String> filterAddr = new ArrayList<>(); // filter MAC address
    private List<NordicEvents> nordicEventsList = new ArrayList<>();
    private FragmentCallback callback;
    private int deviceNumber;
    private NordicDevicesPropsAdapter adapter;

    public NordicDeviceDetailFragment(FragmentCallback callback, int deviceNumber) {
        this.callback = callback;
        this.deviceNumber = deviceNumber;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        appDatabase = ((BeaconDetectionApp)this.getActivity().getApplicationContext()).getRepository().getDatabase();
        mAppExecutors = ((BeaconDetectionApp)this.getActivity().getApplicationContext()).getRepository().getAppExecutors();
        // Inflate the layout for this fragment with binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_nordic_device_detail, container, false);
        View view = binding.getRoot();

        setRecyclerViewBinding();

        // onclick handler
        return view;
    }

    private void setRecyclerViewBinding() {



        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(getActivity());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        adapter = new NordicDevicesPropsAdapter(nordicEventsList, this::onItemClick);
        binding.fragmentNordicDeviceListRv.setLayoutManager(linearLayoutManagerTags);
        binding.fragmentNordicDeviceListRv.setAdapter(adapter);

        mDisposable.add(
                appDatabase.nordicDeviceDao().getAsList()
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( entities -> {
                            if(entities != null) {
                                binding.fragmentNordicDeviceListTitle.setText(entities.get(deviceNumber).getAddress());
                                nordicEventsList.addAll(entities.get(deviceNumber).getNordicEvents());
                                adapter.notifyDataSetChanged();
                            }
                        }, throwable -> Toast.makeText(this.getActivity().getApplicationContext(), "Opps, something goes wrong :(", Toast.LENGTH_LONG).show())
        );

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
     * @param newEvent
     */
    @Override
    public void onItemClick(NordicEvents newEvent) {
        mDisposable.add(
                appDatabase.nordicDeviceDao().getAsList()
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( entities -> {
                            if(entities != null) {
                                List<NordicEvents> tmpEvents = entities.get(deviceNumber).getNordicEvents();
                                if(tmpEvents.contains(newEvent)) {
                                    tmpEvents.remove(newEvent);
                                } else {
                                    tmpEvents.add(newEvent);
                                }
                                entities.get(deviceNumber).setNordicEvents(tmpEvents);
                                nordicEventsList.clear();
                                nordicEventsList.addAll(tmpEvents);
                                adapter.notifyDataSetChanged();
                                mAppExecutors.diskIO().execute(() -> appDatabase.nordicDeviceDao().insert(entities.get(deviceNumber)));
                            }
                        }, throwable -> Toast.makeText(this.getActivity().getApplicationContext(), "Opps, something goes wrong :(" + throwable.getMessage(), Toast.LENGTH_LONG).show())
        );
    }

}
