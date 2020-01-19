package it.chiarani.beacon_detection.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.chiarani.beacon_detection.models.BeaconData;

/**
 * Object for insert a value inside DAO {@link it.chiarani.beacon_detection.db.dao.BeaconDataDao}.
 * View {@link BeaconData}.
 */
@Entity(tableName = "beaconsData")
public class BeaconDataEntity implements BeaconData {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String address;
    private String id1;
    private String id2;
    private String id3;
    private int rssi;
    private String timestamp;
    private final static String TYPE = "BLE";

    public BeaconDataEntity(String address, String id1, String id2, String id3, int rssi) {
        this.address = address;
        this.id1 = id1;
        this.id2 = id2;
        this.id3 = id3;
        this.rssi = rssi;
        this.timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS",
                Locale.getDefault()).format(new Date());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    public String getId1() {
        return id1;
    }

    @Override
    public void setId1(String id1) {
        this.id1 = id1;
    }

    public String getId2() {
        return id2;
    }

    @Override
    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getId3() {
        return id3;
    }

    @Override
    public void setId3(String id3) {
        this.id3 = id3;
    }

    public int getRssi() {
        return rssi;
    }

    @Override
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {return TYPE;}

}
