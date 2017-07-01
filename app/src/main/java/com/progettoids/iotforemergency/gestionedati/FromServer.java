package com.progettoids.iotforemergency.gestionedati;

import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.progettoids.iotforemergency.gestionedati.Parametri;
import com.progettoids.iotforemergency.db.DBManager;
import com.progettoids.iotforemergency.gestionedati.DriverServer;
import com.progettoids.iotforemergency.gui.MapHome;

public class FromServer {

    private final Handler sender = new Handler();
    private int dataBeacon, dataNodi, dataParam, oldDataBeacon, oldDataNodi, oldDataParam, emergenza;
    private boolean lockStatoNodi;
    private Parametri mParametri;
    private DriverServer mDriverServer;
    // Permette di aggiornare la mappa, se questa è attiva
    private MapHome mMapHome;

    private Runnable getNotifiche = new Runnable() {
        @Override
        public void run() {
            ricezioneNotifica();
            sender.postDelayed(getNotifiche, mParametri.T_NOTIFICHE);
        }
    };

    private Runnable getStatoNodi =  new Runnable() {
        @Override
        public void run() {
            ricezioneStatoNodi();
            sender.postDelayed(getStatoNodi, mParametri.T_STATO_NODI);
        }
    };

    public FromServer(DriverServer ds, Parametri pa) {
        lockStatoNodi = false;
        mDriverServer = ds;
        mParametri = pa;
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

    // Attiva e disattiva richiesta notifiche al server
    public void startStatoNodi(boolean onOff) {
        if (onOff && !lockStatoNodi) {
            sender.postDelayed(getStatoNodi, mParametri.T_STATO_NODI);
            lockStatoNodi = true;
        } else if (!onOff) {
            sender.removeCallbacks(getStatoNodi);
            lockStatoNodi = false;
        }
    }

    // Invia la richiesta al server per ricevere la notifiche di emergenza e dello stato delle tabelle
    public void ricezioneNotifica() {
        String urlNotifiche = Parametri.URL_SERVER.concat("/database/tabella_notifica");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, urlNotifiche, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // Legge le date dei parametri memorizzate nel database
                        oldDataParam = DBManager.getDataNotifica("parametri");
                        oldDataNodi = DBManager.getDataNotifica("nodi");
                        oldDataBeacon = DBManager.getDataNotifica("beacon");
                  //      Log.i("FromServer", "DATA Beacon DB: "+oldDataBeacon);
                  //      Log.i("FromServer", "DATA Nodi DB: "+oldDataNodi);
                  //      Log.i("FromServer", "DATA Parametri DB: "+oldDataParam);
                        try {
                            Log.i(this.toString(), "Ricezione Notifica Emergenza RESPONSE: "+response.toString());
                            emergenza = response.getInt("nome_emergenza");
                            dataBeacon = response.getInt("tabella_beacon");
                            dataNodi = response.getInt("tabella_nodo");
                            dataParam = response.getInt("tabella_parametri");
                  //          Log.i(this.toString(), "DATA Beacon Server: "+dataBeacon);
                  //          Log.i(this.toString(), "DATA Nodi Server: "+dataNodi);
                  //          Log.i(this.toString(), "DATA Parametri Server: "+dataParam);

                            if (emergenza == 0) {
                                startStatoNodi(false);
                            } else {
                                startStatoNodi(true);
                            }
                            mParametri.setEmergenza(emergenza);
                            DBManager.updateNotifiche("emergenza",emergenza);
                            if (mMapHome != null) {
                                mMapHome.disegnaEmergenza(emergenza);
                            }

                            // Confronta le date nel database con quelle del server,
                            // se il server ha delle notifica con una data più recente aggiorna invia la get per ricevere l'aggiornamento
                            // e aggiorna la data nel DB locale
                            if(dataBeacon > oldDataBeacon){
                                Log.i(this.toString(), "Trovato Aggiornamento Beacon");
                                ricezioneBeacon();
                                DBManager.updateNotifiche("beacon",dataBeacon);

                            }
                            if(dataNodi > oldDataNodi){
                                Log.i(this.toString(), "Trovato Aggiornamento Nodi");
                                ricezioneNodi();
                                DBManager.updateNotifiche("nodi",dataNodi);
                            }
                            if(dataParam > oldDataParam){
                                Log.i(this.toString(), "Trovato Aggiornamento Parametri");
                                ricezioneParametri();
                                DBManager.updateNotifiche("parametri",dataParam);
                            }
                        }
                        catch (Exception e) {
                            Log.i("FromServer","Ricezione Notifica Emergenza EXCEPTION "+e.toString());
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
        String urlParametri = Parametri.URL_SERVER.concat("/database/tabella_parametri");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, urlParametri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("FromServer","ricezioneParametri: "+response.toString());
                        Log.i("FromServer","ricezioneParametri: Aggiornamento Parametri");
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
                            Log.i("FromServer","Ricezione Parametri EXCEPTION "+e.toString());
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

    // Invia la get al server per la ricezione dei nodi
    public void ricezioneNodi() {
        String urlNodi = Parametri.URL_SERVER.concat("/database/tabella_nodo");
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

    // Invia la get al server per la ricezione dei Beacon
    public void ricezioneBeacon() {
        String urlBeacon = Parametri.URL_SERVER.concat("/database/tabella_beacon");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, urlBeacon, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(this.toString(),"ricezioneBeacon: Aggiornamento Nodi");
                        // Cancella i valori memorizzati prececentemente sulla tabella nodi
                        DBManager.deleteBeacon();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject element = response.getJSONObject(i);
                                String beaconID = element.getString("mac");
                                String nodoID = element.getString("codice");
                                DBManager.saveBeacon(beaconID, nodoID);
                            }
                            // aggiungere alert per riavviare l'app
                        }
                        catch (Exception e) {
                            Log.i(this.toString(),"ricezioneBeacon EXCEPTION: "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DriverServer.errorHandler("Ricezione tabella Beacon",error);
                    }
                });
        mDriverServer.addToQueue(request);
    }

    // Invia la richiesta al server per ricevere i nodi il cui stato è diverso da zero
    public void ricezioneStatoNodi() {
        String urlStatoNodi = Parametri.URL_SERVER.concat("/database/stato_nodi");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, urlStatoNodi, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            DBManager.resetStatoNodi();
                            for (int i=0; i<response.length(); i++) {
                                JSONObject ele = response.getJSONObject(i);
                                String codice = ele.getString("codice");
                                int stato = ele.getInt("stato");
                                DBManager.updateStatoNodo(codice, stato);
                            }
                            if (mMapHome != null) {
                                mMapHome.updateStatoNodi();
                            }
                        }
                        catch (Exception e) {
                            Log.i("Stato nodi","Exception "+e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DriverServer.errorHandler("Stato nodi",error);
                    }
                });
        mDriverServer.addToQueue(request);
    }

    /**
     * Se è attiva la mappa, questa si dichiara per essere aggiornata sullo stato nodi
     * Nota: alla sua distruzione deve invocare questo stesso metodo con input null
     * @param map l'oggetto stesso, null alla sua distruzione
     */
    public void mapHomeAlive(MapHome map) {
        mMapHome = map;
    }
}
