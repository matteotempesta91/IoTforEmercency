package com.progettoids.iotforemergency;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class DriverServer {

    private static DriverServer mDriverServer;
    private final Handler sender = new Handler();
    private DBManager dbManager;
    private final RequestQueue queue;
    private Context context;
    private final String url;
    private boolean loginValido, registrato, flagResponse;
    ProgressDialog progressDialog;

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
        DBHelper dbHelper = new DBHelper(context);
        dbManager = new DBManager(dbHelper);
        context = cont;
        queue = Volley.newRequestQueue(context);
        url = "http://www.bandaappignano.altervista.org/Project/web/app_dev.php";
        loginValido = false;
        flagResponse = false;
       // registrato = false;
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
            Log.i("KKKKKKKK", "MMMMMMMMM");
        }catch(JSONException e){
            e.printStackTrace();
            Log.i("ERRRRRRRRR", "MMMMMMMMM");
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

    public void inviaRegistrazione(String[] registrazione, RegistrazioneActivity regAct) {
        String urlReg = url.concat("/registrazione");
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

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, urlReg, json,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        flagResponse = true;
                        //progressDialog.dismiss();
                        Log.i("POST Response",response.toString());
                        try {
                            registrato = response.getBoolean("check");
                            /*
                            Log.i("Registrato",Boolean.toString(registrato));
                           AlertDialog.Builder miaAlert = new AlertDialog.Builder(context);
                            // Se lo username è libero l'allert rimanda alla pagina di login, altrimenti rimane aperta l'acticity per la registrazione
                           if(registrato) {
                               miaAlert.setTitle("Registrazione Effettuata con successo!");
                                miaAlert.setMessage("Ora sarai rimandato alla pagina di login per accedere all'app");
                                miaAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Activity)context).finish();
                                    }
                                });
                            }else {
                                miaAlert.setTitle("Registrazione errata!");
                                miaAlert.setMessage("Username già presente");
                            }
                            AlertDialog alert = miaAlert.create();
                            alert.show();
                            int i =0;
           */
                            if(registrato) {
                                //((Activity)context).finish();
                            }
                        } catch (Exception e) {

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        flagResponse = true;
                        String err = error.getMessage();
                      //  progressDialog.dismiss();
                        Log.i("POST Response Error",err);
                    }
                });
        queue.add(request);

    //    progressDialog = new ProgressDialog(context);
      //  progressDialog.setMessage("Verifica delle credenziali");
       // progressDialog.show();
    }

    public void verificaLogin(String username, String password) {
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
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Login JSON",response.toString());
                        try {
                            loginValido = response.getBoolean("flag");
                            Log.i("RESPONSE LOGIN", Boolean.toString(loginValido));
                        }
                        catch (Exception e) {
                            Log.i("RESPONE LOGIN","Exception");
                        }
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

    public boolean getRegistrato() {
        return registrato;
    }

    public  Boolean getLoginValido() {
        return loginValido;
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

    // fare update server per registrazione, controllo login di username e password,
    //ricezione cambiamento emergenza, stato nodi

    public static void inviaUtenteReg() {

    }

}