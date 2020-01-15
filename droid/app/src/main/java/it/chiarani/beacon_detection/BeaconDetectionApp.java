package it.chiarani.beacon_detection;

import android.app.Application;

import it.chiarani.beacon_detection.db.AppDatabase;

public class BeaconDetectionApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getApplicationContext(), mAppExecutors);
    }
}
