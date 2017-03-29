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

        String q="CREATE TABLE " + DatabaseStrings.TBL_NAME +
                "("+DatabaseStrings.FIELD_CODICE + " TEXT PRIMARY KEY NOT NULL," +
                DatabaseStrings.FIELD_ID_BEACON + " TEXT," +
                DatabaseStrings.FIELD_POSIZIONE_X + " TEXT," +
                DatabaseStrings.FIELD_POSIZIONE_Y + " TEXT," +
                DatabaseStrings.FIELD_QUOTA + " TEXT," +
                DatabaseStrings.FIELD_STATO + " TEXT," +
                DatabaseStrings.FIELD_TEMPERATURA + " TEXT," +
                DatabaseStrings.FIELD_UMIDITA + " TEXT," +
                DatabaseStrings.FIELD_PRESSIONE + " TEXT," +
                DatabaseStrings.FIELD_LUMINOSITA + " TEXT," +
                DatabaseStrings.FIELD_ACCELERAZIONE + " TEXT," + //Altezza
                DatabaseStrings.FIELD_NUMERO_PERSONE + " TEXT," +//in più
                DatabaseStrings.FIELD_ORARIO_ULTIMA_RICEZIONE + " TEXT)";

        Log.i("DBHelper",q);
        sqLiteDatabase.execSQL(q);
        Log.i("DBHelper","DATABASE CREATO");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
