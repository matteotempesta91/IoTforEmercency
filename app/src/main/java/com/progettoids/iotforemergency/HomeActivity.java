package com.progettoids.iotforemergency;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {
    private TextView txtWelcome;
    private Button btnLogout;
    private BeaconListener bleList;

    // Id necessari per tracciare le richieste effettuate al sistema
    static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        txtWelcome = (TextView)findViewById(R.id.welcome);
        Bundle bundle = this.getIntent().getExtras();
        txtWelcome.setText(bundle.getString("welcomeMsg"));
        logout();
        bleList = new BeaconListener(this);

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
        }
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
                        }
                    } else {
                        Log.i("Localizzazione", "Permessi di localizzazione negati");
                    }
                    return;
                }
            }
        }
    }

    // Gestisce il risultato della richiesta di attivazione del bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_OK) {
                    EnableGPS();
                    bleList.Scansione(true);
                } else {
                    Log.i("BLE", "Non attivo");
                }
                return;
            }
        }
    }

    public void logout(){
        btnLogout = (Button)findViewById(R.id.logout);
        btnLogout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeActivity.this.finish();
            }
        });
    }

    // Abilita il BLE con il consenso dell'utente
    public boolean EnableBluetooth(){
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