package it.chiarani.beacon_detection.controllers;

public final class ScannerController {
    private static long scanPeriod = 1000;
    private static long betweenScanPeriod = 0;

    public ScannerController() {
    }

    public static long getScanPeriod() {
        return scanPeriod;
    }

    public static void setScanPeriod(long scanPeriod) {
        ScannerController.scanPeriod = scanPeriod;
    }

    public static long getBetweenScanPeriod() {
        return betweenScanPeriod;
    }

    public static void setBetweenScanPeriod(long betweenScanPeriod) {
        ScannerController.betweenScanPeriod = betweenScanPeriod;
    }
}
