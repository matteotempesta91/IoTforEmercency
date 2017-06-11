package com.progettoids.iotforemergency;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

public class RegistrazioneActivity extends Activity {

    final Context context = this;
    private Button btnReg;
    private EditText editNome, editCognome, editCF, editUsername, editPassword, editPassword2;
    private DriverServer mdriverServer;
    private String[] datiReg;
    private Boolean registrazioneOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        datiReg = new String[5];
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        btnReg = (Button) findViewById(R.id.buttonReg);
        btnReg.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                controlloReg();
            }
        }) ;
    }

    public void controlloReg() {
        btnReg =                (Button) findViewById(R.id.buttonReg);
        editNome =              (EditText) findViewById(R.id.editNome);
        editCognome =           (EditText) findViewById(R.id.editCognome);
        editCF =                (EditText) findViewById(R.id.editCF);
        editUsername =          (EditText) findViewById(R.id.editUsername);
        editPassword =          (EditText) findViewById(R.id.editPassword);
        editPassword2 =         (EditText) findViewById(R.id.editPassword2);
        TextView txtNome =      (TextView) findViewById(R.id.txtName);
        TextView txtCognome =   (TextView) findViewById(R.id.txtCognome);
        TextView txtCF =        (TextView) findViewById(R.id.txtCodFis);
        TextView txtUsername =  (TextView) findViewById(R.id.txtUsername);
        TextView txtPassword =  (TextView) findViewById(R.id.txtPass1);
        TextView txtPassword2 = (TextView) findViewById(R.id.txtPass2);
        int control =           0;

        if(!Pattern.matches("[a-zA-Z]*", editNome.getText().toString())||editNome.getText().toString().equals("")){
            txtNome.setText("Nome non valido");
            txtNome.setTextColor(Color.parseColor("#db524c"));
        }else{
            datiReg[0] = editNome.getText().toString();
            txtNome.setText("Nome corretto");
            txtNome.setTextColor(Color.parseColor("#73C400"));
            control++;
        }
        if(!Pattern.matches("[a-zA-Z]*", editCognome.getText().toString())||editCognome.getText().toString().equals("")){
            txtCognome.setText("Cognome non valido");
            txtCognome.setTextColor(Color.parseColor("#db524c"));
        }else{
            datiReg[1]=editCognome.getText().toString();
            txtCognome.setText("Cognome Corretto");
            txtCognome.setTextColor(Color.parseColor("#73C400"));
            control++;
        }
        //   if(!Pattern.matches("[a-zA-Z0-9]*", editCF.getText().toString())||editCF.getText().toString().length()!=16){
        if(!Pattern.matches("[a-zA-Z0-9]*", editCF.getText().toString())){
            txtCF.setText("Codice fiscale non valido");
            txtCF.setTextColor(Color.parseColor("#db524c"));
        }else{
            datiReg[2]=editCF.getText().toString();
            txtCF.setText("Codice Fiscale corretto");
            txtCF.setTextColor(Color.parseColor("#73C400"));
            control++;
        }
        if(!Pattern.matches("[a-zA-Z0-9_-]*", editUsername.getText().toString())||editUsername.getText().toString().equals("")){
            txtUsername.setText("Username non valido");
            txtUsername.setTextColor(Color.parseColor("#db524c"));
        } else{
            datiReg[3] = editUsername.getText().toString();
            txtUsername.setText("Username corretto");
            txtUsername.setTextColor(Color.parseColor("#73C400"));
            control++;
        }

        if(!Pattern.matches("[a-zA-Z0-9_-]*", editPassword.getText().toString())||editPassword.getText().toString().equals("")){
            txtPassword.setText("Password non valida");
            txtPassword.setTextColor(Color.parseColor("#db524c"));
        }else{
            datiReg[4] = editPassword.getText().toString();
            txtPassword.setText("Password corretta");
            txtPassword.setTextColor(Color.parseColor("#73C400"));
            control++;
            if(!editPassword.getText().toString().equals(editPassword2.getText().toString())) {
                TextView txt = (TextView) findViewById(R.id.txtPass2);
                txt.setText("Le Password non coincidono");
                txt.setTextColor(Color.parseColor("#db524c"));
            }else {
                txtPassword2.setText("Password corretta");
                txtPassword2.setTextColor(Color.parseColor("#73C400"));
                control++;
            }
        }
        if(control==6){
            // Crea l'istanza di driver server che invia i dati al server e controlla se lo username è già presente nel DB
            mdriverServer=DriverServer.getInstance(context);
            mdriverServer.mToServer.inviaRegistrazione(datiReg, context);
        }
    }

    public void mostraDialog(Boolean flag) {
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(context);
        // Se lo username non è presente sul server l'allert rimanda alla pagina di login, altrimenti rimane aperta l'acticity per la registrazione
        if (flag) {
            miaAlert.setTitle("Registrazione Effettuata con successo!");
            miaAlert.setMessage("Ora sarai rimandato alla pagina di login per accedere all'app");
            miaAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RegistrazioneActivity.this.finish();
                }
            });
        } else {
            miaAlert.setTitle("Errore Registrazione");
            miaAlert.setMessage("Username già presente");
        }
        AlertDialog alert = miaAlert.create();
        alert.show();
    }
}