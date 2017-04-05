package com.progettoids.iotforemergency;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.File;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    final Context context = this;
    private Button btnLogin, btnLoginGuest, btnRegistrati;
    private EditText editUser, editPass;
    private CheckBox ricordami;
    private Login log;
    private boolean flag1stLog;
    private static final int NUMERO_NODI=63;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        log = new Login();
        flag1stLog = true;
        login();
        loginGuest();
        registrazione();
        gestioneCreazineDB();
    }

    public void login() {
        btnLogin=(Button)findViewById(R.id.buttonLogin);
        editUser=(EditText)findViewById(R.id.username);
        editPass=(EditText)findViewById(R.id.password);
        ricordami=(CheckBox)findViewById(R.id.Ricordami);

        // carica dati utente salvati se presenti
        File path = context.getCacheDir();
        File memo = new File(path, "memo");
        if (memo.exists()) {
            try {
                String[] userPwd = log.loadLogin(memo);
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
                    Bundle bundle = new Bundle();
                    bundle.putString("welcomeMsg", "Benvenuto "+editUser.getText().toString());
                    Intent openHome = new Intent(LoginActivity.this, HomeActivity.class);
                    openHome.putExtras(bundle);
                    startActivity(openHome);

                    // salva i dati solo se checkbox segnato
                    if (ricordami.isChecked()) {
                        File path = context.getCacheDir();
                        File memo = new File(path, "memo");
                        try {
                            log.saveLogin(memo, editUser.getText().toString(), editUser.getText().toString());
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

    public void loginGuest() {
        btnLoginGuest=(Button)findViewById(R.id.buttonLoginGuest);
        btnLoginGuest.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                Bundle bundle = new Bundle();
                bundle.putString("welcomeMsg", "Benvenuto Utente Guest");
                Intent openHomeGuest = new Intent(LoginActivity.this, HomeActivity.class);
                openHomeGuest.putExtras(bundle);
                startActivity(openHomeGuest);
            }
        });
    }

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

    public void gestioneCreazineDB(){

        DBManager dbManager;

        DBHelper dBhelper = new DBHelper(this);
        dbManager = new DBManager(dBhelper);


        if(isFirst(this)){
            for (int i=0;i<NUMERO_NODI;i++){
                String codice = DatabaseStrings.codice[i];
                String posizione_x = String.valueOf(DatabaseStrings.posizione_x[i]);
                String posizione_y = String.valueOf(DatabaseStrings.posizione_y[i]);
                Log.i("Login:",codice);
                dbManager.save(codice,posizione_x,posizione_y,null,null,null,null,null,null,null);
            }



            dbManager.query();
        }

    }


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


}