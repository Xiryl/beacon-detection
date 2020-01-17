package it.chiarani.beacon_detection.views;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.BeaconAdapter;
import it.chiarani.beacon_detection.adapters.BeaconDiscoveryAdapter;
import it.chiarani.beacon_detection.controllers.ScannerController;
import it.chiarani.beacon_detection.databinding.ActivityMainBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.BeaconDataEntity;
import it.chiarani.beacon_detection.db.entities.CustomCSVRowEntity;
import it.chiarani.beacon_detection.fragments.BottomNavigationDrawerFragment;
import it.chiarani.beacon_detection.fragments.DiscoveryListFragment;
import it.chiarani.beacon_detection.models.BeaconDevice;
import it.chiarani.beacon_detection.services.BeaconDiscoverService;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final String TAG = "main activity";
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private boolean disableButtonFlag = false;
    private List<BeaconDevice> beaconList = new ArrayList<>();
    BeaconAdapter adapterTags;
    Intent beaconDiscoveryService;
    AppDatabase appDatabase;
    Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        super.onCreate(savedInstanceState);

        askForPermissions();

        this.setSupportActionBar(binding.bottomAppBar);
        setBottomAppBarHamburgerListener();

      /*  mBeaconManager = BeaconManager.getInstanceForApplication(this);

        beaconRegion = new Region("beacon_region", null, null, null);

        // In this example, we will use Eddystone protocol, so we have to define it here
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        //mBeaconManager.setForegroundScanPeriod(100);
        //mBeaconManager.setForegroundBetweenScanPeriod(0);
        //Binds this activity to the BeaconService
        mBeaconManager.bind(this);
       */

        AppExecutors appExecutors = ((BeaconDetectionApp)getApplication()).getRepository().getAppExecutors();

        appDatabase = ((BeaconDetectionApp)getApplication()).getRepository().getDatabase();

       /* appDatabase.beaconDeviceDao().get().observe(this, data -> {
            int x = data.size();
        });

        mDisposable.add(appDatabase.beaconDeviceDao().get()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {

                    if(entities != null) {
                        Toast.makeText(this, entities.getAddress(), Toast.LENGTH_LONG).show();
                        return;
                    }

                }, throwable -> {
                    // Toast.makeText(this, getString(R.string.txtGenericError), Toast.LENGTH_LONG).show();
                }));*/

        mDisposable.add(appDatabase.beaconDeviceDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }
                    binding.activityMainTxtScan.setText("Found (" + entities.size() + ") Beacon(s)");
                    beaconList.clear();
                    beaconList.addAll(entities);
                    adapterTags.notifyDataSetChanged();

                }, throwable -> {
                    // Toast.makeText(this, getString(R.string.txtGenericError), Toast.LENGTH_LONG).show();
                }));


        binding.activityMainBtnCollectData.setOnClickListener(v -> {
            startCollectDialog();
        });

        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(this);
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        binding.activityMainRvReadings.setLayoutManager(linearLayoutManagerTags);

        binding.fab.setOnClickListener(v -> startCollectDialog() );

        adapterTags = new BeaconAdapter(beaconList);
        binding.activityMainRvReadings.setAdapter(adapterTags);

        binding.activityMainTxtData.setMovementMethod(new ScrollingMovementMethod());

      appDatabase.beaconDataDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }
                    for (BeaconDataEntity x : entities) {
                        binding.activityMainTxtData.append(x.getTimestamp()+ " || " +  x.getAddress() + " || " + x.getRssi() + "\n");
                    }
                }, throwable -> {
                    // Toast.makeText(this, getString(R.string.txtGenericError), Toast.LENGTH_LONG).show();
                });

          appDatabase.customCSVRowDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities == null) {
                        return;
                    }
                    for (CustomCSVRowEntity x : entities) {
                        binding.activityMainTxtData1.append(x.getTimestamp()+ " || " +  x.getCsvRow() + "\n");
                    }


                }, throwable -> {
                    // Toast.makeText(this, getString(R.string.txtGenericError), Toast.LENGTH_LONG).show();
                });

    }

    private void stopDiscoveryService() {
        Intent beaconService = new Intent(this, BeaconDiscoverService.class);
        beaconService.setAction(BeaconDiscoverService.ACTIONS.STOP.toString());
        startService(beaconService);
        stopService(beaconService);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottomappbar_menu_search: {
                startBeaconDiscoveryService();
                mMenu.findItem(R.id.bottomappbar_menu_search).setEnabled(false);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setBottomAppBarHamburgerListener() {
        binding.bottomAppBar.setNavigationOnClickListener(view -> {
            BottomNavigationDrawerFragment bottomSheetDialogFragment = new BottomNavigationDrawerFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), "bottom_nav_sheet_dialog");
        });
    }

    private void startCollectDialog() {
        if(this.beaconList.size() == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            alert.setMessage("No beacons has found. Would you like to make a scan before?");
            alert.setTitle("Before continue");

            alert.setView(edittext);

            alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    DiscoveryListFragment bottomSheetDialogFragment = new DiscoveryListFragment();
                    bottomSheetDialogFragment.show(getSupportFragmentManager(), "bottom_nav_sheet_dialog");

                }
            });

            alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    startBeaconDiscoveryService();
                }
            });

            alert.show();
        } else {
            Intent beaconService = new Intent(this, BeaconDiscoverService.class);
            beaconService.setAction(BeaconDiscoverService.ACTIONS.STOP.toString());
            startService(beaconService);

            DiscoveryListFragment bottomSheetDialogFragment = new DiscoveryListFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), "bottom_nav_sheet_dialog");
        }
    }

    private void startBeaconDiscoveryService() {
        binding.activityMainTxtScanTimer.setVisibility(View.VISIBLE);
        binding.activityMainBtnCollectData.setVisibility(View.INVISIBLE);
        binding.activityMainTxtNextOptions.setVisibility(View.INVISIBLE);
     //   binding.activityMainBtnStopSearch.setVisibility(View.VISIBLE);
        mMenu.findItem(R.id.bottomappbar_menu_search).setEnabled(false);
        binding.fab.setEnabled(false);
        new CountDownTimer(ScannerController.getScanTime(), 1000) {
            public void onTick(long millisUntilFinished) {
                binding.activityMainTxtScanTimer.setText("Scanning.. seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mMenu.findItem(R.id.bottomappbar_menu_search).setEnabled(true);
                binding.fab.setEnabled(true);
                binding.activityMainTxtScanTimer.setVisibility(View.INVISIBLE);
                binding.activityMainBtnCollectData.setVisibility(View.VISIBLE);
                binding.activityMainTxtNextOptions.setVisibility(View.VISIBLE);
             //   binding.activityMainBtnStopSearch.setVisibility(View.INVISIBLE);
                stopService(beaconDiscoveryService);
                Toast.makeText(getApplicationContext(), "Beacon discovery service STOPPED.", Toast.LENGTH_SHORT).show();
            }
        }.start();

        beaconDiscoveryService = new Intent(this, BeaconDiscoverService.class);
        beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.START.toString());
        startService(beaconDiscoveryService);
        Toast.makeText(getApplicationContext(), "Beacon discovery service STARTED.", Toast.LENGTH_SHORT).show();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottomappbar_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

   /*@Override
    public void onBeaconServiceConnect() {

        try {
            // Tells the BeaconService to start looking for beacons that match the passed Region object
            mBeaconManager.startRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // Specifies a class that should be called each time the BeaconService gets ranging data, once per second by default

        mBeaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.i(TAG, String.format("Found %s beacons in range", beacons.size()));

        if (beacons.size() > 0) {

            Beacon actualBeacon = beacons.iterator().next();
            boolean hasExtraParams = false;
            if(actualBeacon.getExtraDataFields().size() >= 1) {
                hasExtraParams = true;
            }

            String id1 = "null";
            try {
                id1 = actualBeacon.getId1().toString();
            }
            catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }

            String id2 = "null";
            try {
                id2 = actualBeacon.getId2().toString();
            }
            catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }

            String id3 = "null";
            try {
                id3 = actualBeacon.getId3().toString();
            }
            catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }

            if(beaconList.size() == 0) {
                // add beacon without tlm
                beaconList.add(new BeaconDevice(actualBeacon.getBluetoothAddress(), id1, id2, id3, actualBeacon.getRssi(), actualBeacon.getDistance()));
                adapterTags.notifyDataSetChanged();
                binding.activityMainTxtScan.setText("Found "+beaconList.size() + " beacons" );
            }

    /*        else if(beaconList.size() == 0 && hasExtraParams) {
                // add beacon with TLM
                long unsignedTemp = (actualBeacon.getExtraDataFields().get(2) >> 8);
                double temperature = unsignedTemp > 128 ? unsignedTemp - 256 : unsignedTemp + (actualBeacon.getExtraDataFields().get(2) & 0xff) / 256.0;

                beaconList.add(new BeaconDevice(actualBeacon.getBluetoothAddress(), id1, id2, id3, actualBeacon.getRssi(), actualBeacon.getExtraDataFields().get(0),
                        actualBeacon.getExtraDataFields().get(1), actualBeacon.getExtraDataFields().get(3), actualBeacon.getExtraDataFields().get(4)));
            }*/    /*
            else {

                for (BeaconDevice item : beaconList) {
                    if(!item.getAddress().equals(actualBeacon.getBluetoothAddress())) {
                            beaconList.add(new BeaconDevice(actualBeacon.getBluetoothAddress(), id1, id2, id3, actualBeacon.getRssi(), actualBeacon.getDistance()));
                        adapterTags.notifyDataSetChanged();
                        binding.activityMainTxtScan.setText("Found "+beaconList.size() + " beacons" );
                    }
                }
            }
            Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away. And RSSI:" + beacons.iterator().next().getRssi() + "---" + beacons.iterator().next().getBluetoothName());
        }
    }
*/
    private void askForPermissions() {
        List<String> permissionsToAsk = new ArrayList<>();
        int requestResult = 0;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            permissionsToAsk.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionsToAsk.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsToAsk.toArray(new String[permissionsToAsk.size()]), requestResult);
        }
    }

    @Override
    protected void onDestroy() {
        Completable.fromAction(appDatabase.beaconDeviceDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();

        Completable.fromAction(appDatabase.beaconDataDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();

        stopService(beaconDiscoveryService);
        super.onDestroy();
    }
}
