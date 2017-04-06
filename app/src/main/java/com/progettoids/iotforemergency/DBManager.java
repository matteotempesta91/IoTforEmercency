package com.progettoids.iotforemergency;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Created by matteotempesta on 02/03/17.
 */

public class DBManager
{
    private DBHelper dbhelper;

    public DBManager(DBHelper dbhelper)
    {
        this.dbhelper=dbhelper;
        Log.i("DBManager:","COSTRUTTORE DBMANAGER");
    }



    public void saveNodo(String codice,String posizione_x,String posizione_y,
                         String quota,String stato,String orario_ultima_ricezione) {

        Log.i("DBManager:","1111");
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ContentValues cv = new ContentValues();


        cv.put(DatabaseStrings.FIELD_NODO_CODICE, codice);
        cv.put(DatabaseStrings.FIELD_NODO_POSIZIONE_X, posizione_x);
        cv.put(DatabaseStrings.FIELD_NODO_POSIZIONE_Y, posizione_y);
        cv.put(DatabaseStrings.FIELD_NODO_QUOTA, quota);
        cv.put(DatabaseStrings.FIELD_NODO_STATO, stato);
        cv.put(DatabaseStrings.FIELD_NODO_ORARIO_ULTIMA_RICEZIONE, orario_ultima_ricezione);

        Log.i("DBManager:","2222");

        try
        {
            db.insert(DatabaseStrings.TBL_NAME_NODO, null, cv);
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
        }
    }

    public void saveBeacon(String mac,String codicenodo,String temperatura,
                           String accelerazione,String umidita,String pressione, String luminosita) {

        Log.i("DBManager:","1111");
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ContentValues cv = new ContentValues();


        cv.put(DatabaseStrings.FIELD_BEACON_MAC, mac);
        cv.put(DatabaseStrings.FIELD_BEACON_CODICE_NODO, codicenodo);
        cv.put(DatabaseStrings.FIELD_BEACON_TEMPERATURA, temperatura);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONE, accelerazione);
        cv.put(DatabaseStrings.FIELD_BEACON_UMIDITA, umidita);
        cv.put(DatabaseStrings.FIELD_BEACON_PRESSIONE, pressione);
        cv.put(DatabaseStrings.FIELD_BEACON_LUMINOSITA, luminosita);

        Log.i("DBManager:","2222");

        try
        {
            db.insert(DatabaseStrings.TBL_NAME_BEACON, null, cv);
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
        }
    }


    /*
    public boolean delete(long id)
    {
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        try
        {
            if (db.delete(DatabaseStrings.TBL_NAME, DatabaseStrings.FIELD_ID+"=?", new String[]{Long.toString(id)})>0)
                return true;
            return false;
        }
        catch (SQLiteException sqle)
        {
            return false;
        }

    }
    */

    //questa va cambiata per leggere tutta la tabella?
    public Cursor query(){
        Log.i("DbManager","metodo query");

        Cursor crs = null;
        try
        {
            SQLiteDatabase db = dbhelper.getReadableDatabase();

            crs = db.query(DatabaseStrings.TBL_NAME_BEACON, null, null, null, null, null, null, null);
            Log.i("beacon",String.valueOf(crs.getCount()));
            crs.moveToLast();
            Log.i("beacon",String.valueOf(crs.getCount()));

            String s2 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_TEMPERATURA));
            Log.i("temp:",s2);
            String s3 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ACCELERAZIONE));
            Log.i("acc:",s3);
            String s4 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_UMIDITA));
            Log.i("umid:",s4);
            String s5 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_PRESSIONE));
            Log.i("pressione:",s5);
            String s6 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_LUMINOSITA));
            Log.i("lumin:",s6);




        }
        catch(SQLiteException sqle)
        {
            return null;
        }
        return crs;
    }



}