package com.progettoids.iotforemergency;

import android.os.Handler;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class FromServer {
    private final Handler sender = new Handler();
    private int dataBeacon, dataNodi, dataParam, oldDataBeacon, oldDataNodi, oldDataParam;
    private Parametri mParametri;
    String emergenza;
    private DriverServer mDriverServer;

    private Runnable getNotifiche = new Runnable() {
        @Override
        public void run() {
            ricezioneNotifica();
            sender.postDelayed(getNotifiche, mParametri.T_NOTIFICHE);
        }
    };

    public FromServer(DriverServer ds) {
        mDriverServer = ds;
        mParametri = Parametri.getInstance();
        startUpdate(true);
    }

    // Attiva e disattiva richiesta notifiche al server
    public void startUpdate(boolean onOff) {
        if (onOff) {
            sender.postDelayed(getNotifiche, mParametri.T_NOTIFICHE);
        } else {
            sender.removeCallbacks(getNotifiche);
        }
    }

    // Invia la richiesta al server per ricevere la notifiche di emergenza e dello stato delle tabelle
    public void ricezioneNotifica() {
        String urlNotifiche = Parametri.URL_SERVER.concat("/notifiche");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, urlNotifiche, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // Legge le date dei parametri memorizzate nel database
                        oldDataParam = DBManager.getDataNotifica("parametri");
                        oldDataNodi = DBManager.getDataNotifica("nodi");
                        oldDataBeacon = DBManager.getDataNotifica("beacon");
                        try {
                            Log.i(this.toString(), "Ricezione Notifica Emergenza RESPONSE: "+response.toString());
                            emergenza = response.getString("nome_emergenza");
                            dataBeacon = response.getInt("tabella_beacon");
                            dataNodi = response.getInt("tabella_nodo");
                            dataParam = response.getInt("tabella_parametri");

                            Log.i(this.toString(), "DATA Beacon: "+dataBeacon);
                            Log.i(this.toString(), "DATA NODI: "+dataNodi);
                            Log.i(this.toString(), "DATA Parametri: "+dataParam);

                            if(dataBeacon > oldDataBeacon){
                                Log.i(this.toString(), "Trovato Aggiornamento Beacon");

                            }
                            if(dataNodi > oldDataNodi){
                                Log.i(this.toString(), "Trovato Aggiornamento Nodi");
                                ricezioneNodi();
                            }
                            if(dataParam > oldDataParam){
                                Log.i(this.toString(), "Trovato Aggiornamento Parametri");
                                ricezioneParametri();
                            }
                        }
                        catch (Exception e) {
                            Log.i(this.toString(),"Ricezione Notifica Emergenza EXCEPTION "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DriverServer.errorHandler("Ricezione notifiche",error);
                    }
                });
        mDriverServer.addToQueue(request);
    }

    // Invia la get al server per la ricezione dei parametri
    public void ricezioneParametri() {
        String urlParametri = Parametri.URL_SERVER.concat("/tabella_parametri");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, urlParametri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(this.toString(),"ricezioneParametri: "+response.toString());
                        Log.i(this.toString(),"ricezioneParametri: Aggiornamento Parametri");
                        try {
                            String filtroBeacon = response.getString("filtro_ble");
                            int[] parametri = new int[10];
                            parametri[0] = response.getInt("t_notifiche");
                            parametri[1] = response.getInt("t_nodo");
                            parametri[2] = response.getInt("t_scan");
                            parametri[3] = response.getInt("t_scan_emergenza");
                            parametri[4] = response.getInt("t_scan_period");
                            parametri[5] = response.getInt("t_datiamb");
                            parametri[6] = response.getInt("t_datiamb_emergenza");
                            parametri[7] = response.getInt("t_posizione");
                            parametri[8] = response.getInt("t_posizione_emergenza");
                            parametri[9] = response.getInt("max_try_beacon");
                            DBManager.updateParametri(parametri, filtroBeacon);
                            // aggiungere allert per riavviare l'app
                        }
                        catch (Exception e) {
                            Log.i(this.toString(),"Ricezione Parametri EXCEPTION "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DriverServer.errorHandler("Ricezione Parametri",error);
                    }
                });
        mDriverServer.addToQueue(request);
    }

    // Invia la get al server per la ricezione dei parametri
    public void ricezioneNodi() {
        String urlNodi = Parametri.URL_SERVER.concat("/tabella_nodo");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, urlNodi, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(this.toString(),"ricezioneNodi: Aggiornamento Nodi");
                        // Cancella i valori memorizzati prececentemente sulla tabella nodi
                        DBManager.deleteNodi();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject element = response.getJSONObject(i);
                                String codNodo = element.getString("codice");
                                int posX = element.getInt("posx");
                                int posY = element.getInt("posy");
                                int posZ = element.getInt("posz");
                                DBManager.saveNodo(codNodo,posX,posY,posZ);
                            }
                            // aggiungere allert per riavviare l'app
                        }
                        catch (Exception e) {
                            Log.i(this.toString(),"ricezioneNodi EXCEPTION: "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DriverServer.errorHandler("Ricezione tabella nodo",error);
                    }
                });
        mDriverServer.addToQueue(request);
    }

    // Invia la richiesta al server per ricevere i nodi il cui stato è diverso da zero
    public void ricezioneStatoNodi() {
        String urlNotifiche = Parametri.URL_SERVER.concat("/notifiche");
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
                        DriverServer.errorHandler("Ricezione notifiche nodi",error);
                    }
                });
        mDriverServer.addToQueue(request);
    }

}
