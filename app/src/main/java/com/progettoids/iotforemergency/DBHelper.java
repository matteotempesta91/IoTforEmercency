package com.progettoids.iotforemergency;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by matteotempesta on 03/03/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "DB_LOCALE_15";

    public DBHelper(Context context) {

        super(context, DBNAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.i("DBHelper","INIZIO CREAZIONE DB");

        String q="CREATE TABLE " + DatabaseStrings.TBL_NAME_NODO +
                "("+DatabaseStrings.FIELD_NODO_CODICE + " TEXT PRIMARY KEY NOT NULL," +
                DatabaseStrings.FIELD_NODO_POSIZIONE_X + " TEXT," +
                DatabaseStrings.FIELD_NODO_POSIZIONE_Y + " TEXT," +
                DatabaseStrings.FIELD_NODO_QUOTA + " TEXT," +
                DatabaseStrings.FIELD_NODO_STATO + " TEXT," +
                DatabaseStrings.FIELD_NODO_ORARIO_ULTIMA_RICEZIONE + " TEXT)";

        String q2=" CREATE TABLE " +
                DatabaseStrings.TBL_NAME_BEACON +
                "("+DatabaseStrings.FIELD_BEACON_MAC + " TEXT PRIMARY KEY NOT NULL," +
                DatabaseStrings.FIELD_BEACON_CODICE_NODO + " TEXT," +
                DatabaseStrings.FIELD_BEACON_TEMPERATURA + " TEXT," +
                DatabaseStrings.FIELD_BEACON_ACCELERAZIONE + " TEXT," +
                DatabaseStrings.FIELD_BEACON_UMIDITA + " TEXT," +
                DatabaseStrings.FIELD_BEACON_LUMINOSITA + " TEXT," +
                DatabaseStrings.FIELD_BEACON_PRESSIONE + " TEXT);";

        Log.i("Creazione tabella nodo:",q);
        sqLiteDatabase.execSQL(q);


        Log.i("Creazione tabella beacon:",q2);
        sqLiteDatabase.execSQL(q2);

        Log.i("DBHelper","DATABASE CREATO");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
