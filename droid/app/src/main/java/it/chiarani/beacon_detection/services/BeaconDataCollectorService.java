package it.chiarani.beacon_detection.services;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.controllers.ScannerController;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.BeaconDataEntity;
import it.chiarani.beacon_detection.db.entities.CustomCSVRowEntity;
import it.chiarani.beacon_detection.models.BeaconData;

public class BeaconDataCollectorService extends Service implements BeaconConsumer, RangeNotifier {


    private BeaconManager mBeaconManager;
    private Region beaconRegion;
    private AppExecutors mAppExecutors;
    private AppDatabase mAppDatabase;
    private Map<String, Integer> beaconsPerRow = new HashMap<>();
    private CountDownTimer mCountDownTimer;

    // Possibili azioni che il servizio può intraprendere
    // START: avvia il polling
    // STOP: arresta il polling
    public enum ACTIONS {
        START,
        STOP
    }

    public List<String> availableAddresses = new ArrayList<>();

    /**
     * Fa partire il service. Bisogna chiamare stopSelf() oppure stopService() per farlo terminare
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ACTIONS action = ACTIONS.valueOf(intent.getAction());
        availableAddresses = intent.getStringArrayListExtra("AVAILABLEADRESSES");

        Log.d(BeaconDataCollectorService.class.getSimpleName(),"Received action:"+ action);

        if (action == ACTIONS.START) {
            Log.d(BeaconDataCollectorService.class.getSimpleName(), "Start discovery");

            // L'avvio del monitoraggio viene demandato ad un'altra funzione, per chiarità
            startDiscovery();
        }
        else if (action == ACTIONS.STOP) {
            Log.d(BeaconDataCollectorService.class.getSimpleName(), "Stop discovery");

            stopDiscovery();

            stopSelf();
        }

        // START_STICKY garantisce che se il servizio viene arresto dal SO, esso sarà riavviato
        // NOTA: l'applicazione DEVE essere attiva in background. L'arresto dell'applicazione
        // comporta l'arresto del servizio
        return Service.START_STICKY;
    }

    private void startDiscovery() {
        //Binds this activity to the BeaconService
        mBeaconManager.bind(this);
    }

    private void stopDiscovery() {
        try {
            mBeaconManager.stopRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            Log.e(BeaconDataCollectorService.class.getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Esecuzione di "setup" del service. Avviene solo una volta dopo il startcommand()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("I ", "BeaconDiscoverService Started");

        mAppExecutors = ((BeaconDetectionApp)getApplication()).getRepository().getAppExecutors();
        mAppDatabase = ((BeaconDetectionApp)getApplication()).getRepository().getDatabase();

        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        beaconRegion = new Region("beacon_region", null, null, null);

        // In this example, we will use Eddystone protocol, so we have to define it here
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));

        //mBeaconManager.setForegroundScanPeriod(ScannerController.getScanFrequencyPeriod());
        //mBeaconManager.setForegroundBetweenScanPeriod(ScannerController.getBetweenScanPeriod());
        mBeaconManager.setForegroundScanPeriod(ScannerController.getScanFrequencyPeriod());
        mBeaconManager.setForegroundBetweenScanPeriod(0);
    }

    /**
     * Invocato quando viene distrutto. Qua vanno rimossi tutti i listener, threads e receivers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDiscovery();
        mCountDownTimer.cancel();
        mBeaconManager.unbind(this);
        Toast.makeText(this, "dddddddTERMINATED.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Non fa binding, dunque ritorna null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(this);

        mCountDownTimer = new CountDownTimer(ScannerController.getCollectDataDuration() ,ScannerController.getScanFrequencyPeriod()) {

            @Override
            public void onTick(long millisUntilFinished) {
                HashMap<String, Integer> tmpHashmap = new HashMap<>();
               tmpHashmap.putAll(beaconsPerRow);
                String csvLine = "";

                for(Map.Entry<String, Integer> entry : tmpHashmap.entrySet()) {
                    String key = entry.getKey();
                    Integer value = entry.getValue();

                    csvLine += ";" + key +"-"+value;
                }
                CustomCSVRowEntity row = new CustomCSVRowEntity(csvLine + "");
                mAppExecutors.diskIO().execute(() -> mAppDatabase.customCSVRowDao().insert(row));
                beaconsPerRow.clear();
            }

            @Override
            public void onFinish() {
            }
        };

        mCountDownTimer.start();

        try {
            mBeaconManager.startRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            Log.e(BeaconDataCollectorService.class.getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.i(BeaconDataCollectorService.class.getSimpleName(), String.format("Found %s beacons in range", beacons.size()));
        if (beacons.size() > 0) {
            for(Beacon b : beacons) {
                if(!availableAddresses.contains(b.getBluetoothAddress())) {
                    break;
                }

                // insert the beacon in the hashmap "row"
                beaconsPerRow.put(b.getBluetoothAddress(), b.getRssi());
                String id1 = "null";
                try {
                    id1 = b.getId1().toString();
                }
                catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }

                String id2 = "null";
                try {
                    id2 = b.getId2().toString();
                }
                catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }

                String id3 = "null";
                try {
                    id3 = b.getId3().toString();
                }
                catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }

                BeaconDataEntity tmp = new BeaconDataEntity(b.getBluetoothAddress(), id1, id2, id3, b.getRssi());
                // add beacon without tlm

                mAppExecutors.diskIO().execute(() -> mAppDatabase.beaconDataDao().insert(tmp));
                Log.i(BeaconDataCollectorService.class.getSimpleName(), "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away. And RSSI:" + beacons.iterator().next().getRssi() + "---" + beacons.iterator().next().getBluetoothName());

            }
        }
     }


    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return getApplication().getApplicationContext().bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        getApplication().unbindService(conn);
    }
}
