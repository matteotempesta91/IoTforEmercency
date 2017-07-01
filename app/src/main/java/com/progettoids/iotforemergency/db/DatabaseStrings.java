package com.progettoids.iotforemergency.db;

public class DatabaseStrings {
    /*
    CAMPI NODO
     */

    public static final String TBL_NAME_NODO = "Nodo";
    public static final String FIELD_NODO_CODICE = "codice";
    public static final String FIELD_NODO_POSIZIONE_X = "posizione_x";
    public static final String FIELD_NODO_POSIZIONE_Y = "posizione_y";
    public static final String FIELD_NODO_POSIZIONE_Z = "posizione_z";
    public static final String FIELD_NODO_STATO = "stato";


    /*
    CAMPI BEACON
     */

    public static final String TBL_NAME_BEACON = "Beacon";
    public static final String FIELD_BEACON_MAC = "mac_beacon";
    public static final String FIELD_BEACON_CODICE_NODO= "codice_nodo_beacon";
    public static final String FIELD_BEACON_TEMPERATURA = "temperatura";
    public static final String FIELD_BEACON_ACCELERAZIONEX = "accelerazionex";
    public static final String FIELD_BEACON_ACCELERAZIONEY = "accelerazioney";
    public static final String FIELD_BEACON_ACCELERAZIONEZ = "accelerazionez";
    public static final String FIELD_BEACON_UMIDITA = "umidita";
    public static final String FIELD_BEACON_LUMINOSITA = "luminosita";
    public static final String FIELD_BEACON_PRESSIONE = "pressione";
    public static final String FIELD_BEACON_ORARIO = "orario";

    /*
    CAMPI NOTIFICHE
     */
    public static final String TBL_NAME_NOTIFICA = "Notifica";
    public static final String FIELD_NOTIFICA_NOME = "nome";
    public static final String FIELD_NOTIFICA_DATA = "data";

    // CAMPI PARAMETRI
    public static final String TBL_NAME_PARAMETRI = "Parametri";
    public static final String FIELD_ID_PARAM = "id_parametri";
    public static final String FIELD_T_NOTIFICHE = "t_notifiche";
    public static final String FIELD_T_STATO_NODI = "t_nodi";
    public static final String FIELD_T_SCAN = "t_scan";
    public static final String FIELD_T_SCAN_EMERGENZA = "t_scan_emergenza";
    public static final String FIELD_T_SCAN_PERIOD = "t_scan_period";
    public static final String FIELD_T_DATIAMB = "t_datiamb";
    public static final String FIELD_T_DATIAMB_EMERGENZA = "t_datiamb_emergenza";
    public static final String FIELD_T_POSIZIONE = "t_posizione";
    public static final String FIELD_T_POSIZIONE_EMERGENZA = "t_posizione_emergenza";
    public static final String FIELD_MAX_TRY_BEACON = "max_try_beacon";
    public static final String FIELD_FILTRO_BLE = "filtro_ble";

    public static final String[] nome_notifica = {
            "nodi",
            "beacon",
            "parametri",
            "emergenza"
    };

}

