package it.chiarani.beacon_detection.controllers;

/**
 * Class used for share and save the timing constraints of the data collection session
 */
public final class ScannerController {
    private static long scanFrequencyPeriod = 1000;
    private static long betweenScanPeriod = 0;
    private static long scanTime = 10000;
    private static long collectDataDuration = 50000;
    private static long qtaPeople = 5;

    public ScannerController() {
    }

    /**
     * Frequency wich service scan for retrive the beacons data (in ms)
     * View {@link it.chiarani.beacon_detection.services.BeaconDataCollectorService} for the service
     * @return (ms) frequency of beacons scan
     */
    public static long getScanFrequencyPeriod() {
        return scanFrequencyPeriod;
    }

    public static void setScanFrequencyPeriod(long scanFrequencyPeriod) {
        ScannerController.scanFrequencyPeriod = scanFrequencyPeriod;
    }

    /**
     * Period between two beacon scan (in ms)
     * View {@link it.chiarani.beacon_detection.services.BeaconDataCollectorService} for the service
     * @return (ms) Period between two beacon scan
     */
    public static long getBetweenScanPeriod() {
        return betweenScanPeriod;
    }

    public static void setBetweenScanPeriod(long betweenScanPeriod) {
        ScannerController.betweenScanPeriod = betweenScanPeriod;
    }

    /**
     * Period of the scan (in ms)
     * @return (ms) Period of the scan
     */
    public static long getScanTime() {
        return scanTime;
    }

    public static void setScanTime(long scanTime) {
        ScannerController.scanTime = scanTime;
    }

    /**
     * Period of the data collection session (in ms)
     * View {@link it.chiarani.beacon_detection.services.BeaconDataCollectorService} for the service
     * @return (ms) Period of the data collection session
     */
    public static long getCollectDataDuration() {
        return collectDataDuration;
    }

    public static void setCollectDataDuration(long collectDataDuration) {
        ScannerController.collectDataDuration = collectDataDuration;
    }

    public static long getQtaPeople() {
        return qtaPeople;
    }
}
