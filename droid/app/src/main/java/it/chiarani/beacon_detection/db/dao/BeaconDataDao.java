package it.chiarani.beacon_detection.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.entities.BeaconDataEntity;

/**
 * Database for save all the data received
 */
@Dao
public interface BeaconDataDao {
    @Insert
    void insert(BeaconDataEntity beaconDataEntity);

    @Insert
    void insertAsList(List<BeaconDataEntity> beaconDataEntity);

    @Query("SELECT * FROM beaconsData")
    Flowable<List<BeaconDataEntity>> getAsList();

    @Query("DELETE FROM beaconsData")
    void clear();

    @Delete
    void delete(List<BeaconDataEntity> beaconDataEntityList);
}
