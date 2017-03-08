package com.progettoids.iotforemergency;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Created by gaesse on 06/03/17.
 */

public class BeaconDriver {

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private Context context;
    private BluetoothGattCallback gattCB;
    private BluetoothGatt gattBLE;
    private BluetoothDevice[] fari = new BluetoothDevice[3];
    private int pos = 0;

    public BeaconDriver(Context context) {
        this.context = context;

        gattCB = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == STATE_CONNECTED) {
                    Log.i(gatt.getDevice().toString(), "Connected to GATT server.");
                    Log.i(gatt.getDevice().toString(), "Services:" + gattBLE.discoverServices());
                    // init sensors
                    getDataFromDev();
                } else if (newState == STATE_DISCONNECTED) {
                    Log.i(gatt.getDevice().toString(), "Disconnected from GATT server.");
                }
            }
        };
    }

    public void addDevice(BluetoothDevice faro) {
        boolean presente = false;
        for (int i=0; !presente && fari[i]!=null && i < 2; i++) {
            if (faro.getAddress().toString().equals(fari[i].getAddress().toString()))
            { presente = true; }
        }
        if (!presente) {
            fari[pos] = faro;
            connToDev(pos);
        }
        if (pos < 2) { pos++; }
        else { pos = 0; }
    }

    public void connToDev(int dev) {

        gattBLE = fari[dev].connectGatt(context, false, gattCB);

    }

    public void getDataFromDev() {
        // fix disconnect, handler non funziona fuori dal main
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // get data
                //gattBLE.close();
                gattBLE.disconnect();
            }
        }, 1500);
    }
}
