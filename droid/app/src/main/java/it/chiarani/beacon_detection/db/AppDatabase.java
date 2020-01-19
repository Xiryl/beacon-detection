package it.chiarani.beacon_detection.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.chiarani.beacon_detection.db.dao.BeaconDataDao;
import it.chiarani.beacon_detection.db.dao.BeaconDeviceDao;
import it.chiarani.beacon_detection.db.dao.CustomCSVRowDao;
import it.chiarani.beacon_detection.db.entities.BeaconDataEntity;
import it.chiarani.beacon_detection.db.entities.BeaconDeviceEntity;
import it.chiarani.beacon_detection.db.entities.CustomCSVRowEntity;

@Database(entities = {BeaconDeviceEntity.class, BeaconDataEntity.class, CustomCSVRowEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    // DAO's
    public abstract BeaconDeviceDao beaconDeviceDao();
    public abstract BeaconDataDao beaconDataDao();
    public abstract CustomCSVRowDao customCSVRowDao();

    /**
     * Get a singleton istance
     * @param context
     * @return AppDatabase db istance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "database.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
