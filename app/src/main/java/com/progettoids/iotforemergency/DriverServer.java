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
}