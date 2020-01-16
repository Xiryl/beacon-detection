package it.chiarani.beacon_detection.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.entities.BeaconDataEntity;
import it.chiarani.beacon_detection.db.entities.BeaconDeviceEntity;

@Dao
public interface BeaconDataDao {
    @Insert
    void insert(BeaconDataEntity beaconDataEntity);

    @Query("SELECT * FROM beaconsData")
    Flowable<BeaconDataEntity> get();

    @Query("SELECT * FROM beaconsData")
    Flowable<List<BeaconDataEntity>> getAsList();

    @Query("DELETE FROM beaconsData")
    void clear();

    @Delete
    void delete(List<BeaconDataEntity> beaconDataEntityList);
}
