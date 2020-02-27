package it.chiarani.beacon_detection.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.entities.NordicDeviceEntity;

/**
 * Dao for nordic device view entity: {@link it.chiarani.beacon_detection.db.entities.NordicDeviceEntity}
 */
@Dao
public interface NordicDeviceDao {
    /**
     * Insert a new device
     * @param nordicDevice device
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NordicDeviceEntity nordicDevice);

    /**
     * Return all devices as list
     * @return
     */
    @Insert
    void insertAsList(List<NordicDeviceEntity> nordicDeviceEntityList);

    /**
     * Return all devices as list
     * @return
     */
    @Query("SELECT * FROM NordicDeviceEntity")
    Flowable<List<NordicDeviceEntity>> getAsList();

    /**
     * Clear the database
     */
    @Query("DELETE FROM NordicDeviceEntity")
    void clear();

    /**
     * Delete a list of devices
     * @param nordicDevices
     */
    @Delete
    void delete(List<NordicDeviceEntity> nordicDevices);
}
