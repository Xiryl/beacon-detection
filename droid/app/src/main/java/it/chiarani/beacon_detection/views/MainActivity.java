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
import it.chiarani.beacon_detection.fragments.DataCollectedFragment;
import it.chiarani.beacon_detection.fragments.DiscoveryListFragment;
import it.chiarani.beacon_detection.models.BeaconDevice;
import it.chiarani.beacon_detection.services.BeaconDataCollectorService;
import it.chiarani.beacon_detection.services.BeaconDiscoverService;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String TAG = "main activity";
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private List<BeaconDevice> beaconList = new ArrayList<>();
    private BeaconAdapter adapterTags;
    private Intent beaconDiscoveryService;
    private AppDatabase appDatabase;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        super.onCreate(savedInstanceState);

        askForPermissions();

        this.setSupportActionBar(binding.bottomAppBar);
        setBottomAppBarHamburgerListener();


        AppExecutors appExecutors = ((BeaconDetectionApp)getApplication()).getRepository().getAppExecutors();

        appDatabase = ((BeaconDetectionApp)getApplication()).getRepository().getDatabase();


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

        mDisposable.add(appDatabase.customCSVRowDao().getAsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( entities -> {
                    if(entities != null && entities.size() == 0) {
                        return;
                    }
                    int x = 1;

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
        binding.activityMainBtnStopData.setOnClickListener(v -> {stopDataCollectionService();});

        adapterTags = new BeaconAdapter(beaconList);
        binding.activityMainRvReadings.setAdapter(adapterTags);
        binding.activityMainBtnViewRawData.setOnClickListener(v -> {showLiveData();});
    }

    private void showLiveData  () {
        DataCollectedFragment bottomSheetDialogFragment = new DataCollectedFragment(new ArrayList<String>());
        bottomSheetDialogFragment.show(this.getSupportFragmentManager(), "bottom_nav_sheet_dialog_1");
    }

    private void stopDataCollectionService() {
        beaconDiscoveryService = new Intent(this, BeaconDataCollectorService.class);
        beaconDiscoveryService.setAction(BeaconDiscoverService.ACTIONS.STOP.toString());
        this.stopService(beaconDiscoveryService);

        Toast.makeText(this, "Data collection service TERMINATED.", Toast.LENGTH_SHORT).show();

        binding.activityMainTxtRunningData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnStopData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnViewRawData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnCollectData.setVisibility(View.VISIBLE);

        binding.activityMainBtnExportData.setVisibility(View.VISIBLE);
        binding.activityMainTxtNextOptions.setVisibility(View.VISIBLE);

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

            binding.activityMainTxtRunningData.setVisibility(View.VISIBLE);
            binding.activityMainBtnStopData.setVisibility(View.VISIBLE);
            binding.activityMainBtnViewRawData.setVisibility(View.VISIBLE);
            binding.activityMainBtnCollectData.setVisibility(View.INVISIBLE);
            binding.activityMainBtnExportData.setVisibility(View.INVISIBLE);
            binding.activityMainTxtNextOptions.setVisibility(View.INVISIBLE);
        }
    }

    private void startBeaconDiscoveryService() {
        binding.activityMainTxtScanTimer.setVisibility(View.VISIBLE);
        binding.activityMainBtnCollectData.setVisibility(View.INVISIBLE);

        binding.activityMainBtnExportData.setVisibility(View.INVISIBLE);
        binding.activityMainTxtNextOptions.setVisibility(View.INVISIBLE);
        binding.activityMainTxtRunningData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnStopData.setVisibility(View.INVISIBLE);
        binding.activityMainBtnViewRawData.setVisibility(View.INVISIBLE);
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

                binding.activityMainBtnExportData.setVisibility(View.VISIBLE);
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
     /*   Completable.fromAction(appDatabase.beaconDeviceDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();

        Completable.fromAction(appDatabase.beaconDataDao()::clear)
                .subscribeOn(Schedulers.io())
                .subscribe();*/

        stopService(beaconDiscoveryService);

        super.onDestroy();
    }
}
