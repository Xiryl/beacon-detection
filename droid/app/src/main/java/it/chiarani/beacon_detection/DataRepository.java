package it.chiarani.beacon_detection;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.Executor;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.AppDatabase;
import it.chiarani.beacon_detection.db.entities.BeaconDeviceEntity;

public class DataRepository {

    private static DataRepository instance;
    private final AppExecutors appExecutors;
    private final AppDatabase appDatabase;

    public static DataRepository getInstance(final Context appContext, final AppExecutors appExecutors) {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository(appContext, appExecutors);
                }
            }
        }
        return instance;
    }

    public DataRepository(Context context, AppExecutors appExecutos) {
        appDatabase = AppDatabase.getInstance(context);
        this.appExecutors = appExecutos;
    }

    public AppExecutors getAppExecutors() {
        return appExecutors;
    }

    public AppDatabase getDatabase() {
        return this.appDatabase;
    }
}
