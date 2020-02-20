package it.chiarani.beacon_detection.views;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.NordicDevicesAdapter;
import it.chiarani.beacon_detection.databinding.ActivityMainBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;
import it.chiarani.beacon_detection.fragments.BottomNavigationDrawerFragment;
import it.chiarani.beacon_detection.services.BaseTService;
import it.chiarani.beacon_detection.utils.PermissionsUtils;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class MainActivity extends AppCompatActivity implements ThingySdkManager.ServiceConnectionListener {

    private ActivityMainBinding binding;

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private AppDatabase appDatabase;
    private Menu mMenu;
    private AppExecutors mAppExecutors;
    private ThingySdkManager thingySdkManager;
    private NordicDevicesAdapter scannedDeviceAdapter;
    private List<NordicDeviceEntity> nordicScannedDevices = new ArrayList<>();
    private BaseThingyService.BaseThingyBinder mBinder;


    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        thingySdkManager.bindService(this, BaseTService.class);
        ThingyListenerHelper.registerThingyListener(getApplicationContext(), mThingyListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // check permissions
        PermissionsUtils.askForPermissions(this);

        // set bottombar
        this.setSupportActionBar(binding.bottomAppBar);
        setBottomAppBarHamburgerListener();

        // get Executors
        appDatabase = ((BeaconDetectionApp)getApplication()).getRepository().getDatabase();
        mAppExecutors = ((BeaconDetectionApp)getApplication()).getRepository().getAppExecutors();

        // Set recyclerview
        setDiscoveredDevicesRecyclerview();

        // get sdk
        thingySdkManager = ThingySdkManager.getInstance();


        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        BTScanStart();
    }

    private void BTScanStart() {
        if (mBluetoothAdapter == null) {
            System.out.println("Bluetooth NOT supported. Aborting.");
            return;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                System.out.println("Bluetooth is enabled...");

                // Starting the device discovery
                mBluetoothAdapter.startLeScan(mLeScanCallback);

            }
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            Log.d("TAG123123", "Rssi:"+ rssi + " Dev:"+ device.getAddress());
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        thingySdkManager.unbindService(this);
    }

    @Override
    protected void onDestroy() {
      /*  Completable.fromAction(appDatabase.beaconDeviceDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();
        Completable.fromAction(appDatabase.customCSVRowDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();
        Completable.fromAction(appDatabase.beaconDataDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();*/

        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottomappbar_menu_search: {
                startBLEScan();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottomappbar_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void setBottomAppBarHamburgerListener() {
        binding.bottomAppBar.setNavigationOnClickListener(view -> {
            BottomNavigationDrawerFragment bottomSheetDialogFragment = new BottomNavigationDrawerFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), "bottom_nav_sheet_dialog");
        });
    }

    private void startBLEScan() {
        // set scan
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(10)
                .setUseHardwareBatchingIfSupported(false)
                .build();

        // filter scan by uuid
        List<no.nordicsemi.android.support.v18.scanner.ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(ThingyUtils.THINGY_BASE_UUID)).build());

        // start scan, this will trigger the scanCallback
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(filters, scanSettings, scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);

            for(ScanResult result : results) {
                //thingySdkManager.connectToThingy(getApplicationContext(), result.getDevice(), BaseTService.class);
            }
        }
    };

    private void setDiscoveredDevicesRecyclerview() {
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(this);
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);
        binding.activityMainRvReadings.setLayoutManager(linearLayoutManagerTags);
        scannedDeviceAdapter = new NordicDevicesAdapter(nordicScannedDevices);
        binding.activityMainRvReadings.setAdapter(scannedDeviceAdapter);

        mDisposable.add(
                appDatabase.nordicDeviceDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }

                    nordicScannedDevices.clear();
                    nordicScannedDevices.addAll(entities);
                    scannedDeviceAdapter.notifyDataSetChanged();
                }, throwable -> Toast.makeText(this, "Error during scan UI", Toast.LENGTH_LONG).show())
        );
    }


    @Override
    public void onServiceConnected() {
        mBinder = thingySdkManager.getThingyBinder();
    }

    private ThingyListener mThingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            int x = 1;
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            //BLE STATE
            thingySdkManager.enableButtonStateNotification(device, true);

            //BATTERY STATE
            thingySdkManager.enableBatteryLevelNotifications(device, true);

            thingySdkManager.enableMotionNotifications(device, true);
        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {
            int x = 1;
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
            int x = 1;

            Intent intent = new Intent();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) 0);

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
            // Log.d("onGravityVectorChangedEvent", "onGravityVectorChangedEvent: " + bluetoothDevice.getName() + x + "..." +y + "..." + z);
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };


    private final BroadcastReceiver BroadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String mIntentAction = intent.getAction();
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(mIntentAction)) {
                int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                String mDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            }
        }
    };

}


