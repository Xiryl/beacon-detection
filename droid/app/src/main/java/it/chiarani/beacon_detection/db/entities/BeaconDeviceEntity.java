package it.chiarani.beacon_detection.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.chiarani.beacon_detection.models.BeaconDevice;

/**
 * Object for insert a value inside DAO {@link it.chiarani.beacon_detection.db.dao.BeaconDeviceDao}.
 * View {@link BeaconDevice}.
 */
@Entity(tableName = "beacons", primaryKeys = {"id", "address"})
public class BeaconDeviceEntity implements BeaconDevice {

    @NonNull
    private int id;
    @NonNull
    private String address;
    private String id1;
    private String id2;
    private String id3;
    private int rssi;
    private long telemetryVersion;
    private long batteryMilliVolts;
    private long pduCount;
    private long uptime;
    private String timestamp;
    private double distance;
    private final static String TYPE = "BLE";

    public BeaconDeviceEntity(String address, String id1, String id2, String id3, int rssi, double distance, long telemetryVersion, long batteryMilliVolts, long pduCount, long uptime) {
        this.address = address;
        this.id1 = id1;
        this.id2 = id2;
        this.id3 = id3;
        this.rssi = rssi;
        this.telemetryVersion = telemetryVersion;
        this.batteryMilliVolts = batteryMilliVolts;
        this.pduCount = pduCount;
        this.uptime = uptime;
        this.distance = distance;
        this.timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS",
                Locale.getDefault()).format(new Date());
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId1() {
        return id1;
    }

    public void setId1(String id1) {
        this.id1 = id1;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getId3() {
        return id3;
    }

    public void setId3(String id3) {
        this.id3 = id3;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public long getTelemetryVersion() {
        return telemetryVersion;
    }

    public void setTelemetryVersion(long telemetryVersion) {
        this.telemetryVersion = telemetryVersion;
    }

    public long getBatteryMilliVolts() {
        return batteryMilliVolts;
    }

    public void setBatteryMilliVolts(long batteryMilliVolts) {
        this.batteryMilliVolts = batteryMilliVolts;
    }

    public long getPduCount() {
        return pduCount;
    }

    public void setPduCount(long pduCount) {
        this.pduCount = pduCount;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {return TYPE;}

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
