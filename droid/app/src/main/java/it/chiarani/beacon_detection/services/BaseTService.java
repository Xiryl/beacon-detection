package it.chiarani.beacon_detection.services;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import androidx.annotation.Nullable;

import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyConnection;

/**
 * Nordic Thingy Service
 */
public class BaseTService extends BaseThingyService {

    public class ThingyBinder extends BaseThingyBinder{

        @Override
        public ThingyConnection getThingyConnection(BluetoothDevice device) {
            return mThingyConnections.get(device);
        }
    }

    @Nullable
    @Override
    public BaseThingyBinder onBind(Intent intent) {
        return new ThingyBinder();
    }
}