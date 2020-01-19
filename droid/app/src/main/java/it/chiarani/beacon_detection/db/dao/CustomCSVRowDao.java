package it.chiarani.beacon_detection.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.entities.CustomCSVRowEntity;


/**
 * Database for save all the data as CSV
 */
@Dao
public interface CustomCSVRowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CustomCSVRowEntity customCSVRowEntity);

    @Query("SELECT * FROM customrow")
    Flowable<List<CustomCSVRowEntity>> getAsList();

    @Query("DELETE FROM customrow")
    void clear();

    @Delete
    void delete(List<CustomCSVRowEntity> customCSVRowEntityList);
}

