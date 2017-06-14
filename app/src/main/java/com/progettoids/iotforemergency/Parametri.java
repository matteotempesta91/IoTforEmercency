package com.progettoids.iotforemergency;

class Parametri {
    private static Parametri mParametri;

    // Tiene traccia dello stato di emergenza
    private int StatoEmergenza = 0;

    // Url del server
    public static final String URL_SERVER = "http://www.bandaappignano.altervista.org/Project/web/app_dev.php";

    // Timers per le operazioni di aggiornamento di stato, stato nodi, scansione bluetooth,
    // invio dati ambientali rilevati e posizione.
    public final int T_NOTIFICHE;
    public final int T_STATO_NODI;
    private final int T_SCAN;
    private final int T_SCAN_EMERGENZA;
    public final int T_SCAN_PERIOD;
    private final int T_DATIAMB;
    private final int T_DATIAMB_EMERGENZA;
    private final int T_POSIZIONE;
    private final int T_POSIZIONE_EMERGENZA;

    // Numero massimo di tentativi di lettura dati ambientali sul beacon.
    // Nota: ogni tentativo richiede 2,5 secondi
    public final int MAX_TRY_BEACON;

    // Filtro dispositivi ble,
    // specificare il nome comune ai beacon a cui ci si vuole collegare
    public final String FILTRO_BLE_DEVICE;

    /**
     * Istanzia l'oggetto solo la prima volta
     * @return l'oggetto stesso
     */
    public static synchronized Parametri getInstance() {
        if (mParametri == null) {
            mParametri = new Parametri();
        }
        return mParametri;
    }

    /**
     * Costruttore responsabile del riempimento delle costanti.
     */
    private Parametri() {
        Object[] param = DBManager.loadParametri();
/*
        // valori su app
        T_NOTIFICHE = 6000;
        T_STATO_NODI = 6000;
        T_SCAN = 30000;
        T_SCAN_EMERGENZA = 10000;
        T_SCAN_PERIOD = 2000;
        T_DATIAMB = 65000;
        T_DATIAMB_EMERGENZA = 15000;
        T_POSIZIONE = 32000;
        T_POSIZIONE_EMERGENZA = 12000;
        MAX_TRY_BEACON = 5;
        FILTRO_BLE_DEVICE = "CC2650 SensorTag";
*/

        T_NOTIFICHE = (int) param[0];
        T_STATO_NODI = (int) param[1];
        T_SCAN = (int) param[2];
        T_SCAN_EMERGENZA = (int) param[3];
        T_SCAN_PERIOD = (int) param[4];
        T_DATIAMB = (int) param[5];
        T_DATIAMB_EMERGENZA = (int) param[6];
        T_POSIZIONE = (int) param[7];
        T_POSIZIONE_EMERGENZA = (int) param[8];
        MAX_TRY_BEACON = (int) param[9];
        FILTRO_BLE_DEVICE = (String) param[10];

    }

    /**
     * Imposta lo stato di emergenza
     * @param emergenza intero fra 0 e 3 (in questo caso si distingue solo != 0)
     */
    public void setEmergenza(int emergenza) {
        StatoEmergenza = emergenza;
    }

    /**
     * Restituisce il timer in base allo stato di emergenza
     * @return timer appropriato
     */
    public int timerScan() {
        int timer = T_SCAN;
        if (StatoEmergenza!=0) {
            timer = T_SCAN_EMERGENZA;
        }
        return timer;
    }

    /**
     * Restituisce il timer in base allo stato di emergenza
     * @return timer appropriato
     */
    public int timerDatiAmb() {
        int timer = T_DATIAMB;
        if (StatoEmergenza!=0) {
            timer = T_DATIAMB_EMERGENZA;
        }
        return timer;
    }

    /**
     * Restituisce il timer in base allo stato di emergenza
     * @return timer appropriato
     */
    public int timerPos() {
        int timer = T_POSIZIONE;
        if (StatoEmergenza!=0) {
            timer = T_POSIZIONE_EMERGENZA;
        }
        return timer;
    }
}