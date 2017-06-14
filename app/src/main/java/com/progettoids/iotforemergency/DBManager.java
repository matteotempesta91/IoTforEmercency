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
    public static void saveNodo(String codice, int posizione_x, int posizione_y, int quota) {

        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DatabaseStrings.FIELD_NODO_CODICE, codice);
        cv.put(DatabaseStrings.FIELD_NODO_POSIZIONE_X, posizione_x);
        cv.put(DatabaseStrings.FIELD_NODO_POSIZIONE_Y, posizione_y);
        cv.put(DatabaseStrings.FIELD_NODO_POSIZIONE_Z, quota);
        cv.put(DatabaseStrings.FIELD_NODO_STATO, 0);
       // cv.put(DatabaseStrings.FIELD_NODO_ORARIO_ULTIMA_RICEZIONE, "");

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
        cv.put(DatabaseStrings.FIELD_BEACON_TEMPERATURA, -300);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_UMIDITA, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_PRESSIONE, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_LUMINOSITA, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ORARIO, 0);

        try {
            db.insert(DatabaseStrings.TBL_NAME_BEACON, null, cv);
            Log.i("DBManager", "Beacon" +mac+ "salvato");
        } catch (SQLiteException sqle) {
            Log.e("DBManager", "Errore sql saveBeacon");
        }
        db.close();
    }

    public static void saveNotifica(String nomeNotifica, int dataNotifica) {
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();
        // Converte il formato Date in String per salvarlo nel DB
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        //String dateString = sdf.format(dataNotifica);
        cv.put(DatabaseStrings.FIELD_NOTIFICA_DATA, dataNotifica);
        cv.put(DatabaseStrings.FIELD_NOTIFICA_NOME, nomeNotifica);
        try {
            db.insert(DatabaseStrings.TBL_NAME_NOTIFICA, null, cv);
            Log.i("DBManager", "Notifica Inizializzata");
        } catch (SQLiteException sqle) {
            Log.e("DBManager", "Errore sql saveBeacon: "+sqle.toString());
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
                " WHERE " + DatabaseStrings.FIELD_BEACON_TEMPERATURA + " !=-300;";
        try {
            crs = db.rawQuery(query1, null);
        } catch(SQLiteException sqle) {
            Log.e("DBManager",sqle.toString());
            return null;
        }
        ArrayList<String[]> listaBeacon = new ArrayList<>();
        String[] datiambientali = new String[9];
        // content value per svuotare i campi letti
        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_BEACON_TEMPERATURA, -300);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_UMIDITA,0);
        cv.put(DatabaseStrings.FIELD_BEACON_PRESSIONE, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_LUMINOSITA, 0);
        cv.put(DatabaseStrings.FIELD_BEACON_ORARIO, 0);
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
            datiambientali[8] = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_BEACON_ORARIO));
            listaBeacon.add(datiambientali);

            db.update(DatabaseStrings.TBL_NAME_BEACON, cv, DatabaseStrings.FIELD_BEACON_MAC
                      + "=" + "'" + datiambientali[0] + "'", null);
        }
        crs.close();
        db.close();
        return listaBeacon;
    }

    public static void updateNotifiche(String nomeNotifica, int dataNotifica){
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_NOTIFICA_DATA, dataNotifica);
        db.update(DatabaseStrings.TBL_NAME_NOTIFICA, cv, DatabaseStrings.FIELD_NOTIFICA_NOME
                + "=" + "'" + nomeNotifica + "'", null);
        db.close();
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
         Log.i("DBManager","getPosition query:"+query1);
        Cursor c = db.rawQuery(query1, null);

        int[] posizione = new int[3];
        if(c.moveToFirst()){
            String codice_nodo= c.getString(0);
            String query2="SELECT "+DatabaseStrings.FIELD_NODO_POSIZIONE_X+","
                    +DatabaseStrings.FIELD_NODO_POSIZIONE_Y+","+DatabaseStrings.FIELD_NODO_POSIZIONE_Z
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
     * Aggiorna lo stato del nodo ricevuto dal server nel DB locale
     * @param codice del nodo interessato
     * @param stato aggiornato del suddetto
     */
    public static void updateStatoNodo(String codice, int stato) {
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_NODO_STATO, stato);
        db.update(DatabaseStrings.TBL_NAME_NODO, cv, DatabaseStrings.FIELD_NODO_CODICE
                + "=" + "'" + codice + "'", null);
        db.close();
    }

    // Restituisce il codice del nodo associato con il codice beacon dato in input
    public static String getNodo(String mac_beacon){
        String codice_nodo="";
        SQLiteDatabase db = DBHelper.getInstance(null).getReadableDatabase();
        String query1 = "SELECT "+DatabaseStrings.FIELD_BEACON_CODICE_NODO+" FROM "
                +DatabaseStrings.TBL_NAME_BEACON+" WHERE "+DatabaseStrings.FIELD_BEACON_MAC+"='"+mac_beacon+"';";
        Log.i("DBManager","getNodo query:"+query1);
        Cursor c = db.rawQuery(query1, null);
        if(c.moveToFirst()){
            codice_nodo= c.getString(0);
        }
        c.close();
        db.close();
        return codice_nodo;
    }

    // Restituisce il codice del nodo associato con il codice beacon dato in input
    public static int getDataNotifica(String nomeNotifica){
        int dataNotifica =0;
        SQLiteDatabase db = DBHelper.getInstance(null).getReadableDatabase();
        String query = "SELECT "+DatabaseStrings.FIELD_NOTIFICA_DATA+" FROM "
                +DatabaseStrings.TBL_NAME_NOTIFICA+" WHERE "+DatabaseStrings.FIELD_NOTIFICA_NOME+"='"+nomeNotifica+"';";

        Log.i("DBManager","getDataNotifica query:"+query);

        Cursor c = db.rawQuery(query, null);
        //dataFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        while(c.moveToNext()){
            dataNotifica = c.getInt(0);
        }
        c.close();
        db.close();
        return dataNotifica;
    }

    /**
     * Restituisce i nodi che hanno lo stato diverso da 0. Campi letti: posizione,x,y,z,stato
     * @return
     */
    public static Cursor getStatoNodi() {

        SQLiteDatabase db = DBHelper.getInstance(null).getReadableDatabase();
        String query1 = "SELECT " + DatabaseStrings.FIELD_NODO_STATO+","
                +DatabaseStrings.FIELD_NODO_POSIZIONE_X+","+DatabaseStrings.FIELD_NODO_POSIZIONE_Y
                +","+DatabaseStrings.FIELD_NODO_POSIZIONE_Z + " FROM " + DatabaseStrings.TBL_NAME_NODO
                + " WHERE " + DatabaseStrings.FIELD_NODO_STATO + "<>'" + 0 + "';";
        Cursor c = db.rawQuery(query1, null);
        return c;
    }

    /**
     * Salva i parametri aggiornati nel DB locale inviati dal server
     * è necessario riavviare l'app per caricare i nuovi valori
     * @param param array di object contenente i valori ordinati come nel DB
     */
    public static void updateParametri(int[] param, String filtroBeacon) {
        // salvare i parametri passati dal server come update row
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_T_NOTIFICHE, param[0]);
        cv.put(DatabaseStrings.FIELD_T_STATO_NODI, param[1]);
        cv.put(DatabaseStrings.FIELD_T_SCAN, param[2]);
        cv.put(DatabaseStrings.FIELD_T_SCAN_EMERGENZA, param[3]);
        cv.put(DatabaseStrings.FIELD_T_SCAN_PERIOD, param[4]);
        cv.put(DatabaseStrings.FIELD_T_DATIAMB, param[5]);
        cv.put(DatabaseStrings.FIELD_T_DATIAMB_EMERGENZA, param[6]);
        cv.put(DatabaseStrings.FIELD_T_POSIZIONE, param[7]);
        cv.put(DatabaseStrings.FIELD_T_POSIZIONE_EMERGENZA, param[8]);
        cv.put(DatabaseStrings.FIELD_MAX_TRY_BEACON, param[9]);
        cv.put(DatabaseStrings.FIELD_FILTRO_BLE, filtroBeacon);
        db.update(DatabaseStrings.TBL_NAME_PARAMETRI, cv, DatabaseStrings.FIELD_ID_PARAM
               + "=" + "'" + 0 + "'", null);
        db.close();
    }

    public static void saveParametri(int[] param, String filtroBeacon) {
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_ID_PARAM, 0);
        cv.put(DatabaseStrings.FIELD_T_NOTIFICHE, param[0]);
        cv.put(DatabaseStrings.FIELD_T_STATO_NODI, param[1]);
        cv.put(DatabaseStrings.FIELD_T_SCAN, param[2]);
        cv.put(DatabaseStrings.FIELD_T_SCAN_EMERGENZA, param[3]);
        cv.put(DatabaseStrings.FIELD_T_SCAN_PERIOD, param[4]);
        cv.put(DatabaseStrings.FIELD_T_DATIAMB, param[5]);
        cv.put(DatabaseStrings.FIELD_T_DATIAMB_EMERGENZA, param[6]);
        cv.put(DatabaseStrings.FIELD_T_POSIZIONE, param[7]);
        cv.put(DatabaseStrings.FIELD_T_POSIZIONE_EMERGENZA, param[8]);
        cv.put(DatabaseStrings.FIELD_MAX_TRY_BEACON, param[9]);
        cv.put(DatabaseStrings.FIELD_FILTRO_BLE, filtroBeacon);
        try {
            db.insert(DatabaseStrings.TBL_NAME_PARAMETRI, null, cv);
            Log.i("DBManager", "Parametri salvati");
        } catch (SQLiteException sqle) {
            Log.e("DBManager", "Errore sql saveBeacon");
        }
        db.close();
    }

    public  static void deleteNodi() {
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        db.execSQL("delete from "+ DatabaseStrings.TBL_NAME_NODO);
        db.close();
    }

    public  static void deleteBeacon() {
        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        db.execSQL("delete from "+ DatabaseStrings.TBL_NAME_BEACON);
        db.close();
    }

    /**
     * Carica i parametri salvati nel DB locale,
     * è necessario riavviare l'app in caso di aggiornamento
     * @return array di object contenente i valori ordinati come nel DB
     */
    public static Object[] loadParametri() {
        Object[] param = new Object[11];
        SQLiteDatabase db = DBHelper.getInstance(null).getReadableDatabase();
        String query = "SELECT * FROM "
                +DatabaseStrings.TBL_NAME_PARAMETRI+";";

        Log.i("DBManager","loadParametri query:"+query);
        Cursor c = db.rawQuery(query, null);
        c.moveToNext();
        param[0] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_NOTIFICHE));
        param[1] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_STATO_NODI));
        param[2] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_SCAN));
        param[3] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_SCAN_EMERGENZA));
        param[4] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_SCAN_PERIOD));
        param[5] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_DATIAMB));
        param[6] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_DATIAMB_EMERGENZA));
        param[7] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_POSIZIONE));
        param[8] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_T_POSIZIONE_EMERGENZA));
        param[9] = c.getInt(c.getColumnIndex(DatabaseStrings.FIELD_MAX_TRY_BEACON));
        param[10] = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_FILTRO_BLE));
        c.close();
        db.close();
        return param;
    }
}
