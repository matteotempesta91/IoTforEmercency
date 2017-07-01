package com.progettoids.iotforemergency.beacondriver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.progettoids.iotforemergency.db.DatabaseStrings;
import com.progettoids.iotforemergency.gestionedati.Parametri;
import com.progettoids.iotforemergency.db.DBHelper;

import java.util.UUID;

import static java.lang.Math.pow;

/**
 * Questa classe estende un task asincrono che si collega al servergatt e recupera i dati dei
 * sensori.
 * I parametri sono: BeaconListener invocatore, BluetoothDevice su cui leggere.
 * Restituisce un array di tali dati.
 */
public class BeaconDriver extends AsyncTask<Object, Void, Object[]> {

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private Context context;
    private BluetoothGattCallback gattCB;
    private BluetoothGatt gattBLE; // superfluo se si rimuove il messaggio in postExecute
    private Object[] sensorData;
    // Status variables
    private boolean attesa = true;
    private boolean sensOn = false;
    private String error;

    public BeaconDriver(Context context) {
            super();
            this.context = context;
            sensorData = new Object[7];

            // Definisce la callBack, quando si è connessi si procede al recupero dati
            gattCB = new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == STATE_CONNECTED) {
                            Log.i(gatt.getDevice().toString(), "Connected to GATT server.");
                            gatt.discoverServices();
                        } else if (newState == STATE_DISCONNECTED) {
                            Log.i(gatt.getDevice().toString(), "Disconnected from GATT server.");
                        }
                    } else {
                        attesa = false;
                        error = "Connection failed";
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    //for (BluetoothGattService servizio : gatt.getServices()) {} per debug, scorre tutta la lista
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        initSensors(gatt);
                    } else {
                        attesa = false;
                        error = "Servizi non letti";
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic car, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        String servizio = car.getUuid().toString().substring(0,8);


                        switch (servizio) {
                            case ("f000aa02"):
                                UUID mmovServiceUuid = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
                                UUID mmovConfigUuid = UUID.fromString("f000aa82-0451-4000-b000-000000000000");
                                BluetoothGattService mov = gatt.getService(mmovServiceUuid);
                                BluetoothGattCharacteristic config = null;
                                //sensore accelerometro
                                config = mov.getCharacteristic(mmovConfigUuid);
                                config.setValue(new byte[]{0x38,0});
                                gatt.writeCharacteristic(config);
                                break;
                            case ("f000aa82"):
                                UUID mhumServiceUuid = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
                                UUID mhumConfigUuid = UUID.fromString("f000aa22-0451-4000-b000-000000000000");
                                BluetoothGattService hum = gatt.getService(mhumServiceUuid);
                                // Sensore umidità
                                config = hum.getCharacteristic(mhumConfigUuid);
                                config.setValue(new byte[]{1});
                                gatt.writeCharacteristic(config);
                                break;
                            case ("f000aa22"):
                                UUID mbarServiceUuid = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
                                UUID mbarConfigUuid = UUID.fromString("f000aa42-0451-4000-b000-000000000000");
                                BluetoothGattService bar = gatt.getService(mbarServiceUuid);
                                // Barometro
                                config = bar.getCharacteristic(mbarConfigUuid);
                                config.setValue(new byte[]{1});
                                gatt.writeCharacteristic(config);
                                break;
                            case ("f000aa42"):
                                UUID mopticServiceUuid = UUID.fromString("f000aa70-0451-4000-b000-000000000000");
                                UUID mopticConfigUuid = UUID.fromString("f000aa72-0451-4000-b000-000000000000");
                                BluetoothGattService optic = gatt.getService(mopticServiceUuid);
                                //Sensore Ottico
                                config =optic.getCharacteristic(mopticConfigUuid);
                                config.setValue(new  byte[]{1});
                                gatt.writeCharacteristic(config);
                                break;
                            case ("f000aa72"):
                                sensOn = true;
                                break;

                        }

                    }
                    else {
                        attesa = false;
                        error = "Sensori non attivati";
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic car, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        String servizio = car.getUuid().toString().substring(0,8);

                        switch (servizio) {
                            // Temperatura
                            case ("f000aa01") :
                                double temp = shortUnsignedAtOffset(car, 0) / 128.0;
                                sensorData[0] = temp;

                                // Lettura sensore Accelerometro
                                UUID mmovServiceUuid = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
                                UUID mmovDataUuid = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
                                BluetoothGattService mov = gattBLE.getService(mmovServiceUuid);
                                BluetoothGattCharacteristic datamov = mov.getCharacteristic(mmovDataUuid);
                                gattBLE.readCharacteristic(datamov);

                                break;
                            // Accelerometro
                            case ("f000aa81") :
                                //metodo per leggere il sensore di movimento
                                double x = shortSignedAtOffset(car, 6).doubleValue() / (64*64);
                                double y = shortSignedAtOffset(car, 8).doubleValue() / (64*64);
                                double z = shortSignedAtOffset(car, 10).doubleValue() * -1 / (64*64);

                                sensorData[1] = new double[]{x,y,z};

                                // Lettura sensore Umidità
                                UUID mhumServiceUuid = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
                                UUID mhumDataUuid = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
                                BluetoothGattService humS = gattBLE.getService(mhumServiceUuid);
                                BluetoothGattCharacteristic datahum = humS.getCharacteristic(mhumDataUuid);
                                gattBLE.readCharacteristic(datahum);
                                break;
                            // Humidostato
                            case ("f000aa21") :
                                double hum = shortUnsignedAtOffset(car, 2).doubleValue();
                                // nota: se il beacon si sta scaricando, il sensore non funziona propriamente
                                hum = (hum / 65536)*100;
                                sensorData[2] = hum;
                                // Lettura sensore Pressione
                                UUID mbarServiceUuid = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
                                UUID mbarDataUuid = UUID.fromString("f000aa41-0451-4000-b000-000000000000");
                                BluetoothGattService bar = gattBLE.getService(mbarServiceUuid);
                                BluetoothGattCharacteristic databar = bar.getCharacteristic(mbarDataUuid);
                                gattBLE.readCharacteristic(databar);
                                break;
                            //Barometro
                            case ("f000aa41") :
                                Integer lowerByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);
                                Integer middleByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3 + 1);
                                Integer upperByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3 + 2);
                                // costruisce il dato
                                Integer data = (upperByte << 16) + (middleByte << 8) + lowerByte;

                                sensorData[3] = data/100.0d;
                                //Lettura sensore Ottico
                                UUID mopticServiceUuid = UUID.fromString("f000aa70-0451-4000-b000-000000000000");
                                UUID mopticDataUuid = UUID.fromString("f000aa71-0451-4000-b000-000000000000");
                                BluetoothGattService optic = gattBLE.getService(mopticServiceUuid);
                                BluetoothGattCharacteristic dataoptic = optic.getCharacteristic(mopticDataUuid);
                                gattBLE.readCharacteristic(dataoptic);
                                break;
                            case ("f000aa71") :
                                Integer rawData,e,m;
                                rawData = shortUnsignedAtOffset(car,0).intValue();
                                m = rawData & 0x0FFF;
                                e = (rawData & 0xF000) >> 12;
                                sensorData[4] = m * (0.01 * pow(2.0,e));

                                // letti tutti i dati termina l'attesa
                                attesa = false;
                                break;

                        }
                    }
                    else {
                        attesa = false;
                        error = "errore lettura dati";
                    }
                }
            };
        }

    // Metodo per salvare i dati sul DataBase
    public void salvataggioDatiDB(String mac) {

        SQLiteDatabase db = DBHelper.getInstance(null).getWritableDatabase();
        //+":"+String.valueOf(((double[]) sensorData[1])[1])+":"+String.valueOf(((double[]) sensorData[1])[2]));
        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_BEACON_TEMPERATURA, ((double) sensorData[0]));
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEX, (((double[]) sensorData[1])[0]));
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEY, (((double[]) sensorData[1])[1]));
        cv.put(DatabaseStrings.FIELD_BEACON_ACCELERAZIONEZ, (((double[]) sensorData[1])[2]));
        cv.put(DatabaseStrings.FIELD_BEACON_UMIDITA,((double) sensorData[2]));
        cv.put(DatabaseStrings.FIELD_BEACON_PRESSIONE, ((double) sensorData[3]));
        cv.put(DatabaseStrings.FIELD_BEACON_LUMINOSITA, ((double) sensorData[4]));
        Long tsLong = (System.currentTimeMillis()/1000)+ 7200;
        Log.i("BeaconDriver","salvataggioDatiDB time now: "+tsLong);
        cv.put(DatabaseStrings.FIELD_BEACON_ORARIO, tsLong);

        Log.i("Indirizzo Mac ->", mac);
        db.update(DatabaseStrings.TBL_NAME_BEACON, cv, DatabaseStrings.FIELD_BEACON_MAC + "=" + "'" + mac + "'", null);
    }

    // Recupera i UNSIGNED dati dalla characteristic
    private Integer shortUnsignedAtOffset(BluetoothGattCharacteristic car, int offset) {
        // recupera i byte
        Integer lowerByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.
        // costruisce il dato
        Integer data = (upperByte << 8) + lowerByte;
        return data;
    }

    // Recupera i SIGNED dati dalla characteristic
    private Integer shortSignedAtOffset(BluetoothGattCharacteristic car, int offset) {
        // recupera i byte
        Integer lowerByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset);
        Integer upperByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1); // Note: interpret MSB as unsigned.
        // costruisce il dato
        Integer data = (upperByte << 8) + lowerByte;
        return data;
    }

    // Effettua la connessione in background ed attende che i dati siano disponibili
    @Override
    protected Object[] doInBackground(Object... param) {
            gattBLE = ((BluetoothDevice)param[1]).connectGatt(context, false, gattCB);

            UUID mtempServiceUuid = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
            UUID mtempDataUuid = UUID.fromString("f000aa01-0451-4000-b000-000000000000");

            // Attende lettura completa per max 5*2,5 sec
            // Attesa resta true finchè non sono disponibili tutti i dati ambientali
            for (int i = 0; attesa && i < Parametri.getInstance().MAX_TRY_BEACON; i++) {
                try {
                    // Permette ai sensori di acquisire un pool di dati minimo
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                // Attende sensore attivo
                if (sensOn) {
                    // Lettura sensore Temperatura
                    BluetoothGattService temp = gattBLE.getService(mtempServiceUuid);
                    BluetoothGattCharacteristic datatemp = temp.getCharacteristic(mtempDataUuid);
                    gattBLE.readCharacteristic(datatemp);

                }
            }
            // Se per qualche motivo non sono disponibili in un tempo limite, viene stampato il log di errore
            if (attesa) { Log.i("BeaconDriver", "attesa: true, massime iterazioni"); }
            else if (error != null) {Log.i("Errore ", error); }
            gattBLE.disconnect();
            gattBLE.close();
            // Lettura beacon successivo
            ((BeaconListener)param[0]).signal();
            return sensorData;
        }

    // Questo codice è eseguito nel thread chiamente al termine del task
    @Override
    protected void onPostExecute(Object[] sensorData) {
            if (context != null && error == null && !attesa) {
                salvataggioDatiDB(gattBLE.getDevice().getAddress().toString());
                /*
                // Debug Code: mostra un allert con i dati letti
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage("Sensore: " + gattBLE.getDevice().toString() +
                        "\n" + "Temp: " + String.valueOf((double) sensorData[0]) +
                        "\n" + "Moviment: "+ String.valueOf(((double[]) sensorData[1])[0]) + "\n \t \t" + String.valueOf(((double[]) sensorData[1])[1])
                        + "\n \t \t" + String.valueOf(((double[]) sensorData[1])[2]) +
                        "\n" + "Humidity: " + String.valueOf((double) sensorData[2]) +
                        "\n" + "Pressione hPA: " + String.valueOf( (double) sensorData[3]) +
                        "\n" + "Luminosità: " + String.valueOf( sensorData[4]))
                        .setTitle("Dati sensore letti");
                AlertDialog dialog = builder.create();
                dialog.show();
                */
            } else { Log.i("context ", "null"); }
        }

    // Attiva i sensori del beacon
    private void initSensors(BluetoothGatt gatt) {

            // Init Sensore Temperatura
            UUID mtempServiceUuid = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
            UUID mtempConfigUuid = UUID.fromString("f000aa02-0451-4000-b000-000000000000");

            BluetoothGattService temp = gatt.getService(mtempServiceUuid);

            BluetoothGattCharacteristic config = null;
            // Nel caso il service non esiste, ritorna null

            // Sensore temperatura
            if (temp != null) {
                config = temp.getCharacteristic(mtempConfigUuid);
                config.setValue(new byte[]{1});
                gatt.writeCharacteristic(config);
            } else {
                attesa = false;
                error = " temp service assente";
            }
        }
    }