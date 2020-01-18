package it.chiarani.beacon_detection.controllers;

public final class ScannerController {
    private static long scanFrequencyPeriod = 1000;
    private static long betweenScanPeriod = 0;
    private static long scanTime = 30000;
    private static long collectDataDuration = 720000;

    public ScannerController() {
    }

    public static long getScanFrequencyPeriod() {
        return scanFrequencyPeriod;
    }

    public static void setScanFrequencyPeriod(long scanFrequencyPeriod) {
        ScannerController.scanFrequencyPeriod = scanFrequencyPeriod;
    }

    public static long getBetweenScanPeriod() {
        return betweenScanPeriod;
    }

    public static void setBetweenScanPeriod(long betweenScanPeriod) {
        ScannerController.betweenScanPeriod = betweenScanPeriod;
    }

    public static long getScanTime() {
        return scanTime;
    }

    public static void setScanTime(long scanTime) {
        ScannerController.scanTime = scanTime;
    }

    public static long getCollectDataDuration() {
        return collectDataDuration;
    }

    public static void setCollectDataDuration(long collectDataDuration) {
        ScannerController.collectDataDuration = collectDataDuration;
    }
}
