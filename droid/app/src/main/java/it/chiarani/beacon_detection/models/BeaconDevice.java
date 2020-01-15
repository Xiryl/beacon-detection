package it.chiarani.beacon_detection.models;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Rappresenta un beacon
 */

public interface BeaconDevice {

    int getId();

    void setId(int id);

    String getAddress();

    void setAddress(String address);

    String getId1();

    void setId1(String id1);

    String getId2();

    void setId2(String id2);

    String getId3();

    void setId3(String id3);

    int getRssi();

    void setRssi(int rssi);

    long getTelemetryVersion();

    void setTelemetryVersion(long telemetryVersion);

    long getBatteryMilliVolts();

    void setBatteryMilliVolts(long batteryMilliVolts);

    long getPduCount();

    void setPduCount(long pduCount);

    long getUptime();

    void setUptime(long uptime);

    String getTimestamp();

    void setTimestamp(String timestamp);

    String getType();

    double getDistance();

    void setDistance(double distance);

}

