package com.progettoids.iotforemergency;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HomeActivity extends Activity {
    private TextView txtWelcome;
    private Button btnLogout;
    private BeaconListener bleList;
    private Localizzatore locMe;
    private MapHome mapHome;
    private DriverServer mDriverServer;
    public RelativeLayout layoutHome;

    // Id necessari per tracciare le richieste effettuate al sistema
    static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mDriverServer = DriverServer.getInstance(this);
        txtWelcome = (TextView)findViewById(R.id.welcome);
        Bundle bundle = this.getIntent().getExtras();
        txtWelcome.setText(bundle.getString("welcomeMsg"));
        logout();

        mapHome = (MapHome)findViewById(R.id.IMAGEID);
        bleList = new BeaconListener(this);
        locMe = new Localizzatore(this, bleList, mapHome);

        // Richiesta dei permessi di localizzazione approssimata
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        if (EnableBluetooth()) {
            EnableGPS();
            bleList.Scansione(true);
            locMe.startFinder();
            mDriverServer.mToServer.startAmb(true);
        }

        // Registra il ricevitore per le notifiche di stato
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    //aggiunta per provare le funzioni di disegno
    @Override
    public void onPostCreate(Bundle savedInstance) {
        super.onPostCreate(savedInstance);
        layoutHome =(RelativeLayout)findViewById(R.id.activity_home);

        // Debug code
        //mapHome.disegnaEmergenza(0);         // disegna la cornice per l'emergenza
       // mapHome.disegnaPosizione(133,480,145);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // evita memory leak causato dalle bitmap
        mapHome.emptyBM();
        mapHome=null;
        bleList.stopAll();
        bleList=null;
        locMe.stopFinder();
        locMe = null;
        // Cancella il ricevitore dalle notifiche di stato
        unregisterReceiver(mReceiver);
        mDriverServer.mToServer.startAmb(false);
    }

    // Gestisce il risultato della richiesta dei permessi
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // se l'array è vuoto, la richiesta è stata annullata
        if (grantResults.length > 0) {
            switch (requestCode) {
                case PERMISSION_REQUEST_COARSE_LOCATION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (EnableBluetooth()) {
                            EnableGPS();
                            bleList.Scansione(true);
                            locMe.startFinder();
                            mDriverServer.mToServer.startAmb(true);
                        }
                    } else {
                        Log.i("Localizzazione", "Permessi di localizzazione negati");
                    }
                    break;
                }
            }
        }
    }

    // Permette di ricevere notifice sullo stato del dispositivo
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        EnableGPS();
                        bleList.Scansione(true);
                        locMe.startFinder();
                        mDriverServer.mToServer.startAmb(true);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        bleList.stopAll();
                        locMe.stopFinder();
                        //HomeActivity.this.finish();
                        // Segalare all'utente che l'app non funziona senza ble
                        break;
                }
            }
        }
    };

    public void logout(){
        btnLogout = (Button)findViewById(R.id.logout);
        btnLogout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleList.stopAll();
                locMe.stopFinder();
                finish();
            }
        });
    }

    // Abilita il BLE con il consenso dell'utente
    public boolean EnableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = bleList.getmBluetoothAdapter();
        boolean statoBLE = mBluetoothAdapter.isEnabled();
        // Controlla se il bluetooth è attivo.
        // Nel caso non lo sia ne richiede l'attivazione
        if (mBluetoothAdapter == null || !statoBLE) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        return statoBLE;
    }

    // Abilita il gps con il consenso dell'utente
    public void EnableGPS() {
        // Controlla se il bluetooth è attivo.
        LocationManager locMan = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                !locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Nel caso non lo sia ne richiede l'attivazione
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Attivare i servizi di geolocalizzazione")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}