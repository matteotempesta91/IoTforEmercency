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

    final BluetoothManager bluetoothManager;
    final Context context;
    private android.bluetooth.le.ScanCallback mScanCB; //qui ci sono i risultati della scansione
    private BluetoothLeScanner scanner; // nota: il BLE deve essere on per creare uno scanner
    private Handler goScanH = new Handler();
    private Handler altScanH = new Handler();
    private boolean statoScan;
    private BluetoothDevice[] fari = new BluetoothDevice[3];
    private int pos = 0;

    // Codice task ferma scanner
    private final Runnable stop = new Runnable() {
        @Override
        public void run() {
            scanner.stopScan(mScanCB);
            statoScan = false;
            Log.i("Scanning", "Stop");
            goScanH.postDelayed(start, 20000);
        }
    };

    // Codice task avvia scanner
    private final Runnable start = new Runnable() {
        @Override
        public void run() {
            scanner.startScan(mScanCB);
            statoScan = true;
            Log.i("Scanning", "Start");
            altScanH.postDelayed(stop, 2000);
        }
    };


    public BeaconListener(Context context) {
        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter blAdapter = bluetoothManager.getAdapter();
        scanner = blAdapter.getBluetoothLeScanner();
        this.context = context;
        statoScan = false;
        // Implementa cosa fare per i vari casi di callback
        mScanCB = new ScanCallback() {
            // Se trova un dispositivo
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
               // Log.i("callbackType", String.valueOf(callbackType));
               // Log.i("result", result.toString());
                addDevice(result.getDevice());
            }
            // Se fallisce la scansione
            @Override
            public void onScanFailed(int errorCode) {
                Log.e("Scan Failed", "Error Code: " + errorCode);
            }
        };

    }

    //questo get serve per inviare il contenuto di mBluetoothAdapter
    // alla HomeActivity nella quale è richiamata la funzione EnableBluetooth
    public BluetoothAdapter getmBluetoothAdapter() {
        BluetoothAdapter blAdapter = bluetoothManager.getAdapter();
        return blAdapter;
    }

    // Questo metodo avvia e ferma la scansione periodica, in base al booleano in ingresso
    public void Scansione(Boolean enable) {
        Log.i("Scanning", enable.toString());
        if (enable) {
            goScanH.postDelayed(start,5000);
        } else {
            goScanH.removeCallbacks(start);
            altScanH.removeCallbacks(stop);
            //verifica quando è già spento
            if (statoScan) {
                scanner.stopScan(mScanCB);
                Log.i("Scanning", "Stop");
            }
        }
    }

    // Aggiungi ciclicamente i nuovi disp. BLE, senza ripetizioni
    public void addDevice(BluetoothDevice faro) {
        boolean presente = false;
        for (int i=0; !presente && fari[i]!=null && i < 2; i++) {
            if (faro.getAddress().toString().equals(fari[i].getAddress().toString()))
            { presente = true; }
        }
        if (!presente) {
            fari[pos] = faro;
            if (faro.getAddress().toString().equals("B0:B4:48:BD:93:82") ) {
                // I task possono essere eseguiti solo una volta, occorre ricrearli
                BeaconDriver bDrive = new BeaconDriver(context);
                bDrive.execute(faro);
            }
        }
        if (pos < 2) { pos++; }
        else { pos = 0; }
    }
}
