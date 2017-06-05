package com.progettoids.iotforemergency;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class UpdateFromServer {
    private final Handler sender = new Handler();
    private final RequestQueue queue;
    private Context contextLogin;               // Questo campo contiene il primo context: ovvero quello di loginActivity
    private final String url;

    private Runnable sendNotifiche = new Runnable() {
        @Override
        public void run() {

            sender.postDelayed(sendNotifiche, 6000);  //ERA 60000, L'HO ABBASSATO PER TESTARLO
        }
    };

    public UpdateFromServer(Context cont) {
        contextLogin = cont;                    // Viene inizializzato con il context di loginActivity
        queue = Volley.newRequestQueue(contextLogin);
        url = "http://www.bandaappignano.altervista.org/Project/web/app_dev.php";   // ROOT della url del server
    }

    public void riceviNotifica() {
        String urlLogin =url.concat("/aggiornamento");
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

}
