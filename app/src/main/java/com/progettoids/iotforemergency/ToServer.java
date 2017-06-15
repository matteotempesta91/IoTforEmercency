package com.progettoids.iotforemergency;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ToServer {

    private final Handler sender = new Handler();
    private Parametri mParametri;
    private DriverServer mDriverServer;
    private boolean registrato;

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
            sender.postDelayed(sendDatiAmb, mParametri.timerDatiAmb());
        }
    };

    public ToServer(DriverServer ds, Parametri pa) {
        mDriverServer = ds;
        mParametri = pa;
    }

    // Attiva e disattiva invio dati ambientali al server
    public void startAmb(boolean onOff) {
        if (onOff) {
            sender.postDelayed(sendDatiAmb, mParametri.timerDatiAmb());
        } else {
            sender.removeCallbacks(sendDatiAmb);
        }
    }

    // Invio dati ambientali al server
    public void inviaDatiAmb(ArrayList<String[]> elencoBeacon) {

        String urlDA = Parametri.URL_SERVER.concat("/dati");
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
                datiambientaliJson.put("orario",datiambientali[8]);
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
                        DriverServer.errorHandler("Invio Dati Ambientali",error);
                    }
                });

        mDriverServer.addToQueue(request);
    }

    /** Metodo per l'invio dei dati di registrazione verso al server tramite JSON
     * Il context contRegistrazione che viene passato a inviaRegistrazione è il context di RegistrazioneActivity,
     * registrazione è un vettore di stinghe contenente i dati dell'utente per la registrazione
     */
    public void inviaRegistrazione(String[] registrazione, final Context contRegistrazione) {
        final ProgressDialog progDialog = new ProgressDialog(contRegistrazione);    // finestra di caricamente in attesa della risposta del server
        String urlReg = Parametri.URL_SERVER.concat("/registrazione");                   // Aggiunge alla root dell'url l'indirizzo per la richiesta al server
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
                            ((RegistrazioneActivity)contRegistrazione).mostraDialog(registrato,"Username già presente");  // usiamo contRegistrazione (RegistrazioneActivity) e non context che si riferisce a LoginActivity
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
                        ((RegistrazioneActivity)contRegistrazione).mostraDialog(false,"Connessione al server non riuscita");
                        String err = error.getMessage();
                        Log.i("POST Response Error",err+"!");
                    }
                });
        // Aggiunge la richiesta per il server alla queue
        mDriverServer.addToQueue(request);
        // Crea e visualizza la progress dialog che si chiuderà quando verrà ricevuta la risposta dal server
        progDialog.setTitle("Registrazione in corso...");
        progDialog.setMessage("Attendere prego");
        progDialog.show();
    }

    // Invia la posizione dell'utente al server
    public void inviaPos(String id_utente, String id_nodo) {
        Log.i("DriverServer:","inviaPos: Invio in corso........................................................");
        JSONObject json = new JSONObject();
        JSONObject posizioneUtenteJson = new JSONObject();
        String urlPos = Parametri.URL_SERVER.concat("/posizione");
        try{
            posizioneUtenteJson.put("id_utente", id_utente);
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
                        DriverServer.errorHandler("Invio Posizione",error);
                    }
                });
        mDriverServer.addToQueue(request);
    }

}
