package com.progettoids.iotforemergency;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.CharBuffer;

/**
 * Created by marco on 17/01/2017.
 */

public class Login {
    private String user;
    private String pwd;

    
    // salva i dati nella cartella cache locale
    public void saveLogin(File memo, String user, String pwd) throws Exception {
        try {
            FileWriter scrivi = new FileWriter(memo);
            scrivi.write(user + "\n" + pwd + "\n");
            scrivi.close();
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
