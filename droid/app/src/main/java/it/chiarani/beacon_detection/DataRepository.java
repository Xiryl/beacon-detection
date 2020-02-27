package it.chiarani.beacon_detection;

import android.content.Context;

import it.chiarani.beacon_detection.db.AppDatabase;

/**
 * App repository, collect the istances
 */
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

    DataRepository(Context context, AppExecutors appExecutos) {
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
