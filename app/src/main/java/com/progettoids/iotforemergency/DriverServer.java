package com.progettoids.iotforemergency;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import java.util.ArrayList;

public class DriverServer {

    private static DriverServer mDriverServer;
    private final Handler sender = new Handler();
    private final RequestQueue queue;
    private boolean loginValido;
    public ToServer mToServer;
    public FromServer mFromServer;
    // Questo campo contiene il primo context: ovvero quello di loginActivity
    private Context contextLogin;

    private DriverServer(Context cont) {
        contextLogin = cont;                    // Viene inizializzato con il context di loginActivity
        queue = Volley.newRequestQueue(contextLogin);
        loginValido = false;
        mToServer = new ToServer(this);
        mFromServer = new FromServer(this);
    }

    // Per accedere all'oggetto questo metodo fornisce il riferimento, in questo modo è creato una sola volta per tutta l'applicazione
    // synchronized permette di gestire l'accesso critico
    public static synchronized  DriverServer getInstance(Context context){
        if(mDriverServer==null) {
            mDriverServer = new DriverServer(context);
        }
        return mDriverServer;
    }

    // Aggiunge all'unica coda il messaggio da inviare al server
    public synchronized void addToQueue(JsonArrayRequest req) {
        queue.add(req);
    }
    public synchronized void addToQueue(JsonObjectRequest req) {
        queue.add(req);
    }

    // Verifica che username e password inserite dall'utente siano presenti nel server
    // e chiama mostraDialog() per mostrare all'utente il risultato del login e consentire di aprire l'activity home
    public void verificaLogin(String username, String password) {
        final ProgressDialog progDialog = new ProgressDialog(contextLogin);    // finestra di caricamente in attesa della risposta del server
        String urlLogin = Parametri.URL_SERVER.concat("/login");
        JSONObject json = new JSONObject();
        JSONObject loginJson = new JSONObject();
        try{
            loginJson.put("username", username );
            loginJson.put("password", password);
            json.put("login", loginJson);
        }catch(JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, urlLogin, json,
// -------------------------------------------- RESPONSE LISTENER --------------------------------------------
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Chiude la progress dialog quando riceve il JSONObject di risposta per il login dal server
                        progDialog.dismiss();
                        Log.i("Login JSON",response.toString());
                        try {
                            loginValido = response.getBoolean("check");
                            Log.i("RESPONSE LOGIN", Boolean.toString(loginValido));
                            // mostraDialog() crea l'allertDialog per visulizzare il risultato del login e apre l'HomeActivity
                            // usiamo contRegistrazione (RegistrazioneActivity) e non context che si riferisce a LoginActivity
                            ((LoginActivity)contextLogin).mostraDialog(loginValido);
                        }
                        catch (Exception e) {
                            Log.i("RESPONSE LOGIN","Exception "+e.toString());
                        }
                    }
                },
//-------------------------------------------------------------------------------------------------------------
// ---------------------------------------------- ERROR LISTENER ----------------------------------------------
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Chiude la progress dialog quando il server risponde errore alla richiesta di login
                        progDialog.dismiss();
                        DriverServer.errorHandler("Login",error);
                    }
                });
//-------------------------------------------------------------------------------------------------------------
        // Aggiunge la richiesta per il server alla queue
        mDriverServer.addToQueue(request);
        // Crea e visualizza la progress dialog che si chiuderà quando verrà ricevuta la risposta dal server
        progDialog.setTitle("Login in corso...");
        progDialog.setMessage("Attendere prego");
        progDialog.show();
    }

    public void inviaLoginGuest(String idUtenteGuest) {
        final ProgressDialog progDialog = new ProgressDialog(contextLogin);    // finestra di caricamente in attesa della risposta del server
        String urlReg = Parametri.URL_SERVER.concat("/loginGuest");                       // Aggiunge alla root dell'url l'indirizzo per la richiesta al server

// -------------------------------------------- CREAZIONE JSON LOGINGUEST -----------------------------------------
        JSONObject json = new JSONObject();
        JSONObject loginGuestJson = new JSONObject();
        try{
            loginGuestJson.put("username", idUtenteGuest);
            json.put("loginGuest", loginGuestJson);
        }catch(JSONException e){
            e.printStackTrace();
        }
// -----------------------------------------------------------------------------------------------------------------

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, urlReg, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progDialog.dismiss();
                        Log.i("POST Response",response.toString());
                        ((LoginActivity)contextLogin).loginGuest();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progDialog.dismiss();
                        DriverServer.errorHandler("Login Guest",error);
                    }
                });
        addToQueue(request);
        progDialog.setTitle("Login in corso...");
        progDialog.setMessage("Attendere prego");
        progDialog.show();
    }

    // errorHandler gestisce la risposta quando arriva un errore dal server, prende in input il nome del metodo in cui viene chiamato e l'errore del server
    public static void errorHandler(String chiamata, VolleyError error){
        // Salva il messaggio e la causa dell'errore, se sono null significa che il server non è in grado di rispondere perchè lo si sta aggiornando
        String msgError = error.getMessage()+" "+error.getCause();
        if(msgError.equals("null null"))
        {
            msgError = "SERVER DOWN";
        }
        Log.i("DriverServer",chiamata+" Error Response: "+msgError);
        // Visualizza anche il codice errore data, soltanto nel caso in cui networkResponse non sia nullo
        if(error.networkResponse!=null)
        {
            Log.i("POST Response Error",String.valueOf(error.networkResponse.statusCode)+" "
                    +error.networkResponse.data+" !");
        }
    }

    //TODO
    // fare update server per registrazione, controllo login di username e password,
    //ricezione cambiamento emergenza, stato nodi
}