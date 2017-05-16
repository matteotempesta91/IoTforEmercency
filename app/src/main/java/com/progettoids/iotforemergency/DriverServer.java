package com.progettoids.iotforemergency;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.*;

/**
 * Created by matteotempesta on 27/04/17.
 */

public class DriverServer {

    private final Handler sender = new Handler();
    private DBManager dbManager;

    public DriverServer(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbManager = new DBManager(dbHelper);
    }

    // Runnable per invio dati ambientali periodico
    private Runnable sendDatiAmb = new Runnable() {
        @Override
        public void run() {
            //creazione json per l'invio di dati ambientali
            String[] datiambientali = dbManager.getdatiambientali();
            createjsonDatiAmbientali(datiambientali);
            sender.postDelayed(sendDatiAmb, 60000);
        }
    };

    // Attiva e disattiva invio dati ambientali al server
    public void startAmb(boolean onOff) {
        if (onOff) {
            sender.postDelayed(sendDatiAmb, 60000);
        } else {
          sender.removeCallbacks(sendDatiAmb);
        }
    }

    public void sendPosition(String id_utente,int[] position){
    }


    public void createjsonposizione(String id_utente, int[] position){

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

        Log.i("File Json", json.toString());

    }


    public void createjsonDatiAmbientali(String[] datiambientali) {


        JSONObject json = new JSONObject();
        JSONObject datiambientaliJson = new JSONObject();
        try{
            datiambientaliJson.put("temperatura", datiambientali[0] );
            datiambientaliJson.put("accelerazione_x", datiambientali[1]);
            datiambientaliJson.put("accelerazione_y", datiambientali[2]);
            datiambientaliJson.put("accelerazione_z",datiambientali[3]);
            datiambientaliJson.put("umidità",datiambientali[4]);
            datiambientaliJson.put("luminosità",datiambientali[5]);
            datiambientaliJson.put("pressione",datiambientali[6]);
            json.put("dati_ambientali",datiambientaliJson);
            Log.i("KKKKKKKK", "MMMMMMMMM");
        }catch(JSONException e){
            e.printStackTrace();
            Log.i("ERRRRRRRRR", "MMMMMMMMM");
        }

        Log.i("File Json Dati Amb", json.toString());


    }

}