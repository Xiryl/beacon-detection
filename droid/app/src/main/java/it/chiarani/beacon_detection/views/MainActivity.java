package it.chiarani.beacon_detection.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.BeaconAdapter;
import it.chiarani.beacon_detection.adapters.FragmentCallback;
import it.chiarani.beacon_detection.controllers.FragmentCallbackType;
import it.chiarani.beacon_detection.controllers.Helpers;
import it.chiarani.beacon_detection.controllers.ScannerController;
import it.chiarani.beacon_detection.databinding.ActivityMainBinding;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.CustomCSVRowEntity;
import it.chiarani.beacon_detection.fragments.BottomNavigationDrawerFragment;
import it.chiarani.beacon_detection.fragments.DiscoveryListFragment;
import it.chiarani.beacon_detection.models.BeaconDevice;
import it.chiarani.beacon_detection.services.BeaconDataCollectorService;
import it.chiarani.beacon_detection.services.BeaconDiscoverService;

public class MainActivity extends AppCompatActivity implements FragmentCallback {

    private ActivityMainBinding binding;

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private List<BeaconDevice> beaconList = new ArrayList<>();
    private BeaconAdapter adapterTags;
    private AppDatabase appDatabase;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        super.onCreate(savedInstanceState);

        askForPermissions();

        this.setSupportActionBar(binding.bottomAppBar);
        setBottomAppBarHamburgerListener();

        appDatabase = ((BeaconDetectionApp)getApplication()).getRepository().getDatabase();

       /* Completable.fromAction(appDatabase.customCSVRowDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();
        Completable.fromAction(appDatabase.beaconDataDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();*/

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


        // Start data collection dialog
        binding.activityMainBtnCollectData.setOnClickListener(v ->  startCollectDialog() );
        binding.fab.setOnClickListener(v -> startCollectDialog() );

        // Stop service
        binding.activityMainBtnStopData.setOnClickListener(v -> {stopDataCollectionService();});

        // View live data
        binding.activityMainBtnViewRawData.setOnClickListener(v -> {showLiveData();});

        // Export data
        binding.activityMainBtnExportData.setOnClickListener( v-> {exportData();});

        // recyclerview
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(this);
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);
        binding.activityMainRvReadings.setLayoutManager(linearLayoutManagerTags);
        adapterTags = new BeaconAdapter(beaconList);
        binding.activityMainRvReadings.setAdapter(adapterTags);
    }

    @Override
    protected void onResume() {
        if(Helpers.isMyServiceRunning(BeaconDataCollectorService.class, this)) {
            hideCollectActions();
            showServiceActions();
        }
        else {
            hideServiceActions();
            showCollectActions();
        }
        super.onResume();
    }

    private void showLiveData () {
        Toast.makeText(this, "open livedata modal.", Toast.LENGTH_SHORT).show();
    }

    private void exportData () {

        if(Helpers.isMyServiceRunning(BeaconDataCollectorService.class, this)) {
            Toast.makeText(this, "Stop service before save the data collected!", Toast.LENGTH_LONG).show();
            return;
        }

        appDatabase.customCSVRowDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(1)
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }

                    String csvData = "";

                    for(CustomCSVRowEntity row : entities) {
                        csvData += row.getTimestamp() + row.getCsvRow() +"\n";
                    }

                    Helpers.writeToFile("data_collection_", csvData, this);
                    Toast.makeText(this, "saved", Toast.LENGTH_LONG).show();

                });


        /*mDisposable.add(appDatabase.customCSVRowDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }

                    String csvData = "";

                    for(CustomCSVRowEntity row : entities) {
                        csvData += row.getTimestamp() + row.getCsvRow() +"\n";
                    }

                    Helpers.writeToFile("data_collection_", csvData, this);
                    Toast.makeText(this, "saved", Toast.LENGTH_LONG).show();
                    mDisposable.dispose();
                }, throwable -> {
                     Toast.makeText(this, "Ooops, an error occurred :(", Toast.LENGTH_LONG).show();
                }));*/
    }

    private void stopDataCollectionService() {
        if(Helpers.isMyServiceRunning(BeaconDataCollectorService.class, this)) {
            Intent beaconDiscoveryService = new Intent(this, BeaconDataCollectorService.class);
            beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.STOP.toString());
            startService(beaconDiscoveryService);
        }
        else {
            Toast.makeText(this, "Already stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bottomappbar_menu_search: {
                if(Helpers.isMyServiceRunning(BeaconDiscoverService.class, this)) {
                    Toast.makeText(this, "Scan service already in execution", Toast.LENGTH_SHORT).show();
                }
                else {
                    startBeaconDiscoveryService();
                    mMenu.findItem(R.id.bottomappbar_menu_search).setEnabled(false);
                }

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

            alert.setPositiveButton("No", (dialog, whichButton) -> {
                DiscoveryListFragment bottomSheetDialogFragment = new DiscoveryListFragment(this::onFragmentCallback);
                bottomSheetDialogFragment.show(getSupportFragmentManager(), "bottom_nav_sheet_dialog");

            });

            alert.setNegativeButton("Yes", (dialog, whichButton) -> startBeaconDiscoveryService());

            alert.show();
        } else {
            if(Helpers.isMyServiceRunning(BeaconDiscoverService.class, this)) {
                Intent beaconDiscoveryService = new Intent(this, BeaconDiscoverService.class);
                beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.STOP.toString());
                startService(beaconDiscoveryService);
            }

            DiscoveryListFragment bottomSheetDialogFragment = new DiscoveryListFragment(this::onFragmentCallback);
            bottomSheetDialogFragment.show(getSupportFragmentManager(), "bottom_nav_sheet_dialog");
        }
    }

    private void startBeaconDiscoveryService() {
        binding.activityMainTxtScanTimer.setVisibility(View.VISIBLE);
        mMenu.findItem(R.id.bottomappbar_menu_search).setEnabled(false);
        binding.fab.setEnabled(false);

        new CountDownTimer(ScannerController.getScanTime(), 1000) {
            public void onTick(long millisUntilFinished) {
                binding.activityMainTxtScanTimer.setText("Scanning.. seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {

                binding.activityMainTxtScanTimer.setVisibility(View.INVISIBLE);
                showCollectActions();
                mMenu.findItem(R.id.bottomappbar_menu_search).setEnabled(true);
                binding.fab.setEnabled(true);
                Intent beaconDiscoveryService = new Intent(getApplicationContext(), BeaconDiscoverService.class);
                beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.STOP.toString());
                startService(beaconDiscoveryService);
                Toast.makeText(getApplicationContext(), "Beacon discovery service STOPPED.", Toast.LENGTH_SHORT).show();
            }
        }.start();

        Intent beaconDiscoveryService = new Intent(this, BeaconDiscoverService.class);
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

    @Override
    public void onFragmentCallback(int fragmentType, FragmentCallbackType messageType, List<String> MACFilterList) {
        if(fragmentType == 1) {
            switch (messageType) {
                case SERVICE_NOT_RUNNING: Toast.makeText(this, "Service no running", Toast.LENGTH_LONG).show();
                case SERVICE_ALREADY_RUNNING: Toast.makeText(this, "Service si running", Toast.LENGTH_LONG).show();
                case START_COLLECT_SERVICE: {
                    if(MACFilterList == null || MACFilterList.size() == 0) {
                        Toast.makeText(this, "Please choose 1 or n beacon(s) to start a scan", Toast.LENGTH_LONG).show();
                        return;
                    }
                    startDataCollectorService(new ArrayList<>(MACFilterList));
                }
            }
        }
    }

    private void showCollectActions() {
        binding.activityMainTxtNextOptions.setVisibility(View.VISIBLE);
        binding.activityMainBtnCollectData.setVisibility(View.VISIBLE);
    }

    private void hideCollectActions() {
        binding.activityMainTxtNextOptions.setVisibility(View.INVISIBLE);
        binding.activityMainBtnCollectData.setVisibility(View.INVISIBLE);
    }

    private void showServiceActions() {
        binding.activityMainTxtRunningData.setVisibility(View.VISIBLE);
        binding.activityMainBtnStopData.setVisibility(View.VISIBLE);
        binding.activityMainBtnViewRawData.setVisibility(View.VISIBLE);
        binding.activityMainBtnExportData.setVisibility(View.VISIBLE);
    }

    private void hideServiceActions() {
        binding.activityMainTxtRunningData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnStopData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnViewRawData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnExportData.setVisibility(View.INVISIBLE);
    }

    private void startDataCollectorService(ArrayList<String> filters) {
        if(Helpers.isMyServiceRunning(BeaconDataCollectorService.class, this)){
            Toast.makeText(this, "Scan service already running!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Db cleared before start", Toast.LENGTH_LONG).show();

        Completable.fromAction(appDatabase.customCSVRowDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();
        Completable.fromAction(appDatabase.beaconDataDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();

        hideCollectActions();
        showServiceActions();

        Intent beaconCollectorService = new Intent(this, BeaconDataCollectorService.class);
        beaconCollectorService.putStringArrayListExtra("AVAILABLEADRESSES", filters);
        beaconCollectorService.setAction(BeaconDiscoverService.ACTIONS.START.toString());
        startService(beaconCollectorService);

        Toast.makeText(this, "Service started.", Toast.LENGTH_LONG).show();

    }
}
