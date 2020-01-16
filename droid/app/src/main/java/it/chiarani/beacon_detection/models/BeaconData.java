package it.chiarani.beacon_detection.models;

public interface BeaconData {
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

    String getTimestamp();

    void setTimestamp(String timestamp);

    String getType();
}
