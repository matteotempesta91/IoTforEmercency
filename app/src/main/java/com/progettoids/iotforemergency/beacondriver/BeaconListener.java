package com.progettoids.iotforemergency.beacondriver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.progettoids.iotforemergency.gestionedati.Parametri;

import java.util.ArrayList;

public class BeaconListener {

    private final BluetoothManager bluetoothManager;
    private final Context context;
    private android.bluetooth.le.ScanCallback mScanCB; //qui ci sono i risultati della scansione
    private BluetoothLeScanner scanner; // nota: il BLE deve essere on per creare uno scanner
    private Handler scanH = new Handler();
    private boolean statoScan;
    private ArrayList<ScanResult> fari = new ArrayList<>();
    private int refresh = 0;
    private Parametri mParametri;

    // Codice task ferma scanner
    private final Runnable stop = new Runnable() {
        @Override
        public void run() {
            if (bluetoothManager.getAdapter().isEnabled()) {
                scanner.stopScan(mScanCB);
            }
            statoScan = false;
            Log.i("Scanning", "Stop");
            scanH.postDelayed(start, mParametri.timerScan());
        }
    };

    // Codice task avvia scanner
    private final Runnable start = new Runnable() {
        @Override
        public void run() {
            // ogni due scansioni si rinnova la lista
            if (refresh > 1) {
                refresh = 0;
                fari.clear();
            }
            refresh++;
            if (bluetoothManager.getAdapter().isEnabled()) {
                scanner.startScan(mScanCB);
                statoScan = true;
            }
            Log.i("Scanning", "Start");
            scanH.postDelayed(stop, mParametri.T_SCAN_PERIOD);
        }
    };


    public BeaconListener(Context context) {
        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.context = context;
        mParametri = Parametri.getInstance();
        statoScan = false;
        // Implementa cosa fare per i vari casi di callback
        mScanCB = new ScanCallback() {
            // Se trova un dispositivo
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
               // Log.i("callbackType", String.valueOf(callbackType));
               // Log.i("result", result.toString());
                addDevice(result);
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
        // Lo scanner è invocato solo prima della scansione per attendere che il ble sia attivo
        BluetoothAdapter blAdapter = bluetoothManager.getAdapter();
        if (scanner == null) {
            scanner = blAdapter.getBluetoothLeScanner();
        }
        if (scanner != null) {
            Log.i("Scanning", enable.toString());
            if (enable) {
                scanH.post(start);
            } else {
                scanH.removeCallbacks(start);
                scanH.removeCallbacks(stop);
                //verifica quando non scansiona ed il bluetooth è acceso (nel caso l'utente lo spenga manualmente)
                if (statoScan && blAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    scanner.stopScan(mScanCB);
                    Log.i("Scanning", "Stop");
                }
            }
        } else { Log.i("Scanner ", "null"); }
    }

    // Aggiungi ciclicamente i nuovi disp. BLE, senza ripetizioni
    private void addDevice(ScanResult result) {
        boolean presente = false;
        BluetoothDevice foundDev = result.getDevice();
        String nomeDev;
        try {
            nomeDev = foundDev.getName();
        } catch (Exception ex) {
            Log.i("BeaconListener","Errore getName");
            nomeDev = "NN";
        }
        // Vengono considerati solo i dispositivi bluetooth con nome definito nel filtro
        if (mParametri.FILTRO_BLE_DEVICE.equals(nomeDev)) {
            for (ScanResult faroNoto : fari) {
                if (foundDev.getAddress().equals(faroNoto.getDevice().getAddress())) {
                    presente = true;
                }
            }
            if (!presente) {
                fari.add(result);
                testWait(foundDev);
            }
        }
    }

    // Ricerca beacon vicino
    public String closestBle() {
        ScanResult vicino = null;
        int rilevati = fari.size();
        if (rilevati > 0) {
            vicino = fari.get(0);
        }
        if (rilevati > 1) {
            for (ScanResult faroNoto : fari) {
                if (vicino != null) {
                    if (faroNoto.getRssi() > vicino.getRssi()) {
                        vicino = faroNoto;
                    }
                } else {
                    Log.i("beaconListener", "Errore closestBle");
                }
            }
        }
        String macAdrs = "NN";
        if (vicino != null) {
            macAdrs = vicino.getDevice().getAddress();
        }
        return macAdrs;
    }

    // Semaforo binario per sincronizzare letture da più sensori
    private boolean lock = false;
    private ArrayList<BluetoothDevice> daLeggere = new ArrayList<>();
    private BeaconDriver bDrive;

    // Verifica se il ble è occupato e mette in coda
    private void testWait(BluetoothDevice beacon) {
        if (lock) {
            daLeggere.add(beacon);
        } else {
            lock = true;
            // I task possono essere eseguiti solo una volta, occorre ricrearli
            bDrive = new BeaconDriver(context);
            bDrive.execute(this, beacon);
        }
    }

    // Riceve il segnale di via libera dal task che ha terminato
    void signal() {
        bDrive = null;
        if (!daLeggere.isEmpty()) {
            BluetoothDevice beacon = daLeggere.get(0);
            daLeggere.remove(0);
            // I task possono essere eseguiti solo una volta, occorre ricrearli
            bDrive = new BeaconDriver(context);
            bDrive.execute(this, beacon);
        } else {
            lock = false;
        }
    }

    // Libera la risorsa, nota: cancel() attende comunque doinbackground() finisca ma ignora il risultato
    public void stopAll() {
        daLeggere.clear();
        Scansione(false);
        if (bDrive != null) {
        bDrive.cancel(true);
        }
    }
}
