package it.chiarani.beacon_detection.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import it.chiarani.beacon_detection.AppExecutors;
import it.chiarani.beacon_detection.BeaconDetectionApp;
import it.chiarani.beacon_detection.R;
import it.chiarani.beacon_detection.controllers.Helpers;
import it.chiarani.beacon_detection.controllers.ScannerController;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.BeaconDataEntity;
import it.chiarani.beacon_detection.db.entities.CustomCSVRowEntity;
import it.chiarani.beacon_detection.models.BeaconData;
import it.chiarani.beacon_detection.views.MainActivity;

public class BeaconDataCollectorService extends Service implements BeaconConsumer, RangeNotifier {


    private BeaconManager mBeaconManager;
    private Region beaconRegion;
    private AppExecutors mAppExecutors;
    private AppDatabase mAppDatabase;
    private Map<String, Integer> beaconsPerRow = new TreeMap<>();
    private TreeMap<String, Integer> orderRow = new TreeMap<>();
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


        if (action == ACTIONS.START) {
            Log.d(BeaconDataCollectorService.class.getSimpleName(), "Start discovery");

            availableAddresses = intent.getStringArrayListExtra("AVAILABLEADRESSES");
            for(String x : availableAddresses) {
                orderRow.put(x, 1);
            }

            Log.d(BeaconDataCollectorService.class.getSimpleName(),"Received action:"+ action);

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


        String csvLine = "Dati di sessione con durata: "+ ScannerController.getCollectDataDuration() +", e frequenza (ms): "+ScannerController.getScanFrequencyPeriod()+"\n";

        for(Map.Entry<String, Integer> entry : orderRow.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            csvLine += "timestamp;" + key;
        }

        CustomCSVRowEntity row = new CustomCSVRowEntity(csvLine);
        mAppExecutors.diskIO().execute(() -> mAppDatabase.customCSVRowDao().insert(row));
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

        //mBeaconManager.bind(this);
        beaconRegion = new Region("beacon_region", null, null, null);

        // In this example, we will use Eddystone protocol, so we have to define it here
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        mBeaconManager.setAndroidLScanningDisabled(true);
        //mBeaconManager.setForegroundScanPeriod(ScannerController.getScanFrequencyPeriod());
        //mBeaconManager.setForegroundBetweenScanPeriod(ScannerController.getBetweenScanPeriod());
        mBeaconManager.setBackgroundBetweenScanPeriod(0);
        mBeaconManager.setBackgroundScanPeriod(110l);
        mBeaconManager.setForegroundScanPeriod(110l);
        mBeaconManager.setForegroundBetweenScanPeriod(0);

        setNotification(1);
    }

    /**
     * Invocato quando viene distrutto. Qua vanno rimossi tutti i listener, threads e receivers
     */
    @Override
    public void onDestroy() {
        setNotification(0);
        super.onDestroy();
        stopDiscovery();
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mBeaconManager.unbind(this);
        Toast.makeText(this, "Collector service TERMINATED.", Toast.LENGTH_SHORT).show();
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

        /* mCountDownTimer = new CountDownTimer(ScannerController.getCollectDataDuration() ,ScannerController.getScanFrequencyPeriod()) {

            @Override
            public void onTick(long millisUntilFinished) {
                Map<String, Integer> tmpHashmap = new TreeMap<>(beaconsPerRow);

                String csvLine = "";

                if(tmpHashmap.size() == 0) {
                    beaconsPerRow.clear();
                    return;
                }

                for(Map.Entry<String, Integer> entry : orderRow.entrySet()) {
                    if(tmpHashmap.containsKey(entry.getKey())) {
                        Integer value = tmpHashmap.get(entry.getKey());
                        csvLine += ";" + value;
                    }else {
                        csvLine += ";0";
                    }
                }


                CustomCSVRowEntity row = new CustomCSVRowEntity(csvLine + "");
                mAppExecutors.diskIO().execute(() -> mAppDatabase.customCSVRowDao().insert(row));
                beaconsPerRow.clear();
                // setNotification(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                stopSelf();
            }
        };

        mCountDownTimer.start();*/

        try {
            mBeaconManager.startRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            Log.e(BeaconDataCollectorService.class.getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        //List<BeaconDataEntity> tmp = new ArrayList<>();
        Map<String, Integer> tmpHashmap = new TreeMap<>(beaconsPerRow);
        //String x = "";
        StringBuilder x = new StringBuilder();
        Log.i(BeaconDataCollectorService.class.getSimpleName(), String.format("Found %s beacons in range", beacons.size()));
            for(Beacon b : beacons) {


                x.append(";" + b.getBluetoothAddress() + ", RSSI:"+ b.getRssi() + ";");

              //  tmpHashmap.put(b.getBluetoothAddress(), b.getRssi());

                // tmp.add(new BeaconDataEntity(b.getBluetoothAddress(), id1, id2, id3, b.getRssi()));
                // add beacon without tlm

                 // Log.i(BeaconDataCollectorService.class.getSimpleName(), "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away. And RSSI:" + beacons.iterator().next().getRssi() + "---" + beacons.iterator().next().getBluetoothName());

            }

      /*      StringBuilder csvLine = new StringBuilder();
            for(Map.Entry<String, Integer> entry : orderRow.entrySet()) {
                if(tmpHashmap.containsKey(entry.getKey())) {
                    Integer value = tmpHashmap.get(entry.getKey());
                    csvLine.append(";" + value);
                }else {
                    csvLine.append(";0");
                }
            }*/

            mAppExecutors.diskIO().execute(() -> mAppDatabase.customCSVRowDao().insert(new CustomCSVRowEntity(x.toString())));

            // mAppExecutors.diskIO().execute(() -> mAppDatabase.beaconDataDao().insertAsList(tmp));
            //mAppExecutors.diskIO().execute(() -> mAppDatabase.customCSVRowDao().insert(new CustomCSVRowEntity(x.toString())));


     }


    private void setNotification(long beaconsFoundSoFar) {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        // Se il parametro della funzione è -1 visualizzo un messaggio di errore sulla notifica
        String notificationTitle = "Beacon detection";
        String notificationMessage = "collecting... ";

        if(beaconsFoundSoFar == 0) {
            notificationTitle = "Beacon detection";
            notificationMessage = "End.";
        }

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        Notification notification = new NotificationCompat.Builder(this, "misc")
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSmallIcon(R.drawable.ic_ble)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "misc",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
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
