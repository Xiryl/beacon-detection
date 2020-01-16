package it.chiarani.beacon_detection.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.chiarani.beacon_detection.models.BeaconData;

@Entity(tableName = "beaconsData")
public class BeaconDataEntity implements BeaconData {
    /**
     * Inizializza una nuova istanza di questa class
     * @param address indirizzo mac del beacon
     * @param id1 id1 del beacon
     * @param id2 id2 del beacon
     * @param id3 id3 del beacon
     * @param rssi rssi del beacon
     */
    public BeaconDataEntity(String address, String id1, String id2, String id3, int rssi) {
        this.address = address;
        this.id1 = id1;
        this.id2 = id2;
        this.id3 = id3;
        this.rssi = rssi;
        this.timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS",
                Locale.getDefault()).format(new Date());
    }

    /**
     * Restituisce l'id univoco di questa classe nel database
     * (detto in altre parole, questo campo rappresenta una chiave primaria auto incrementante.
     * Non è il massimo per grossi progetti ma in questo caso è perfetta)
     * @return id univoco di questa classe nel database
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'id univoco di questa classe nel database
     * @param id id univoco di questa classe nel database
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce l'indirizzo del beacon
     * @return indirizzo del beacon
     */
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Restituisce l'id1 del beacon
     * @return id1 del beacon
     */
    public String getId1() {
        return id1;
    }

    @Override
    public void setId1(String id1) {
        this.id1 = id1;
    }

    /**
     * Restituisce l'id2 del beacon
     * @return id2 del beacon
     */
    public String getId2() {
        return id2;
    }

    @Override
    public void setId2(String id2) {
        this.id2 = id2;
    }

    /**
     * Restituisce l'id3 del beacon
     * @return id3 del beacon
     */
    public String getId3() {
        return id3;
    }

    @Override
    public void setId3(String id3) {
        this.id3 = id3;
    }

    /**
     * Restituisce l'rssi del beacon
     * @return rssi del beacon
     */
    public int getRssi() {
        return rssi;
    }

    @Override
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     * Restituisce il timestamp di creazione dell'oggetto
     * @return timestamp di creazione dell'oggetto
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Imposta il timestamp di creazione dell'oggetto
     * @param timestamp timestamp di creazione dell'oggetto
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {return TYPE;}

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String address;
    private String id1;
    private String id2;
    private String id3;
    private int rssi;
    private String timestamp;
    private final static String TYPE = "BLE";
}
