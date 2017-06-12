package com.progettoids.iotforemergency;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Estende la classe SQLiteOpenHelper, che fornisce i metodi per dialogare con il DB locale
 */

public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper mDBHelper;
    public static final String DBNAME = "DB_LOCALE_15";

    private DBHelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    /**
     * Invocato alla prima creazione del DB locale, crea le tabelle necessarie
     * @param sqLiteDatabase : Passato dal costruttore
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.i("DBHelper","INIZIO CREAZIONE DB");

        String q="CREATE TABLE " + DatabaseStrings.TBL_NAME_NODO +
                "("+DatabaseStrings.FIELD_NODO_CODICE + " TEXT PRIMARY KEY NOT NULL," +
                DatabaseStrings.FIELD_NODO_POSIZIONE_X + " INTEGER," +
                DatabaseStrings.FIELD_NODO_POSIZIONE_Y + " INTEGER," +
                DatabaseStrings.FIELD_NODO_POSIZIONE_Z + " INTEGER," +
                DatabaseStrings.FIELD_NODO_STATO + " INTEGER);";

        String q2=" CREATE TABLE " + DatabaseStrings.TBL_NAME_BEACON +
                "("+DatabaseStrings.FIELD_BEACON_MAC + " TEXT PRIMARY KEY NOT NULL," +
                DatabaseStrings.FIELD_BEACON_CODICE_NODO + " TEXT," +
                DatabaseStrings.FIELD_BEACON_TEMPERATURA + " REAL," +
                DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX + " REAL," +
                DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY + " REAL," +
                DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ + " REAL," +
                DatabaseStrings.FIELD_BEACON_UMIDITA + " REAL," +
                DatabaseStrings.FIELD_BEACON_LUMINOSITA + " REAL," +
                DatabaseStrings.FIELD_BEACON_PRESSIONE + " REAL," +
                DatabaseStrings.FIELD_BEACON_ORARIO + " INTEGER);";     // E' integer perchè stiamo usando timestamp

        // CREATE della tabella notifiche, identica a quella presente sul server
        String q3=" CREATE TABLE " + DatabaseStrings.TBL_NAME_NOTIFICA +
                "("+DatabaseStrings.FIELD_NOTIFICA_NOME + " TEXT PRIMARY KEY NOT NULL," +
                DatabaseStrings.FIELD_NOTIFICA_DATA + " INTEGER);";     // E' integer perchè stiamo usando timestamp

        // Tabella per i parametri di configurazione
        String q4=" CREATE TABLE " + DatabaseStrings.TBL_NAME_PARAMETRI +
                "("+DatabaseStrings.FIELD_ID_PARAM + " INTEGER PRIMARY KEY NOT NULL," +
                DatabaseStrings.FIELD_T_NOTIFICHE + " INTEGER," +
                DatabaseStrings.FIELD_T_STATO_NODI + " INTEGER," +
                DatabaseStrings.FIELD_T_SCAN + " INTEGER," +
                DatabaseStrings.FIELD_T_SCAN_EMERGENZA + " INTEGER," +
                DatabaseStrings.FIELD_T_SCAN_PERIOD + " INTEGER," +
                DatabaseStrings.FIELD_T_DATIAMB + " INTEGER," +
                DatabaseStrings.FIELD_T_DATIAMB_EMERGENZA + " INTEGER," +
                DatabaseStrings.FIELD_T_POSIZIONE + " INTEGER," +
                DatabaseStrings.FIELD_T_POSIZIONE_EMERGENZA + " INTEGER," +
                DatabaseStrings.FIELD_MAX_TRY_BEACON + " INTEGER," +
                DatabaseStrings.FIELD_FILTRO_BLE + " TEXT);";

        Log.i("Creazione tabella nodo:",q);
        sqLiteDatabase.execSQL(q);

        Log.i("Creazione tab beacon:",q2);
        sqLiteDatabase.execSQL(q2);

        Log.i("Creazione tab notif:",q3);
        sqLiteDatabase.execSQL(q3);

        Log.i("Creazione tab param:",q4);
        sqLiteDatabase.execSQL(q4);

        Log.i("DBHelper","DATABASE CREATO");

    }

    /**
     * Questa classe può avere un'unica istanza, invocare questo metodo per accedere all'oggetto.
     * @param context : necessario per accedere al DB
     *                NOTA: è necessario solo alla prima invocazione,
     *                se si è sicuri che già esiste è possibile passare NULL
     * @return : l'istanza dell'oggetto stesso
     */
    public static synchronized  DBHelper getInstance(Context context){
        if(mDBHelper==null) {
            mDBHelper = new DBHelper(context);
        }
        return mDBHelper;
    }

    /**
     *  ??
     * @param sqLiteDatabase
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
