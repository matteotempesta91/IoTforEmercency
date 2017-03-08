package com.progettoids.iotforemergency;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BeaconListener {

    private int[] rssi = new int[3];
    private String[] address = new String[3];
    private BluetoothAdapter mBluetoothAdapter;
    private android.bluetooth.le.ScanCallback mScanCallback; //qui ci sono i risultati della scansione
    private Boolean mScanning = false; //flag stato scan bluetooth
    private BluetoothLeScanner scanner; // nota: il BLE deve essere on per creare uno scanner
    public BeaconDriver bleDrv;

    final BluetoothManager bluetoothManager;

    public BeaconListener(Context context) {
        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Implementa cosa fare per i vari casi di callback
        mScanCallback = new ScanCallback() {
            // Se trova un dispositivo
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.i("callbackType", String.valueOf(callbackType));
                Log.i("result", result.toString());
                BluetoothDevice btDevice = result.getDevice();
                bleDrv.addDevice(btDevice);
            }
            // Se fallisce la scansione
            @Override
            public void onScanFailed(int errorCode) {
                Log.e("Scan Failed", "Error Code: " + errorCode);
            }
        };

        bleDrv = new BeaconDriver(context);

    }

    //questo get serve per inviare il contenuto di mBluetoothAdapter
    // alla HomeActivity nella quale è richiamata la funzione EnableBluetooth
    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void Scansione(Boolean enable) {
        Handler mHandler = new Handler();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Interrompe lo scan dopo 10 secondi.
        final long SCAN_PERIOD = 5000;

        Log.i("Scanning", enable.toString());

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scanner.stopScan(mScanCallback);
                    Log.i("Scanning", "Stop");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            scanner.startScan(mScanCallback);
            Log.i("Scanning", "Start");
        } else {
            mScanning = false;
            scanner.stopScan(mScanCallback);
        }
    }
}
