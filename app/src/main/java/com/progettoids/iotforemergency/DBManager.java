
package com.progettoids.iotforemergency;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Created by matteotempesta on 02/03/17.
 *
 * cose da fare nel db:
 *  -METTERE LA CHIUSURA DEL DB
 *  -forse la riga quota
 *
 *  - rigirare tutto in static
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
                         String quota,int stato,String orario_ultima_ricezione) {

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
                           String accelerazionex,String accelerazioney, String accelerazionez,String umidita,
                           String pressione, String luminosita) {

        Log.i("DBManager:","1111");
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ContentValues cv = new ContentValues();


        cv.put(DatabaseStrings.FIELD_BEACON_MAC, mac);
        cv.put(DatabaseStrings.FIELD_BEACON_CODICE_NODO, codicenodo);
        cv.put(DatabaseStrings.FIELD_BEACON_TEMPERATURA, temperatura);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX, accelerazionex);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY, accelerazioney);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ, accelerazionez);
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

            crs = db.query(DatabaseStrings.TBL_NAME_BEACON, null, null, null, null, null, null);
            Log.i("beacon",String.valueOf(crs.getCount()));
            crs.moveToLast();
            Log.i("beacon",String.valueOf(crs.getCount()));

            String s2 = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_TEMPERATURA));
            Log.i("temp:",s2);
            String s3x = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX));
            Log.i("acc:",s3x);
            String s3y = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY));
            Log.i("acc:",s3y);
            String s3z = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ));
            Log.i("acc:",s3z);
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



    public int[] getPosition(String mac_beacon){

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        String query1="SELECT "+DatabaseStrings.FIELD_BEACON_CODICE_NODO+" FROM "+DatabaseStrings.TBL_NAME_BEACON+" WHERE "+DatabaseStrings.FIELD_BEACON_MAC+"='"+mac_beacon+"';";
        Log.i("query1:",query1);
        Cursor c = db.rawQuery(query1, null);

        int[] posizione=new int[3];
        if(c.moveToFirst()){
            String codice_nodo= c.getString(0);
            Log.i("codice_nodo:",codice_nodo);


            String query2="SELECT "+DatabaseStrings.FIELD_NODO_POSIZIONE_X+","+DatabaseStrings.FIELD_NODO_POSIZIONE_Y+","+DatabaseStrings.FIELD_NODO_QUOTA+" FROM "+DatabaseStrings.TBL_NAME_NODO+" WHERE "+DatabaseStrings.FIELD_NODO_CODICE+"='"+codice_nodo+"';";
            Log.i("query2:",query2);
            Cursor c2 = db.rawQuery(query2, null);
            if(c2.moveToFirst()) {
                String x = c2.getString(0);
                Log.i("x:", x);
                posizione[0]= Integer.parseInt(x);

                String y = c2.getString(1);
                Log.i("y:", y);
                posizione[1]= Integer.parseInt(y);

                String z = c2.getString(2);
                Log.i("z:", z);
                posizione[2]= Integer.parseInt(z);
            }
            c2.close();



        }
        c.close();
        db.close();


        return posizione;
    }


/*
query che restituisce i nodi che hanno lo stato diverso da 0 . campi posizione,x,y,z, stato
 */


    public void getNodiStato0() {

        SQLiteDatabase db = dbhelper.getReadableDatabase();
        String query1 = "SELECT " + DatabaseStrings.FIELD_NODO_STATO+","+DatabaseStrings.FIELD_NODO_POSIZIONE_X+","+DatabaseStrings.FIELD_NODO_POSIZIONE_Y+","+DatabaseStrings.FIELD_NODO_QUOTA + " FROM " + DatabaseStrings.TBL_NAME_NODO + " WHERE " + DatabaseStrings.FIELD_NODO_STATO + "<>'" + 0+ "';";
        Log.i("query Diverso da 0:", query1);
        Cursor c = db.rawQuery(query1, null);

        if(c.moveToFirst()) {
            String stato= c.getString(0);
            Log.i("stato:",stato);

            String x= c.getString(1);
            Log.i("---->x:",x);

            String y= c.getString(2);
            Log.i("---->y:",y);

            String z= c.getString(3);
            Log.i("---->z:",z);

        }

    }
}
