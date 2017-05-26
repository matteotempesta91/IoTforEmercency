package com.progettoids.iotforemergency;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class Localizzatore {

    private int x, y, z;
    private BeaconListener bleList;
    private MapHome mHome;
    private Context context;
    private Handler finder;
    private DriverServer mDriverServer;

    public Localizzatore(Context context, BeaconListener ble, MapHome maphome) {
        this.context = context;
        bleList = ble;
        mHome = maphome;
        finder = new Handler();
        mDriverServer = DriverServer.getInstance(context);

// Se 0 0 0 => pos sconosciuta
        x=0;
        y=0;
        z=0;
    }

    // Runnable per localizzare l'user, eseguito periodicamente fino a stop
    private final Runnable findMe = new Runnable() {
        @Override
        public void run() {
            Log.i("finder", "Ricerca pos");
            String macAdrs = bleList.closestBle();
            if (!macAdrs.equals("NN")) {
                int[] pos = DBManager.getPosition(macAdrs);
                if (pos[0] != x && pos[1] != y && z != pos[2]) {
                    x = pos[0];
                    y = pos[1];
                    z = pos[2];
                    mHome.disegnaPosizione(x, y, z);

// PRENDO L'ID COME VARIABILE GLOBALE
                    final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                    final String id_utente= reader.getString("id_utente", null);


// creazione file json per l'invio della posizione al server
              //      DriverServer driverServer=new DriverServer(context);
                    mDriverServer.inviaPos(id_utente, pos);
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