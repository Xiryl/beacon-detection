package it.chiarani.beacon_detection.db;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import it.chiarani.beacon_detection.db.dao.BeaconDeviceDao;
import it.chiarani.beacon_detection.db.entities.BeaconDeviceEntity;

public class DataSource {

    private final BeaconDeviceDao beaconDeviceDao;

    public DataSource(BeaconDeviceDao beaconDeviceDao) {
        this.beaconDeviceDao = beaconDeviceDao;
    }



}
