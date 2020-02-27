package it.chiarani.beacon_detection.views;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.ConnectNordicAdapter;
import it.chiarani.beacon_detection.adapters.ItemClickListener;
import it.chiarani.beacon_detection.adapters.NordicDevicesAdapter;
import it.chiarani.beacon_detection.databinding.ActivityMainBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;
import it.chiarani.beacon_detection.fragments.BottomNavigationDrawerFragment;
import it.chiarani.beacon_detection.fragments.ConnectNordicFragment;
import it.chiarani.beacon_detection.fragments.NordicDeviceDetailFragment;
import it.chiarani.beacon_detection.models.NordicDevice;
import it.chiarani.beacon_detection.models.NordicEvents;
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

public class MainActivity extends AppCompatActivity implements ThingySdkManager.ServiceConnectionListener, ItemClickListener {

    private ActivityMainBinding binding;

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private AppDatabase appDatabase;
    private Menu mMenu;
    private AppExecutors mAppExecutors;
    private ThingySdkManager thingySdkManager;
    private NordicDevicesAdapter scannedDeviceAdapter;
    private HashMap<String, BluetoothDevice> scanResultList = new HashMap<>();
    private List<NordicDeviceEntity> nordicDeviceEntityList = new ArrayList<>();
    private List<BluetoothDevice> connDevices = new ArrayList<>();
    private BaseThingyService.BaseThingyBinder mBinder;
    BluetoothGatt gatt;

    @Override
    protected void onStart() {
        super.onStart();
        thingySdkManager.bindService(this, BaseTService.class);
        // ThingyListenerHelper.registerThingyListener(getApplicationContext(), mThingyListener);
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


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDatabase();
            }
        });


        mDisposable.add(
            appDatabase.nordicDeviceDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null) {

                        int conn = 0;
                        for(NordicDeviceEntity en : entities) {
                            if(en.isConnected()) conn++;
                        }

                        nordicDeviceEntityList.clear();
                        nordicDeviceEntityList.addAll(entities);
                        scannedDeviceAdapter.notifyDataSetChanged();
                        binding.activityMainTxtScan.setText(String.format("%s LE devices from db, %s conn", nordicDeviceEntityList.size(), conn));

                    }
                }, throwable -> Toast.makeText(this, "Opps, something goes wrong :(", Toast.LENGTH_LONG).show())
        );

        binding.activityMainBtnConnectDevice.setOnClickListener(v -> connectNordic());
        binding.activityMainBtnCollectData.setOnClickListener( v -> startDataCollection());

    }

    void connectNordic() {
        ConnectNordicFragment bottomSheetDialogFragment = new ConnectNordicFragment(scanResultList);
        bottomSheetDialogFragment.show(getSupportFragmentManager(), "conn_nordic_fragment");
    }

    void viewDatabase() {
        mDisposable.add(
            appDatabase.nordicDeviceDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    int x = 1;
                }, throwable -> Toast.makeText(this, "Error during scan UI", Toast.LENGTH_LONG).show())
        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        thingySdkManager.disconnectFromAllThingies();
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
        Completable.fromAction(appDatabase.nordicDeviceDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();
        thingySdkManager.disconnectFromAllThingies();
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


        if(nordicDeviceEntityList.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This will override saved devices")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            bleScan();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Toast.makeText(MainActivity.this, "cancelled.", Toast.LENGTH_SHORT).show();
                        }
                    });
            // Create the AlertDialog object and return it
            builder.create().show();
        } else {
            bleScan();
        }
    }

    private void bleScan() {
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
        try {
            scanner.startScan(filters, scanSettings, scanCallback);
        }
        catch (Exception ex) {
            Toast.makeText(this, "Oops. Qualcosa è andato storto. Il bluetooth è acceso?", Toast.LENGTH_LONG).show();
            return;
        }



        new CountDownTimer(10000, 1000) {

            int counter = 10000;
            public void onTick(long millisUntilFinished) {
                binding.activityMainTxtScan.setText(String.format("Found %s LE devices", scanResultList.size()));
                binding.activityMainTxtScanTimer.setVisibility(View.VISIBLE);
                binding.activityMainTxtScanTimer.setText(String.format("Remaining %s seconds.", counter / 1000));
                counter -= 1000;
            }

            public void onFinish() {
                binding.activityMainTxtScanTimer.setVisibility(View.INVISIBLE);
                scanner.stopScan(scanCallback);
                Toast.makeText(MainActivity.this, "LE scanner end.", Toast.LENGTH_SHORT).show();
            }
        }.start();

        Toast.makeText(MainActivity.this, "LE scanner start.", Toast.LENGTH_SHORT).show();
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results) {

                if(!scanResultList.containsKey(result.getDevice().getAddress())){
                    NordicDeviceEntity en = new NordicDeviceEntity(result.getDevice().getAddress(), result.getRssi(), result.getDevice().getName());
                    List<NordicEvents> events = new ArrayList<>();
                    events.add(NordicEvents.buttonStateChanged);
                    events.add(NordicEvents.batteryLevelChanged);
                    events.add(NordicEvents.accelerometerValueChanged);
                    en.setNordicEvents(events);
                    scanResultList.put(result.getDevice().getAddress(), result.getDevice());
                    mAppExecutors.diskIO().execute(() -> appDatabase.nordicDeviceDao().insert(en));
                }
            }
        }
    };

    private void setDiscoveredDevicesRecyclerview() {
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(this);
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);
        binding.activityMainRvReadings.setLayoutManager(linearLayoutManagerTags);
        scannedDeviceAdapter = new NordicDevicesAdapter(nordicDeviceEntityList, this::onItemClick);
        binding.activityMainRvReadings.setAdapter(scannedDeviceAdapter);
    }

    private void startDataCollection() {

        for (BluetoothDevice dev : thingySdkManager.getConnectedDevices()){

            ThingyListenerHelper.registerThingyListener(getApplicationContext(), mThingyListener, dev);
            //events
            thingySdkManager.enableMotionNotifications(dev, true);
            thingySdkManager.enableAirQualityNotifications(dev, true);
            thingySdkManager.enableBatteryLevelNotifications(dev, true);
            thingySdkManager.enableButtonStateNotification(dev, true);
            thingySdkManager.enableColorNotifications(dev, true);
            // thingySdkManager.enableEnvironmentNotifications(dev, true); --> CREA DISCONNESSIONE
            thingySdkManager.enableEulerNotifications(dev, true);
            thingySdkManager.enableGravityVectorNotifications(dev, true);
            thingySdkManager.enableHeadingNotifications(dev, true);
            // thingySdkManager.enableHumidityNotifications(dev, true);//  --> CREA DISCONNESSIONE
            thingySdkManager.enableOrientationNotifications(dev, true);
            thingySdkManager.enablePedometerNotifications(dev, true);
            thingySdkManager.enablePressureNotifications(dev, true);
            thingySdkManager.enableQuaternionNotifications(dev, true);
            thingySdkManager.enableRawDataNotifications(dev, true);
            thingySdkManager.enableRotationMatrixNotifications(dev, true);
            thingySdkManager.enableSoundNotifications(dev, true);
            thingySdkManager.enableSpeakerStatusNotifications(dev, true);
            thingySdkManager.enableTapNotifications(dev, true);
            thingySdkManager.enableThingyMicrophone(dev, true);
            thingySdkManager.enableUiNotifications(dev, true);

           /* thingySdkManager.enableMotionNotifications(dev, true);
            thingySdkManager.enableAirQualityNotifications(dev, true);
            thingySdkManager.enableBatteryLevelNotifications(dev, true);
            thingySdkManager.enableButtonStateNotification(dev, true);
            thingySdkManager.enableColorNotifications(dev, true);
            thingySdkManager.enableEnvironmentNotifications(dev, true);
            thingySdkManager.enableEulerNotifications(dev, true);
            thingySdkManager.enableGravityVectorNotifications(dev, true);
            thingySdkManager.enableHeadingNotifications(dev, true);
            thingySdkManager.enableHumidityNotifications(dev, true);
            thingySdkManager.enableOrientationNotifications(dev, true);
            thingySdkManager.enablePedometerNotifications(dev, true);
            thingySdkManager.enablePressureNotifications(dev, true);
            thingySdkManager.enableQuaternionNotifications(dev, true);
            thingySdkManager.enableRawDataNotifications(dev, true);
            thingySdkManager.enableRotationMatrixNotifications(dev, true);
            thingySdkManager.enableSoundNotifications(dev, true);
            thingySdkManager.enableSpeakerStatusNotifications(dev, true);
            thingySdkManager.enableTapNotifications(dev, true);
            thingySdkManager.enableThingyMicrophone(dev, true);
            thingySdkManager.enableUiNotifications(dev, true);*/

            // gatt = dev.connectGatt(getApplicationContext(), true, gattCallback);

           /* //BATTERY STATE
            thingySdkManager.enableBatteryLevelNotifications(dev, true);

            thingySdkManager.enableMotionNotifications(dev, true);

            thingySdkManager.setMotionProcessingFrequency(dev, ThingyUtils.MPU_FREQ_MAX_INTERVAL);

            gatt = dev.connectGatt(getApplicationContext(), true, gattCallback);*/
        }

        /*
        for(BluetoothDevice dev : scanResultList.values()) {
            //BLE STATE
            thingySdkManager.enableButtonStateNotification(dev, true);


            //BATTERY STATE
            thingySdkManager.enableBatteryLevelNotifications(dev, true);

            thingySdkManager.enableMotionNotifications(dev, true);

            thingySdkManager.setMotionProcessingFrequency(dev, ThingyUtils.MPU_FREQ_MAX_INTERVAL);

            gatt = dev.connectGatt(getApplicationContext(), true, gattCallback);
        }*/

    }

    @Override
    public void onServiceConnected() {
        mBinder = (BaseTService.ThingyBinder) thingySdkManager.getThingyBinder();
    }

    private ThingyListener mThingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {
            Log.d("Callback", "1 ok");
        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
            Log.d("Callback", "2 ok");
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {
            Log.d("Callback", "3 ok");
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {
            Log.d("Callback", "4 ok");
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {
            Log.d("Callback", "5 ok");
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {
            Log.d("Callback", "6 ok");
        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {
            Log.d("Callback", "7 ok");
            //gatt.readRemoteRssi();
        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {
            Log.d("Callback", "8 ok");
        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {
            Log.d("Callback", "9 ok");
        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {
            Log.d("Callback", "10 ok");
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {
            Log.d("Callback", "11 ok");
        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            Log.d("Callback", "12 ok");
           //  gatt.readRemoteRssi();
        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            Log.d("Callback", "13 ok");
        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            Log.d("Callback", "14 ok");
        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {
            Log.d("Callback", "15 ok");
        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {
            Log.d("Callback", "16 ok");
        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {
            Log.d("Callback", "17 ok");
        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            Log.d("Callback", "18 ok");
            // Log.d("onGravityVectorChangedEvent", "onGravityVectorChangedEvent: " + bluetoothDevice.getName() + x + "..." +y + "..." + z);
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {
            Log.d("Callback", "19 ok");
        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {
            Log.d("Callback", "20 ok");
        }
    };

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d("gattCallback", "Rssi:" + rssi);
           /* NordicDeviceEntity entity = new NordicDeviceEntity(gatt.getDevice().getAddress(), rssi, "Nordic:52");
            mAppExecutors.diskIO().execute(() -> appDatabase.nordicDeviceDao().insert(entity));
            nordicDeviceEntityList.add(entity);*/
        }
    };

    @Override
    public void onItemClick(int position) {
        NordicDeviceDetailFragment fragment = new NordicDeviceDetailFragment(null, position);
        fragment.show(getSupportFragmentManager(), "bottom_nav_sheet_dialog");
    }

}


