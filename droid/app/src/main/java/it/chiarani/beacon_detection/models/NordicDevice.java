package it.chiarani.beacon_detection.models;

/**
 * Rappresent a Nordic Device
 */
public interface NordicDevice {
    String getAddress();

    void setAddress(String address);

    int getRssi();

    void setRssi(int rssi);

    String getName();

    void setName(String name);

    String getTimestamp();

    void setTimestamp(String timestamp);

    String getType();

    void setType(String type);
}
