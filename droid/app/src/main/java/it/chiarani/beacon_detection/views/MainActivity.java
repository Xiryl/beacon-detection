package it.chiarani.beacon_detection.views;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.adapters.BeaconAdapter;
import it.chiarani.beacon_detection.databinding.ActivityMainBinding;
import it.chiarani.beacon_detection.models.BeaconItem;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final String TAG = "main activity";

    private BeaconManager mBeaconManager;
    private Region beaconRegion;
    private List<BeaconItem> beaconList = new ArrayList<>();
    BeaconAdapter adapterTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        super.onCreate(savedInstanceState);

        askForPermissions();

        mBeaconManager = BeaconManager.getInstanceForApplication(this);

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

        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(this);
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);

        binding.activityMainRvReadings.setLayoutManager(linearLayoutManagerTags);

        adapterTags = new BeaconAdapter(beaconList);
        binding.activityMainRvReadings.setAdapter(adapterTags);
    }

    @Override
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
                beaconList.add(new BeaconItem(actualBeacon.getBluetoothAddress(), id1, id2, id3, actualBeacon.getRssi(), actualBeacon.getDistance()));
                adapterTags.notifyDataSetChanged();
                binding.activityMainTxtScan.setText("Found "+beaconList.size() + " beacons" );
            }

    /*        else if(beaconList.size() == 0 && hasExtraParams) {
                // add beacon with TLM
                long unsignedTemp = (actualBeacon.getExtraDataFields().get(2) >> 8);
                double temperature = unsignedTemp > 128 ? unsignedTemp - 256 : unsignedTemp + (actualBeacon.getExtraDataFields().get(2) & 0xff) / 256.0;

                beaconList.add(new BeaconItem(actualBeacon.getBluetoothAddress(), id1, id2, id3, actualBeacon.getRssi(), actualBeacon.getExtraDataFields().get(0),
                        actualBeacon.getExtraDataFields().get(1), actualBeacon.getExtraDataFields().get(3), actualBeacon.getExtraDataFields().get(4)));
            }*/
            else {

                for (BeaconItem item : beaconList) {
                    if(!item.getAddress().equals(actualBeacon.getBluetoothAddress())) {
                            beaconList.add(new BeaconItem(actualBeacon.getBluetoothAddress(), id1, id2, id3, actualBeacon.getRssi(), actualBeacon.getDistance()));
                        adapterTags.notifyDataSetChanged();
                        binding.activityMainTxtScan.setText("Found "+beaconList.size() + " beacons" );
                    }
                }
            }
            Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away. And RSSI:" + beacons.iterator().next().getRssi() + "---" + beacons.iterator().next().getBluetoothName());
        }
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
}