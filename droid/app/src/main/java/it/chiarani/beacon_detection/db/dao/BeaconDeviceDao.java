package it.chiarani.beacon_detection.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.entities.BeaconDeviceEntity;


/**
 * Database for save all the beacons found
 */
@Dao
public interface BeaconDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BeaconDeviceEntity beacon);

    @Query("SELECT * FROM beacons")
    Flowable<List<BeaconDeviceEntity>> getAsList();

    @Query("DELETE FROM beacons")
    void clear();

    @Delete
    void delete(List<BeaconDeviceEntity> beacons);
}
