package it.chiarani.beacon_detection.services;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.controllers.ScannerController;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.BeaconDeviceEntity;

public class BeaconDiscoverService extends Service implements BeaconConsumer, RangeNotifier {


    private BeaconManager mBeaconManager;
    private Region beaconRegion;
    private AppExecutors mAppExecutors;
    private AppDatabase mAppDatabase;

    // Possibili azioni che il servizio può intraprendere
    // START: avvia il polling
    // STOP: arresta il polling
    public enum ACTIONS {
        START,
        STOP
    }

    /**
     * Fa partire il service. Bisogna chiamare stopSelf() oppure stopService() per farlo terminare
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ACTIONS action = ACTIONS.valueOf(intent.getAction());

        Log.d(BeaconDiscoverService.class.getSimpleName(),"Received action:"+ action);

        if (action == ACTIONS.START) {
            Log.d(BeaconDiscoverService.class.getSimpleName(), "Start discovery");

            // L'avvio del monitoraggio viene demandato ad un'altra funzione, per chiarità
            startDiscovery();
        }
        else if (action == ACTIONS.STOP) {
            Log.d(BeaconDiscoverService.class.getSimpleName(), "Stop discovery");

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
            Log.e(BeaconDiscoverService.class.getSimpleName(), e.getMessage());
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

        mBeaconManager.setForegroundScanPeriod(ScannerController.getScanFrequencyPeriod());
        mBeaconManager.setForegroundBetweenScanPeriod(ScannerController.getBetweenScanPeriod());

    }

    /**
     * Invocato quando viene distrutto. Qua vanno rimossi tutti i listener, threads e receivers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDiscovery();
        mBeaconManager.unbind(this);
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

        try {
            mBeaconManager.startRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            Log.e(BeaconDiscoverService.class.getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.i(BeaconDiscoverService.class.getSimpleName(), String.format("Found %s beacons in range", beacons.size()));
        if (beacons.size() > 0) {
            for(Beacon b : beacons) {

                Beacon actualBeacon = b;
                String id1 = "null";
                try {
                    id1 = actualBeacon.getId1().toString();
                }
                catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }

                String id2 = "null";
                try {
                    id2 = actualBeacon.getId2().toString();
                }
                catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }

                String id3 = "null";
                try {
                    id3 = actualBeacon.getId3().toString();
                }
                catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }

                BeaconDeviceEntity tmp = new BeaconDeviceEntity(actualBeacon.getBluetoothAddress(), id1, id2, id3, actualBeacon.getRssi(), actualBeacon.getDistance(), 0,0,0,0);
                // add beacon without tlm

                mAppExecutors.diskIO().execute(() -> mAppDatabase.beaconDeviceDao().insert(tmp));
                Log.i(BeaconDiscoverService.class.getSimpleName(), "The first beacon I see is about "+actualBeacon.getDistance()+" meters away. And RSSI:" + actualBeacon.getRssi() + "---" + actualBeacon.getBluetoothName());

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
