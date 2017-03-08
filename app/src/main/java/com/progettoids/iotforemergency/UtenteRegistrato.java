package com.progettoids.iotforemergency;

/**
 * Created by marco on 17/01/2017.
 */

public class UtenteRegistrato extends Utente {
    private String nome;
    private String cognome;
    private String codFiscale;
    private String username;
    private String password;

    public UtenteRegistrato(String nome, String cognome, String codFiscale, String username, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.codFiscale = codFiscale;
        this.username = username;
        this.password = password;
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
