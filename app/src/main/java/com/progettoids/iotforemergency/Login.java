package com.progettoids.iotforemergency;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.CharBuffer;

public class Login {
    private String user;
    private String pwd;
    DriverServer driverServer;

    // salva i dati nella cartella cache locale
    public void saveLogin(Context context, File memo, String user, String pwd) throws Exception {
        try {
            FileWriter scrivi = new FileWriter(memo);
            scrivi.write(user + "\n" + pwd + "\n");
            scrivi.close();

            // SALVA COME VARIABILE GLOBALE L'ID UTENTE
            final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = reader.edit();
            editor.putString("id_utente", user);
            editor.commit();
            this.user = user;
            this.pwd = pwd;
        } catch (Exception ex) {
            throw ex;
        }
    }

    // carica i dati dal file memo, questo deve già esistere all'invocazione del metodo
    public String[] loadLogin(File memo) throws Exception {
        String[] userPwd;
        try {
            FileReader leggi = new FileReader(memo);
            CharBuffer buffer = CharBuffer.allocate(30);
            leggi.read(buffer);
            buffer.rewind();
            userPwd = buffer.toString().split("\n");
            leggi.close();
            user = userPwd[0];
            pwd = userPwd[1];
        } catch (Exception ex) {
            throw ex;
        }
        return userPwd;
    }


    public String getUser(){
        return this.user;
    }
}