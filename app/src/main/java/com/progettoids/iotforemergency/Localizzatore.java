package com.progettoids.iotforemergency;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class Localizzatore {

    private int x, y, z;
    private BeaconListener bleList;
    private MapHome mHome;
    private Handler finder;
    private DriverServer mDriverServer;
    private Parametri mParametri;
    private Context context;
    private String id_utente;

    public Localizzatore(Context context, BeaconListener ble, MapHome maphome) {
        this.context = context;
        bleList = ble;
        mHome = maphome;
        finder = new Handler();
        mDriverServer = DriverServer.getInstance(context);
        mParametri = Parametri.getInstance();

        // PRENDO L'ID COME VARIABILE GLOBALE
        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        id_utente= reader.getString("id_utente", null);

        // Recupero l'ultima posizione nota se presente
        x = reader.getInt("pos_x", 0);
        y = reader.getInt("pos_y", 0);
        z = reader.getInt("pos_z", 0);
        // Se 0 0 0 => pos sconosciuta
    }

    // Runnable per localizzare l'user, eseguito periodicamente fino a stop
    private final Runnable findMe = new Runnable() {
        @Override
        public void run() {
            Log.i("Localizzatore", "Inizio Ricerca pos");
            String macAdrs = bleList.closestBle();
            if (!macAdrs.equals("NN")) {
                int[] pos = DBManager.getPosition(macAdrs);
                if (pos[0] != x || pos[1] != y || z != pos[2] || mHome.posIsZero()) {
                    x = pos[0];
                    y = pos[1];
                    z = pos[2];
                    mHome.disegnaPosizione(x, y, z);
                    mHome.setBitmap();

                    // creazione file json per l'invio della posizione al server
                    // DriverServer driverServer=new DriverServer(context);
                    // mDriverServer.inviaPos(id_utente, pos);
                    mDriverServer.mToServer.inviaPos(id_utente, DBManager.getNodo(macAdrs));
                }
            }
            finder.postDelayed(findMe, mParametri.timerPos());
        }
    };

    // Avvia la localizzazione
    public void startFinder() {
        finder.postDelayed(findMe, mParametri.timerPos());
    }

    // Ferma la localizzazione
    public void stopFinder() {
        finder.removeCallbacks(findMe);
        // Quando viene arrestata la localizzazione, salve l'ultima posizione nota
        // questo permette di risparmiare tempo nel visualizzare l'interfaccia mapHome
        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = reader.edit();
        editor.putInt("pos_x", x);
        editor.putInt("pos_y", y);
        editor.putInt("pos_z", z);
        editor.commit();
    }

    // Aggiunge le variabli di posizione nel context
    // per non perderne traccia alla distruzione della GUI
    // Nota: deve essere chiamato fuori dalla mapHome, altrimenti la posizione è resettata ad ogni creazione della GUI
    public static void addPosContext(Context context) {
        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = reader.edit();
        editor.putInt("pos_x", 0);
        editor.putInt("pos_y", 0);
        editor.putInt("pos_z", 0);
        editor.commit();
    }

    // Elimina l'ultima posizione nota e ferma la ricerca
    public void forgetMe() {
        x=0;
        y=0;
        z=0;
        stopFinder();
    }
}