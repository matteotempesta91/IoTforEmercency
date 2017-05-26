package com.progettoids.iotforemergency;

import android.app.Activity;
import android.app.AlertDialog;
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

public class DriverServer {

    private static DriverServer mDriverServer;
    private final Handler sender = new Handler();
    private DBManager dbManager;
    private final RequestQueue queue;
    private Context contextLogin;            // Questo campo contiene il primo context: ovvero quello di loginActivity
    private final String url;
    private boolean loginValido, registrato;

    // Runnable per invio dati ambientali periodico
    private Runnable sendDatiAmb = new Runnable() {
        @Override
        public void run() {
            //creazione json per l'invio di dati ambientali
            String[] datiambientali = dbManager.getdatiambientali();
            inviaDatiAmb(datiambientali);
            sender.postDelayed(sendDatiAmb, 60000);
        }
    };

    private DriverServer(Context cont) {
        DBHelper dbHelper = new DBHelper(contextLogin);
        dbManager = new DBManager(dbHelper);
        contextLogin = cont;                 // viene inizializzato con il context di loginActivity
        queue = Volley.newRequestQueue(contextLogin);
        url = "http://www.bandaappignano.altervista.org/Project/web/app_dev.php";   // ROOT della url del server
        loginValido = false;
    }

    // Per accedere all'oggetto questo metodo fornisce il riferimento, in questo modo è creato una sola volta per tutta l'applicazione
    // synchronized permette di gestire l'accesso critico
    public static synchronized  DriverServer getInstance(Context context){
        if(mDriverServer==null) {
            mDriverServer = new DriverServer(context);
        }
        return mDriverServer;
    }

    // Attiva e disattiva invio dati ambientali al server
    public void startAmb(boolean onOff) {
        if (onOff) {
            sender.postDelayed(sendDatiAmb, 60000);
        } else {
          sender.removeCallbacks(sendDatiAmb);
        }
    }

    public  void createJsonPosizione(String id_utente, int[] position){
        JSONObject json = new JSONObject();
        JSONObject posizioneutenteJson = new JSONObject();
        try{
            posizioneutenteJson.put("id_utente", id_utente );
            posizioneutenteJson.put("posizione_x", position[0]);
            posizioneutenteJson.put("posizione_y", position[1]);
            posizioneutenteJson.put("quota",position[2]);
            json.put("posizione_utente",posizioneutenteJson);
            Log.i("KKKKKKKK", "MMMMMMMMM");
        }catch(JSONException e){
            e.printStackTrace();
            Log.i("ERRRRRRRRR", "MMMMMMMMM");
        }

        //Log.i("File Json", json.toString());
        //inviaPos(json,"http://www.bandaappignano.altervista.org/Project/web/app_dev.php/blog");
    }


    public void inviaDatiAmb(String[] datiambientali) {

        String urlDA = url.concat("/blog");
        JSONObject json = new JSONObject();
        JSONObject datiambientaliJson = new JSONObject();
        try{
            datiambientaliJson.put("temperatura", datiambientali[0] );
            datiambientaliJson.put("accelerazione_x", datiambientali[1]);
            datiambientaliJson.put("accelerazione_y", datiambientali[2]);
            datiambientaliJson.put("accelerazione_z",datiambientali[3]);
            datiambientaliJson.put("umidita",datiambientali[4]);
            datiambientaliJson.put("luminosita",datiambientali[5]);
            datiambientaliJson.put("pressione",datiambientali[6]);
            json.put("dati_ambientali",datiambientaliJson);
        }catch(JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, urlDA, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("POST Response",response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err = error.getMessage();
                        Log.i("POST Response Error",err);
                    }
                });
        queue.add(request);
    }

    // Metodo per l'invio dei dati di registrazione verso al server tramite JSON
    // Il context contRegistrazione che viene passato a inviaRegistrazione è il context di RegistrazioneActivity,
    // registrazione è un vettore di stinghe contenente i dati dell'utente per la registrazione

    public void inviaRegistrazione(String[] registrazione, final Context contRegistrazione) {
        final ProgressDialog progDialog = new ProgressDialog(contRegistrazione);    // finestra di caricamente in attesa della risposta del server
        String urlReg = url.concat("/registrazione");                   // Aggiunge alla root dell'url l'indirizzo per la richiesta al server
// ----------------------------------- CREAZIONE JSON -----------------------------------
        JSONObject json = new JSONObject();
        JSONObject registrazioneJson = new JSONObject();
        try{
            registrazioneJson.put("nome", registrazione[0] );
            registrazioneJson.put("cognome", registrazione[1]);
            registrazioneJson.put("cod_fiscale", registrazione[2]);
            registrazioneJson.put("username", registrazione[3]);
            registrazioneJson.put("password",registrazione[4]);

            json.put("registrazione",registrazioneJson);
        }catch(JSONException e){
            e.printStackTrace();
        }
// ----------------------------------------------------------------------------------------
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, urlReg, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Chiude la progress dialog quando riceve il JSONObject dal server in risposta alla registrazione di un nuovo utente
                        progDialog.dismiss();
                        Log.i("POST Response",response.toString());
                        try {
                            registrato = response.getBoolean("check");
                           ((RegistrazioneActivity)contRegistrazione).mostraDialog(registrato);  // usiamo contRegistrazione (RegistrazioneActivity) e non context che si riferisce a LoginActivity
                        } catch (Exception e) {
                            Log.i("POST Response Exception",e.toString()+"!");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Chiude la progress dialog quando il server risponde errore alla richiesta di registrazione di un nuovo utente
                        progDialog.dismiss();
                        String err = error.getMessage();
                        Log.i("POST Response Error",err+"!");
                    }
                });
        // Aggiunge la richiesta per il server alla queue
        queue.add(request);
        // Crea e visualizza la progress dialog che si chiuderà quando verrà ricevuta la risposta dal server
        progDialog.setTitle("Registrazione in corso...");
        progDialog.setMessage("Attendere prego");
        progDialog.show();
    }

    // Verifica che username e password inserite dall'utente siano presenti nel server
    // e chiama mostraDialog() per mostrare all'utente il risultato del login e consentire di aprire l'activity home
    public void verificaLogin(String username, String password) {
        final ProgressDialog progDialog = new ProgressDialog(contextLogin);    // finestra di caricamente in attesa della risposta del server
        String urlLogin =url.concat("/login");
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
                        // Salva il messaggio e la causa dell'errore, se sono null significa che si sta aggiornando il server
                        String msgError = error.getMessage()+" "+error.getCause();
                        if(msgError.equals("null null"))
                        {
                            msgError = "SERVER DOWN";
                        }
                        Log.i("POST Response Error",msgError);
                        // Visualizza anche il codice errore data, soltanto nel caso in cui networkResponse non sia nullo
                        if(error.networkResponse!=null)
                        {
                            Log.i("POST Response Error",String.valueOf(error.networkResponse.statusCode)+" "
                                +error.networkResponse.data+" !");
                        }
                    }
                });
//-------------------------------------------------------------------------------------------------------------
        // Aggiunge la richiesta per il server alla queue
        queue.add(request);
        // Crea e visualizza la progress dialog che si chiuderà quando verrà ricevuta la risposta dal server
        progDialog.setTitle("Login in corso...");
        progDialog.setMessage("Attendere prego");
        progDialog.show();
    }

    public void inviaLoginGuest(String idUtenteGuest) {
        final ProgressDialog progDialog = new ProgressDialog(contextLogin);    // finestra di caricamente in attesa della risposta del server
        String urlReg = url.concat("/loginGuest");                       // Aggiunge alla root dell'url l'indirizzo per la richiesta al server

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
                        String err = error.getMessage();
                        Log.i("DriverServer","inviaLoginGuest Response ERROR: " +err);
                    }
                });
        queue.add(request);
        progDialog.setTitle("Login in corso...");
        progDialog.setMessage("Attendere prego");
        progDialog.show();
    }

    public void inviaPos(String id_utente, int[] position) {
        JSONObject json = new JSONObject();
        JSONObject posizioneutenteJson = new JSONObject();
        String urlPos = url.concat("/blog");
        try{
            posizioneutenteJson.put("id_utente", id_utente );
            posizioneutenteJson.put("posizione_x", position[0]);
            posizioneutenteJson.put("posizione_y", position[1]);
            posizioneutenteJson.put("quota",position[2]);
            json.put("posizione_utente",posizioneutenteJson);
        }catch(JSONException e){
            e.printStackTrace();
            Log.i("DriverServer", "Creazione PosizioneUtenteJson Exception: "+e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("POST Response",response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String err = error.getMessage();
                        Log.i("POST Response Error",err);
                    }
                });
        queue.add(request);
    }


    public void riceviJson(String url) {
        JSONObject json;

        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("GET Server Response",response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("POST Response Error","err");
            }
        });
// Add the request to the RequestQueue.
        queue.add(request);
    }

    //TODO
    // fare update server per registrazione, controllo login di username e password,
    //ricezione cambiamento emergenza, stato nodi

    public void metodoProva(String username, String password) {

        String urlLogin =url.concat("/database");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, urlLogin, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Log.i("PROVA GET", response.getJSONObject(0).toString());
                            ((LoginActivity)contextLogin).mostraDialog(loginValido);  // usiamo contRegistrazione (RegistrazioneActivity) e non context che si riferisce a LoginActivity
                        }
                        catch (Exception e) {
                            Log.i("RESPONSE LOGIN","Exception "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msgError = error.getMessage()+" "+error.getCause();
                        if(msgError.equals("null null"))
                        {
                            msgError = "SERVER DOWN";
                        }
                        Log.i("POST Response Error",msgError);
                        if(error.networkResponse!=null)
                        {
                            Log.i("POST Response Error",String.valueOf(error.networkResponse.statusCode)+" "
                                    +error.networkResponse.data+" !");
                        }
                    }
                });
        queue.add(request);
    }
}