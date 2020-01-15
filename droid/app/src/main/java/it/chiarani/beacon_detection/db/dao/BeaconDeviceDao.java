package it.chiarani.beacon_detection.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.entities.BeaconDeviceEntity;

@Dao
public interface BeaconDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(BeaconDeviceEntity beacon);

    @Query("SELECT * FROM beacons")
    Flowable<BeaconDeviceEntity> get();

    @Query("SELECT * FROM beacons")
    Flowable<BeaconDeviceEntity> getSync();

    @Query("DELETE FROM beacons")
    void clear();

    @Delete
    void delete(List<BeaconDeviceEntity> beacons);
}
