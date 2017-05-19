package com.progettoids.iotforemergency;

import java.sql.Driver;

/**
 * Created by marco on 17/01/2017.
 */

public class UtenteRegistrato {
    private String nome;
    private String cognome;
    private String codFiscale;
    private String username;
    private String password;
    DriverServer driverServer;

    public UtenteRegistrato(String nome, String cognome, String codFiscale, String username, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.codFiscale = codFiscale;
        this.username = username;
        this.password = password;
       // driverServer = new DriverServer(context);
    }

    public void setNome(String nomeUtente) {
        nome = nomeUtente;
    }

    public void setCognome(String cognomeUtente) { cognome = cognomeUtente;
    }

    public void setCodFiscale(String codFisUtente) {
        codFiscale = codFisUtente;
    }

    public void setUsername(String user) {
        username = user;
    }

    public void setPassword(String id) {
        password = id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getCodFiscale() {
        return codFiscale;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
