package com.progettoids.iotforemergency.gui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.progettoids.iotforemergency.db.DBHelper;
import com.progettoids.iotforemergency.gestionedati.DriverServer;
import com.progettoids.iotforemergency.gestionedati.Localizzatore;
import com.progettoids.iotforemergency.gestionedati.Login;
import com.progettoids.iotforemergency.R;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class LoginActivity extends Activity {
    final Context context = this;
    private Button btnLogin, btnLoginGuest, btnRegistrati;
    private EditText editUser, editPass;
    private CheckBox ricordami;
    private Login loginUtils;
    private boolean flag1stLog;
    private DriverServer mDriverServer;
    private DBHelper mDBhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginUtils = new Login();
        flag1stLog = true;
        mDBhelper = DBHelper.getInstance(context);
        mDriverServer = DriverServer.getInstance(context);
        login();
        registrazione();
        btnLoginGuest=(Button)findViewById(R.id.buttonLoginGuest);
        btnLoginGuest.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                mDriverServer.inviaLoginGuest(getMacAddr());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    // Pulsante login
    public void login() {
        btnLogin=(Button)findViewById(R.id.buttonLogin);
        editUser=(EditText)findViewById(R.id.username);
        editPass=(EditText)findViewById(R.id.password);
        ricordami=(CheckBox)findViewById(R.id.Ricordami);
        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = reader.edit();

        // Aggiunge le variabili per ricordare l'ultima posizione anche alla distruzione della GUI Home
        Localizzatore.addPosContext(context);

        // carica dati utente salvati se presenti
        File path = context.getCacheDir();
        final File memo = new File(path, "memo");
        if (memo.exists()) {
            try {
                String[] userPwd = loginUtils.loadLogin(memo);
                if (userPwd.length >= 2) {
                    ricordami.setChecked(true);
                    editUser.setText(userPwd[0]);
                    editPass.setText(userPwd[1]);
                }
            } catch (Exception ex) {
                flag1stLog = false;
                AlertDialog.Builder miaAlert = new AlertDialog.Builder(context);
                miaAlert.setTitle("Error");
                miaAlert.setMessage(ex.getMessage());
                AlertDialog alert = miaAlert.create();
                alert.show();
            }
        }
        btnLogin.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                if(controlloCampi())
                {
                    // Controlla se Username e Password sono presenti nel server
                    if(mDriverServer == null) {
                        Log.i("mDriverServer","is NULL");
                    } else {
                        Log.i("mDriverServer","not null");
                    }
                    editor.putString("id_utente", editUser.getText().toString());
                    editor.apply();
                    mDriverServer.verificaLogin(editUser.getText().toString(),editPass.getText().toString());

                    // salva i dati solo se checkbox è segnato
                    if (ricordami.isChecked()) {
                        File path = context.getCacheDir();
                        File memo = new File(path, "memo");
                        try {
                            loginUtils.saveLogin(context, memo, editUser.getText().toString(), editPass.getText().toString());
                        } catch (Exception ex) {
                            AlertDialog.Builder miaAlert = new AlertDialog.Builder(context);
                            miaAlert.setTitle("Error");
                            miaAlert.setMessage(ex.getMessage());
                            AlertDialog alert = miaAlert.create();
                            alert.show();
                        }
                    }
                } else {
                    AlertDialog.Builder miaAlert = new AlertDialog.Builder(context);
                    miaAlert.setTitle("Login Error");
                    miaAlert.setMessage("Carattere inserito non valido");
                    AlertDialog alert = miaAlert.create();
                    alert.show();
                }
            }
        });

        // auto Login solo al primo avvio
        if (flag1stLog && memo.exists()) {
            btnLogin.callOnClick();
            flag1stLog = false;
        }
    }

    // Invocato da driverServer
    public void loginGuest(boolean riuscito) {
        Bundle bundle = new Bundle();
        String macAdrress;
        if (riuscito) {
            bundle.putString("welcomeMsg", "Benvenuto Utente Guest");
            macAdrress=getMacAddr();
            Log.i("macAddress:",macAdrress);
        } else {
            bundle.putString("welcomeMsg", "Modalità Offline");
            bundle.putString("offline", "Si sta usando la modalità offline \n Uscire e rientrare per riprovare ad accedere al server");
            // Settare a 0000 l'id permetterà di distinguere la modlaità offline in ogni parte del software
            macAdrress="0000";
        }
        Intent openHomeGuest = new Intent(LoginActivity.this, HomeActivity.class);
        openHomeGuest.putExtras(bundle);
        startActivity(openHomeGuest);
        // SALVA L'ID UTENTE GUEST COME VARIABILE GLOBALE
        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = reader.edit();
        editor.putString("id_utente", macAdrress);
        editor.apply();
    }

    // Recupera il macAddress del dispositivo wifi del telefono e lo associa all'utente guest
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            Log.i("loginaActivity","errore " + ex.toString());
        }
        return "02:00:00:00:00:00";
    }

    // Pulsante registrazione
    public void registrazione() {
        btnRegistrati=(Button)findViewById(R.id.buttonRegistrati);
        btnRegistrati.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {

                Intent openRegistrazione = new Intent(LoginActivity.this, RegistrazioneActivity.class);
                startActivity(openRegistrazione);
            }
        });
    }

    // Verifica che nei campi login e pwd non siano presenti caratteri illeciti
    public boolean controlloCampi() {
        boolean ret = false;
        String user = editUser.getText().toString();
        String pass = editPass.getText().toString();
        if ( !user.equals("") && !pass.equals("")) {
            if (Pattern.matches("[a-zA-Z0-9_-]*", user) && Pattern.matches("[a-zA-Z0-9_-]*", pass)) {
                ret = true;
            }
        }
        return ret;
    }

    // Verifica se l'App è avviata per la prima volta
    public static boolean isFirst(Context context){

        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        final boolean first = reader.getBoolean("is_first", true);
        if(first){
            final SharedPreferences.Editor editor = reader.edit();
            editor.putBoolean("is_first", false);
            editor.apply();
        }
        return first;
    }

    public static void setFirst(Context context){
        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = reader.edit();
            editor.putBoolean("is_first", true);
            editor.apply();
    }

    // Mostra un dialog se il login ha successo o meno
    public void mostraDialog(Boolean flag) {

        // Se lo username non è presente sul server l'allert rimanda alla pagina di login, altrimenti rimane aperta l'acticity per la registrazione
        if (flag) {
            Bundle bundle = new Bundle();
            bundle.putString("welcomeMsg", "Benvenuto " + editUser.getText().toString());
            Intent openHome = new Intent(LoginActivity.this, HomeActivity.class);
            openHome.putExtras(bundle);
            startActivity(openHome);
        } else {
            AlertDialog.Builder miaAlert = new AlertDialog.Builder(context);
            miaAlert.setTitle("Errore Login");
            miaAlert.setMessage("Username o Passoword Errati");
            AlertDialog alert = miaAlert.create();
            alert.show();
        }
    }

    // Mostra un dialog in caso di errore di connessione, se in numero errore è 1 allora l'appviene riavviata
    public void mostraDialog(String err, int numErr) {
        // Se lo username non è presente sul server l'allert rimanda alla pagina di login, altrimenti rimane aperta l'acticity per la registrazione
        final AlertDialog.Builder errorAlert = new AlertDialog.Builder(context);
        errorAlert.setTitle("Errore di Connessione");
        if(numErr == 1) {
            errorAlert.setMessage("Errore Critico: "+err);
            errorAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });
        } else {
            errorAlert.setMessage(err);
            errorAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alert = errorAlert.create();
        //
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();
    }
}