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
    private Context contextLogin;               // Questo campo contiene il primo context: ovvero quello di loginActivity
    private final String url;
    private boolean loginValido, registrato;

    // Runnable per invio dati ambientali periodico
    private Runnable sendDatiAmb = new Runnable() {
        @Override
        public void run() {
            //creazione json ed invio dei dati ambientali di tutti i beacon trovati
            ArrayList<String[]> datiArrayList = DBManager.getdatiambientali();
            // Invia i dati ambientali solo se ci sono
            if(!datiArrayList.isEmpty()) {
                inviaDatiAmb(datiArrayList);
            }
            //ricezioneNotifica();
            sender.postDelayed(sendDatiAmb, 6000);  //ERA 60000, L'HO ABBASSATO PER TESTARLO
        }
    };

    private DriverServer(Context cont) {
        contextLogin = cont;                    // Viene inizializzato con il context di loginActivity
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
            sender.postDelayed(sendDatiAmb, 6000); // ERA 60000 L0HO ABBASSATO PER FARE UNA PROVA
        } else {
          sender.removeCallbacks(sendDatiAmb);
        }
    }

    public void inviaDatiAmb(ArrayList<String[]> elencoBeacon) {

        String urlDA = url.concat("/dati");
        JSONArray elencoB = new JSONArray();
        JSONObject datiambientaliJson = new JSONObject();
       // JSONObject dato = new JSONObject();

        try{
            for (String[] datiambientali : elencoBeacon) {
            datiambientaliJson.put("mac_beacon", datiambientali[0]);
            datiambientaliJson.put("temperatura", datiambientali[1]);
            datiambientaliJson.put("accelerazione_x", datiambientali[2]);
            datiambientaliJson.put("accelerazione_y", datiambientali[3]);
            datiambientaliJson.put("accelerazione_z",datiambientali[4]);
            datiambientaliJson.put("umidita",datiambientali[5]);
            datiambientaliJson.put("luminosita",datiambientali[6]);
            datiambientaliJson.put("pressione",datiambientali[7]);
            //    dato.put("dati_ambientali",datiambientaliJson);
            elencoB.put(datiambientaliJson);
            //    elencoB.put(dato);
                Log.i("DriverServer","InviaDatiAmbientali-JSONArray: "+elencoB.toString());
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.PUT, urlDA, elencoB,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("Driver Server","InviaDatiAmbientali RESPONSE:"+response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorHandler("Invio Dati Ambientali",error);
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
                        errorHandler("Login",error);
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
                        errorHandler("Login Guest",error);
                    }
                });
        queue.add(request);
        progDialog.setTitle("Login in corso...");
        progDialog.setMessage("Attendere prego");
        progDialog.show();
    }

   // public void inviaPos(String id_utente, int[] position) {
   public void inviaPos(String id_utente, String id_nodo) {
        Log.i("DriverServer:","inviaPos: Invio in corso........................................................");
        JSONObject json = new JSONObject();
        JSONObject posizioneUtenteJson = new JSONObject();
        String urlPos = url.concat("/posizione");
        try{
            posizioneUtenteJson.put("id_utente", id_utente );
            posizioneUtenteJson.put("id_nodo",id_nodo);
          json.put("posizione_utente",posizioneUtenteJson);
        }catch(JSONException e){
            e.printStackTrace();
            Log.i("DriverServer", "Creazione PosizioneUtenteJson Exception: "+e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, urlPos, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("POST Response",response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorHandler("Invio Posizione",error);
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

    // Invia la richiesta al server per ricevere i nodi il cui stato è diverso da zero
    public void ricezioneStatoNodi() {
        String urlNotifiche =url.concat("/notifiche");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, urlNotifiche, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Log.i("PROVA GET", response.getJSONObject(0).toString());
                        }
                        catch (Exception e) {
                            Log.i("RESPONSE GET","Exception "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorHandler("Ricezione notifiche nodi",error);
                    }
                });
        queue.add(request);
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