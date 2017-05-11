package com.progettoids.iotforemergency;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matteotempesta on 27/04/17.
 */

public class DriverServer {

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