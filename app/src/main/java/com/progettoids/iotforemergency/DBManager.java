package com.progettoids.iotforemergency;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

/**
 * Fornisce i metodi per effettuare ed interpretare le query al DB locale
 * DBhelper deve essere già stato invocato da un'activity
 */

public class DBManager {

    /**
     * Salva un nuovo nodo nella tabella
     * @param codice : ID unico del nodo
     * @param posizione_x
     * @param posizione_y
     * @param quota : o posizione z
     */
    public static void saveNodo(String codice,String posizione_x,String posizione_y,
                         String quota) {

        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DatabaseStrings.FIELD_NODO_CODICE, codice);
        cv.put(DatabaseStrings.FIELD_NODO_POSIZIONE_X, posizione_x);
        cv.put(DatabaseStrings.FIELD_NODO_POSIZIONE_Y, posizione_y);
        cv.put(DatabaseStrings.FIELD_NODO_QUOTA, quota);
        cv.put(DatabaseStrings.FIELD_NODO_STATO, 0);
        cv.put(DatabaseStrings.FIELD_NODO_ORARIO_ULTIMA_RICEZIONE, "");

        try {
            db.insert(DatabaseStrings.TBL_NAME_NODO, null, cv);
            Log.i("DBManager", "Nodo salvato");
        } catch (SQLiteException sqle) {
            Log.e("DBManager", "Errore sql saveNodo");
        }
        db.close();
    }

    /**
     * Salva un nuovo Beacon nel DB locale
     * @param mac : indirizzo MAC bluetooth del beacon
     * @param codicenodo : nodo al quale è associato
     */
    public static void saveBeacon(String mac,String codicenodo) {

        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DatabaseStrings.FIELD_BEACON_MAC, mac);
        cv.put(DatabaseStrings.FIELD_BEACON_CODICE_NODO, codicenodo);
        cv.put(DatabaseStrings.FIELD_BEACON_TEMPERATURA, "");
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX, "");
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY, "");
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ, "");
        cv.put(DatabaseStrings.FIELD_BEACON_UMIDITA, "");
        cv.put(DatabaseStrings.FIELD_BEACON_PRESSIONE, "");
        cv.put(DatabaseStrings.FIELD_BEACON_LUMINOSITA, "");

        try {
            db.insert(DatabaseStrings.TBL_NAME_BEACON, null, cv);
            Log.i("DBManager", "Beacon salvato");
        } catch (SQLiteException sqle) {
            Log.e("DBManager", "Errore sql saveBeacon");
        }
        db.close();
    }

    /**
     * Legge e poi elimina i dati ambientali salvati nel DB locale dei beacon incontrati
     * @return : array bidimensionale di lunghezza pari al numero di beacon incontrati contenente
     *          i rispettivi dati ambientali e macAddress
     */
    public static ArrayList<String[]> getdatiambientali(){

        Cursor crs = null;
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        String query1 = "SELECT * FROM " + DatabaseStrings.TBL_NAME_BEACON +
                " WHERE NOT" + DatabaseStrings.FIELD_BEACON_TEMPERATURA + "IS NULL" + "';";
        try {
            crs = db.rawQuery(query1, null);
        } catch(SQLiteException sqle) {
            Log.e("DBManager","Errore getDatiAmb");
            return null;
        }
        ArrayList<String[]> listaBeacon = new ArrayList<>();
        String[] datiambientali = new String[8];
        // content value per svuotare i campi letti
        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_BEACON_TEMPERATURA, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_UMIDITA,0);
        cv.put(DatabaseStrings.FIELD_BEACON_PRESSIONE, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_LUMINOSITA, 0);
        // Ciclo per interpretare i risultati
        while (crs.moveToNext()) {
            datiambientali[0] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_MAC));
            datiambientali[1] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_TEMPERATURA));
            datiambientali[2] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX));
            datiambientali[3] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY));
            datiambientali[4] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ));
            datiambientali[5] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_UMIDITA));
            datiambientali[6] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_LUMINOSITA));
            datiambientali[7] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_PRESSIONE));
            listaBeacon.add(datiambientali);
            db.update(DatabaseStrings.TBL_NAME_BEACON, cv, DatabaseStrings.FIELD_BEACON_MAC
                      + "=" + "'" + datiambientali[0] + "'", null);
        }
        crs.close();
        db.close();
        return listaBeacon;
    }

    /**
     * Recupera la posizione dell'utente in base al beacon più vicino ricevuto in input
     * @param mac_beacon : macAddress del beacon più vicino
     * @return : posizione gemoetrica
     */
    public static int[] getPosition(String mac_beacon){

        SQLiteDatabase db = DBHelper.getInstance(null).getReadableDatabase();
        String query1 = "SELECT "+DatabaseStrings.FIELD_BEACON_CODICE_NODO+" FROM "
                +DatabaseStrings.TBL_NAME_BEACON+" WHERE "+DatabaseStrings.FIELD_BEACON_MAC+"='"+mac_beacon+"';";
        Cursor c = db.rawQuery(query1, null);

        int[] posizione=new int[3];
        if(c.moveToFirst()){
            String codice_nodo= c.getString(0);
            String query2="SELECT "+DatabaseStrings.FIELD_NODO_POSIZIONE_X+","
                    +DatabaseStrings.FIELD_NODO_POSIZIONE_Y+","+DatabaseStrings.FIELD_NODO_QUOTA
                    +" FROM "+DatabaseStrings.TBL_NAME_NODO+" WHERE "+DatabaseStrings.FIELD_NODO_CODICE+"='"+codice_nodo+"';";
            Cursor c2 = db.rawQuery(query2, null);
            if(c2.moveToFirst()) {
                String x = c2.getString(0);
                posizione[0]= Integer.parseInt(x);

                String y = c2.getString(1);
                posizione[1]= Integer.parseInt(y);

                String z = c2.getString(2);
                posizione[2]= Integer.parseInt(z);
            }
            c2.close();
        }
        c.close();
        db.close();
        return posizione;
    }

    /**
     * Restituisce i nodi che hanno lo stato diverso da 0. Campi letti: posizione,x,y,z,stato
     * @return
     */
    public static Cursor getStatoNodi() {

        SQLiteDatabase db = DBHelper.getInstance(null).getReadableDatabase();
        String query1 = "SELECT " + DatabaseStrings.FIELD_NODO_STATO+","
                +DatabaseStrings.FIELD_NODO_POSIZIONE_X+","+DatabaseStrings.FIELD_NODO_POSIZIONE_Y
                +","+DatabaseStrings.FIELD_NODO_QUOTA + " FROM " + DatabaseStrings.TBL_NAME_NODO
                + " WHERE " + DatabaseStrings.FIELD_NODO_STATO + "<>'" + 0+ "';";
        Cursor c = db.rawQuery(query1, null);
        return c;
        /* Debug code
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
        */
    }
}
