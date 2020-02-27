package it.chiarani.beacon_detection.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.chiarani.beacon_detection.db.dao.NordicDeviceDao;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;

@Database(entities = {NordicDeviceEntity.class}, version = 2, exportSchema = false)
@TypeConverters({NordicEventsConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    /**
     * Nordic device DAO
     * @return Nordic device DAO
     */
    public abstract NordicDeviceDao nordicDeviceDao();

    /**
     * Singletone istance
     * @param context ctx
     * @return AppDatabase istance
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
