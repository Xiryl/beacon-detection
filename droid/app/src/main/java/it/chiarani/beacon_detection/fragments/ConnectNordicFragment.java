package it.chiarani.beacon_detection.fragments;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.ScanResult;
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
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.ConnectNordicAdapter;
import it.chiarani.beacon_detection.adapters.FragmentCallback;
import it.chiarani.beacon_detection.adapters.ItemClickListener;
import it.chiarani.beacon_detection.adapters.ItemPropsClickListener;
import it.chiarani.beacon_detection.adapters.NordicDevicesPropsAdapter;
import it.chiarani.beacon_detection.controllers.FragmentCallbackType;
import it.chiarani.beacon_detection.databinding.FragmentConnectNordicBinding;
import it.chiarani.beacon_detection.databinding.FragmentNordicDeviceDetailBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;
import it.chiarani.beacon_detection.models.NordicEvents;
import it.chiarani.beacon_detection.services.BaseTService;
import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;


public class ConnectNordicFragment extends BottomSheetDialogFragment implements ItemClickListener, ThingySdkManager.ServiceConnectionListener {

    private FragmentConnectNordicBinding binding;

    private List<NordicDeviceEntity> nordicDeviceEntityList = new ArrayList<>();
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private HashMap<String, BluetoothDevice> scanResultList = new HashMap<>();
    private AppDatabase appDatabase;
    private AppExecutors mAppExecutors;
    private ConnectNordicAdapter adapter;
    private ThingySdkManager thingySdkManager;
    private BaseThingyService.BaseThingyBinder mBinder;

    public ConnectNordicFragment(HashMap<String, BluetoothDevice> scanResultList) {
        this.scanResultList = scanResultList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        appDatabase = ((BeaconDetectionApp) this.getActivity().getApplicationContext()).getRepository().getDatabase();
        mAppExecutors = ((BeaconDetectionApp) this.getActivity().getApplicationContext()).getRepository().getAppExecutors();
        // Inflate the layout for this fragment with binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect_nordic, container, false);
        View view = binding.getRoot();


        thingySdkManager = ThingySdkManager.getInstance();

        ThingyListenerHelper.registerThingyListener(getContext(), mThingyListener);

        setRecyclerViewBinding();

        // onclick handler
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setRecyclerViewBinding() {


        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(getActivity());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        adapter = new ConnectNordicAdapter(nordicDeviceEntityList, this::onItemClick);
        binding.fragmentConnectNordicListRv.setLayoutManager(linearLayoutManagerTags);
        binding.fragmentConnectNordicListRv.setAdapter(adapter);

        mDisposable.add(
                appDatabase.nordicDeviceDao().getAsList()
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(entities -> {
                            if (entities != null) {
                                nordicDeviceEntityList.addAll(entities);
                                adapter.notifyDataSetChanged();
                            }
                        }, throwable -> Toast.makeText(this.getActivity().getApplicationContext(), "Opps, something goes wrong :(", Toast.LENGTH_LONG).show())
        );
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mDisposable.dispose(); // prevent memory leak
    }

    /**
     * For filter the beacons MAC adresses
     *
     * @param pos
     */
    @Override
    public void onItemClick(int pos) {
        if(nordicDeviceEntityList.get(pos).isConnected()){
            if(scanResultList.containsKey(nordicDeviceEntityList.get(pos).getAddress())) {
                BluetoothDevice dev = scanResultList.get(nordicDeviceEntityList.get(pos).getAddress());
                thingySdkManager.disconnectFromThingy(dev);
            }
        } else {
            if(scanResultList.containsKey(nordicDeviceEntityList.get(pos).getAddress())) {
                BluetoothDevice dev = scanResultList.get(nordicDeviceEntityList.get(pos).getAddress());
                thingySdkManager.connectToThingy(getContext(), dev, BaseTService.class);
            }
        }


    }

    @Override
    public void onServiceConnected() {
        mBinder = thingySdkManager.getThingyBinder();
    }

    private ThingyListener mThingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            mDisposable.add(
                appDatabase.nordicDeviceDao().getAsList()
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(entities -> {
                            if (entities != null) {
                                for(NordicDeviceEntity en : entities) {
                                    if(en.getAddress().equals(device.getAddress())) {
                                        en.setConnected(true);
                                        nordicDeviceEntityList.clear();
                                        nordicDeviceEntityList.addAll(entities);
                                        adapter.notifyDataSetChanged();
                                        mAppExecutors.diskIO().execute(() -> appDatabase.nordicDeviceDao().insert(en));
                                    }
                                }
                            }
                        }, throwable -> Toast.makeText(getActivity().getApplicationContext(), "Opps, something goes wrong :(", Toast.LENGTH_LONG).show())
            );
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            mDisposable.add(
                appDatabase.nordicDeviceDao().getAsList()
                    .take(1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(entities -> {
                        if (entities != null) {
                            for(NordicDeviceEntity en : entities) {
                                if(en.getAddress().equals(device.getAddress())) {
                                    en.setConnected(false);
                                    nordicDeviceEntityList.clear();
                                    nordicDeviceEntityList.addAll(entities);
                                    adapter.notifyDataSetChanged();
                                    mAppExecutors.diskIO().execute(() -> appDatabase.nordicDeviceDao().insert(en));
                                }
                            }
                        }
                    }, throwable -> Toast.makeText(getActivity().getApplicationContext(), "Opps, something goes wrong :(", Toast.LENGTH_LONG).show())
            );
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {

        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }

    };
}
