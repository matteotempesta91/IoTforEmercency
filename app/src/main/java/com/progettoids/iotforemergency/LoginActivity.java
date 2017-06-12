package com.progettoids.iotforemergency;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

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

    // Pulsante login
    public void login() {
        btnLogin=(Button)findViewById(R.id.buttonLogin);
        editUser=(EditText)findViewById(R.id.username);
        editPass=(EditText)findViewById(R.id.password);
        ricordami=(CheckBox)findViewById(R.id.Ricordami);

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
                    if(mDriverServer == null)
                    {
                        Log.i("mDriverServer","is NULL");
                    }
                    else
                    {
                        Log.i("mDriverServer","not null");
                    }
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
                } else
                {
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

    // Invocato da driverServer solo se l'utente ospite è registrato sul server con successo
    public void loginGuest() {
                Bundle bundle = new Bundle();
                bundle.putString("welcomeMsg", "Benvenuto Utente Guest");
                Intent openHomeGuest = new Intent(LoginActivity.this, HomeActivity.class);
                openHomeGuest.putExtras(bundle);
                startActivity(openHomeGuest);
                // SALVA L'ID UTENTE GUEST COME VARIABILE GLOBALE
                final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = reader.edit();
                String macAdrress=getMacAddr();
                Log.i("macAddress:",macAdrress);
                editor.putString("id_utente", macAdrress);
                editor.commit();
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
        if(Pattern.matches("[a-zA-Z0-9_-]*", editUser.getText().toString())&&Pattern.matches("[a-zA-Z0-9_-]*", editPass.getText().toString()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // Verifica se l'App è avviata per la prima volta
    public static boolean isFirst(Context context){

        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        final boolean first = reader.getBoolean("is_first", true);
        if(first){
            final SharedPreferences.Editor editor = reader.edit();
            editor.putBoolean("is_first", false);
            editor.commit();
        }
        return first;
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

    // Mostra un dialog in caso di errore di connessione
    public void mostraDialog(String err) {
        // Se lo username non è presente sul server l'allert rimanda alla pagina di login, altrimenti rimane aperta l'acticity per la registrazione
            AlertDialog.Builder errorAlert = new AlertDialog.Builder(context);
            errorAlert.setTitle("Errore di Connessione");
            errorAlert.setMessage(err);
            AlertDialog alert = errorAlert.create();
            alert.show();
    }
}