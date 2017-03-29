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
        Log.i("DBManager:","rrrrasdsadadujujujrr");
    }



    public void save(String codice,String id_beacon,String posizione_x,String posizione_y,
                     String quota,String stato,String temperatura,String luminosita,
                     String accelerazione,String numero_persone,String orario_ultima_ricezione) {

        Log.i("DBManager:","1111");
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ContentValues cv = new ContentValues();


        cv.put(DatabaseStrings.FIELD_CODICE, codice);
        cv.put(DatabaseStrings.FIELD_ID_BEACON, id_beacon);
        cv.put(DatabaseStrings.FIELD_POSIZIONE_X, posizione_x);
        cv.put(DatabaseStrings.FIELD_POSIZIONE_Y, posizione_y);
        cv.put(DatabaseStrings.FIELD_QUOTA, quota);
        cv.put(DatabaseStrings.FIELD_STATO, stato);
        cv.put(DatabaseStrings.FIELD_TEMPERATURA, temperatura);
        cv.put(DatabaseStrings.FIELD_UMIDITA, temperatura);
        cv.put(DatabaseStrings.FIELD_PRESSIONE, temperatura);
        cv.put(DatabaseStrings.FIELD_LUMINOSITA, luminosita);
        cv.put(DatabaseStrings.FIELD_ACCELERAZIONE, accelerazione);
        cv.put(DatabaseStrings.FIELD_NUMERO_PERSONE, numero_persone);
        cv.put(DatabaseStrings.FIELD_ORARIO_ULTIMA_RICEZIONE, orario_ultima_ricezione);

        Log.i("DBManager:","2222");

        try
        {
            db.insert(DatabaseStrings.TBL_NAME, null, cv);
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
        Cursor crs = null;
        try
        {
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            crs = db.query(DatabaseStrings.TBL_NAME, null, null, null, null, null, null, null);
            crs.moveToLast();
           // String s = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_CODICE));
            String s2 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_TEMPERATURA));
            String s3 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_ACCELERAZIONE));
            String s4 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_UMIDITA));
            String s5 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_PRESSIONE));
            String s6 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_LUMINOSITA));
            //String s= String.valueOf(crs.getColumnIndex(DatabaseStrings.FIELD_CODICE));
            Log.i("DBMANAGER","99999");
            Log.i("DBMANAGER",s2+"-"+s3+"-"+s4+"-"+s5+"-"+s6);

        }
        catch(SQLiteException sqle)
        {
            return null;
        }
        return crs;
    }



}