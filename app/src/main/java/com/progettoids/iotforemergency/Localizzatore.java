package com.progettoids.iotforemergency;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class Localizzatore {

    private int x, y, quota;
    private BeaconListener bleList;
    private MapHome mHome;
    private Context context;
    private Handler finder;

    public Localizzatore(Context context, BeaconListener ble, MapHome maphome) {
        this.context = context;
        bleList = ble;
        mHome = maphome;
        finder = new Handler();

// Se 0 0 0 => pos sconosciuta
        x=0;
        y=0;
        quota=0;
    }

    // Runnable per localizzare l'user, eseguito periodicamente fino a stop
    private final Runnable findMe = new Runnable() {
        @Override
        public void run() {
            Log.i("finder", "Ricerca pos");
            String macAdrs = bleList.closestBle();
            if (!macAdrs.equals("NN")) {
                DBHelper mDBhe = new DBHelper(context);
                DBManager mDBman = new DBManager(mDBhe);
                int[] pos = mDBman.getPosition(macAdrs);
                if (pos[0] != x && pos[1] != y && quota != pos[2]) {
                    x = pos[0];
                    y = pos[1];
                    quota = pos[2];
                    mHome.disegnaPosizione(x,y,quota);
// SEND POS TO SERVER //


// PRENDO L'ID COME VARIABILE GLOBALE
                    final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                    final String id_utente= reader.getString("id_utente", null);


// creazione file json per l'invio della posizione al server
                    DriverServer driverServer=new DriverServer();
                    driverServer.createjsonposizione(id_utente,pos);
                }
            }
            finder.postDelayed(findMe, 21000);
        }
    };

    // Avvia la localizzazione
    public void startFinder() {
        finder.postDelayed(findMe, 5000);
    }

    // Ferma la localizzazione
    public void stopFinder() {
        finder.removeCallbacks(findMe);
    }
}