package it.chiarani.beacon_detection.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Object for insert a value inside DAO {@link it.chiarani.beacon_detection.db.dao.CustomCSVRowDao}.
 * This is a custom CSV Row
 */
@Entity(tableName = "customrow")
public class CustomCSVRowEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String csvRow;
    private String timestamp;

    public CustomCSVRowEntity(String csvRow) {
        this.csvRow = csvRow;
        this.timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS",
                Locale.getDefault()).format(new Date());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCsvRow() {
        return csvRow;
    }

    public void setCsvRow(String csvRow) {
        this.csvRow = csvRow;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
