package com.progettoids.iotforemergency;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateFromServer {
    private final Handler sender = new Handler();
    private final RequestQueue queue;
    private Context contextLogin;               // Questo campo contiene il context di loginActivity
    private final String url;
    private int dataBeacon,dataNodi,dataParam;
    DateFormat dataFormat;
   // Date dataBeacon,dataNodi,dataParam;
    String emergenza;

    private Runnable sendNotifiche = new Runnable() {
        @Override
        public void run() {
            ricezioneNotifica();
            sender.postDelayed(sendNotifiche, 6000);  //ERA 60000, L'HO ABBASSATO PER TESTARLO
        }
    };

    public UpdateFromServer(Context cont) {
        contextLogin = cont;                    // Viene inizializzato con il context di loginActivity
        queue = Volley.newRequestQueue(contextLogin);
        url = "http://www.bandaappignano.altervista.org/Project/web/app_dev.php";   // ROOT della url del server
        startUpdate(true);
    }

    // Attiva e disattiva invio dati ambientali al server
    public void startUpdate(boolean onOff) {
        if (onOff) {
            sender.postDelayed(sendNotifiche, 6000); // ERA 60000 L0HO ABBASSATO PER FARE UNA PROVA
        } else {
            sender.removeCallbacks(sendNotifiche);
        }
    }

    public void riceviNotifica() {
        String urlLogin = url.concat("/aggiornamento");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, urlLogin, null,
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

    // Invia la richiesta al server per ricevere la notifiche di emergenza e dello stato delle tabelle
    public void ricezioneNotifica() {
        String urlNotifiche = url.concat("/notifiche");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, urlNotifiche, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.i("DriverServer", "Ricezione Notifica Emergenza RESPONSE: "+response.toString());
                            emergenza = response.getString("nome_emergenza");
                            dataBeacon = response.getInt("tabella_beacon");
                            dataNodi = response.getInt("tabella_nodo");
                            dataParam = response.getInt("tabella_parametri");

                            Log.i("DriverServer", "DATA Beacon: "+dataBeacon);
                            Log.i("DriverServer", "DATA NODI: "+dataNodi);
                            Log.i("DriverServer", "DATA Parametri: "+dataParam);
                /*            dataFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");   // Definisce il formato della data
                            // Salva il tipo ti emergenza e le date in cui sono state cambiate la tebelle nodo, beacon e parametri sul server
                            dataBeacon = dataFormat.parse(response.getString("tabella_beacon"));
                            dataNodi = dataFormat.parse(response.getString("tabella_nodo"));
                            dataParam = dataFormat.parse(response.getString("tabella_parametri"));
                            Log.i("DriverServer", "DATA NODI: "+dataFormat.format(dataNodi));
                            Log.i("DriverServer", "DATA Beacon: "+dataFormat.format(dataBeacon));
                            Log.i("DriverServer", "DATA Parametri: "+dataFormat.format(dataParam));
                            // Recupera le date salvate sul DB locale dell'ultimo aggiornamento ricevuto,
                            // se le date ricevute dal server sono più recenti invia la richiesta al server per scaricare le tabelle
                            if(dataBeacon.after(DBManager.getDataNotifica("beacon"))){

                            }
                            if(dataNodi.after(DBManager.getDataNotifica("nodi"))){

                            }
                            if(dataParam.after(DBManager.getDataNotifica("parametri"))){

                            }
               */
                        }
                        catch (Exception e) {
                            Log.i("DriverServer","Ricezione Notifica Emergenza EXCEPTION "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DriverServer.errorHandler("Ricezione notifiche nodi",error);
                    }
                });
        queue.add(request);
    }

}
