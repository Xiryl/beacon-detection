package it.chiarani.beacon_detection.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.chiarani.beacon_detection.models.NordicDevice;
import it.chiarani.beacon_detection.models.NordicEvents;

/**
 * Entity for LE scanned nordic devices
 */
@Entity(tableName = "NordicDeviceEntity")
public class NordicDeviceEntity implements NordicDevice {

    @PrimaryKey
    @NonNull
    private String address;
    private int rssi;
    private String name;
    private String timestamp;
    private String type;
    private List<NordicEvents> nordicEvents;

    /**
     * New nordic device obj
     * @param address MAC address
     * @param rssi RSSI value
     * @param name BLE device name
     */
    public NordicDeviceEntity(String address, int rssi, String name) {
        this.address = address;
        this.rssi = rssi;
        this.name = name;
        this.timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS",
                Locale.getDefault()).format(new Date());
        this.type = "NORDIC-THYNGY-52";
    }

    /**
     * MAC address
     * @return the MAC address
     */
    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int getRssi() {
        return this.rssi;
    }

    @Override
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTimestamp() {
        return this.timestamp;
    }

    @Override
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public List<NordicEvents> getNordicEvents() {
        return nordicEvents;
    }

    @Override
    public void setNordicEvents(List<NordicEvents> nordicEvents) {
        this.nordicEvents = nordicEvents;
    }
}
