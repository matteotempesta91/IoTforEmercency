package com.progettoids.iotforemergency;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.UUID;

import static java.lang.Math.pow;

/**
 * Questa classe estende un task asincrono che si collega al servergatt e recupera i dati dei
 * sensori. Restituisce un array di tali dati.
 */
public class BeaconDriver extends AsyncTask<BluetoothDevice, Void, Object[]> {

        private static final int STATE_DISCONNECTED = 0;
        private static final int STATE_CONNECTED = 2;

        private Context context;
        private BluetoothGattCallback gattCB;
        private BluetoothGatt gattBLE;
        private Object[] sensorData;
        private boolean attesa = true;
        private boolean sensOn = false;
        private int letture = 0;

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
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    //for (BluetoothGattService servizio : gatt.getServices()) {} per debug, scorre tutta la lista
                    if (status == BluetoothGatt.GATT_SUCCESS) { initSensors(gatt); }
                    else { attesa = false; }
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
                                if (mov != null) {
                                    config = mov.getCharacteristic(mmovConfigUuid);
                                    config.setValue(new byte[]{0x38,0});
                                    gatt.writeCharacteristic(config);
                                } else { attesa = false; }
                                break;
                            case ("f000aa82"):
                                UUID mhumServiceUuid = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
                                UUID mhumConfigUuid = UUID.fromString("f000aa22-0451-4000-b000-000000000000");
                                BluetoothGattService hum = gatt.getService(mhumServiceUuid);
                                // Sensore umidità
                                if (hum != null) {
                                    config = hum.getCharacteristic(mhumConfigUuid);
                                    config.setValue(new byte[]{1});
                                    gatt.writeCharacteristic(config);
                                } else { attesa = false; }
                                break;
                            case ("f000aa22"):
                                UUID mbarServiceUuid = UUID.fromString("f000aa40-0451-4000-b000-000000000000");
                                UUID mbarConfigUuid = UUID.fromString("f000aa42-0451-4000-b000-000000000000");
                                BluetoothGattService bar = gatt.getService(mbarServiceUuid);
                                // Barometro
                                if (bar != null) {
                                    config = bar.getCharacteristic(mbarConfigUuid);
                                    config.setValue(new byte[]{1});
                                    gatt.writeCharacteristic(config);
                                } else { attesa = false; }
                                break;
                            case ("f000aa42"):
                                sensOn = true;
                                break;
                        }

                    }
                    else { attesa = false; }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic car, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                        String servizio = car.getUuid().toString().substring(0,8);

                        double var = shortUnsignedAtOffset(car,0).doubleValue();
                        Log.i("Prova ->", "BBBBBBB"+ String.valueOf(var));


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
                                hum = hum - (hum % 4);

                                hum = (-6f) + 125f * (hum / 65535f);
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
                                final Integer t_r;	// Temperature raw value from sensor
                                final Integer p_r;	// Pressure raw value from sensor
                                final Double t_a; 	// Temperature actual value in unit centi degrees celsius
                                final Double S;	// Interim value in calculation
                                final Double O;	// Interim value in calculation
                                final Double p_a; 	// Pressure actual value in unit Pascal.

                                double[] coefficients = new double[8];

                                coefficients[0] = shortUnsignedAtOffset(car,0).doubleValue();
                                Log.i("Coefficente0 ->", "BBBBBBB"+ String.valueOf(coefficients[0]));
                                coefficients[1] = shortUnsignedAtOffset(car, 2).doubleValue();
                                Log.i("Coefficente1 ->", "BBBBBBB"+ String.valueOf(coefficients[1]));
                                coefficients[2] = shortUnsignedAtOffset(car, 4).doubleValue();
                                Log.i("Coefficente2", "Riesco a leggerlo?" + String.valueOf(coefficients[2]) );
                                //Bisogna trovare un modo per leggere i valori dopo la terza chiamata
                                coefficients[3] = shortUnsignedAtOffset(car, 6).doubleValue();
                                coefficients[4] = shortSignedAtOffset(car, 8).doubleValue();
                                coefficients[5] = shortSignedAtOffset(car, 10).doubleValue();//sta la stessa chiamata qualche riga sopra(144)
                                coefficients[6] = shortSignedAtOffset(car, 12).doubleValue();
                                coefficients[7] = shortSignedAtOffset(car, 14).doubleValue();
                                Log.i("Ci sono", "NNNNNNNNNNNN");


                                t_r = shortSignedAtOffset(car, 0);
                                p_r = shortUnsignedAtOffset(car, 2);

                                t_a = (100 * (coefficients[0] * t_r / pow(2,8) + shortUnsignedAtOffset(car, 2) * pow(2,6))) / pow(2,16);
                                Log.i("Temperatura t_a", "Leggi la Temperatura ->" + String.valueOf(t_a));
                                S = coefficients[2] + coefficients[3] * t_r / pow(2,17) + ((coefficients[4] * t_r / pow(2,15)) * t_r) / pow(2,19);
                                O = coefficients[5] * pow(2,14) + coefficients[6] * t_r / pow(2,3) + ((coefficients[7] * t_r / pow(2,15)) * t_r) / pow(2,4);
                                p_a = (S * p_r + O) / pow(2,14);
                                sensorData[3] = p_a;

                                break;

                        }
                        if (letture > 5 ) {
                            attesa = false;
                        }
                        letture++;
                    }
                    else { attesa = false; }
                }
            };
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
        protected Object[] doInBackground(BluetoothDevice... faro) {
            gattBLE = faro[0].connectGatt(context, false, gattCB);

            UUID mtempServiceUuid = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
            UUID mtempDataUuid = UUID.fromString("f000aa01-0451-4000-b000-000000000000");

            // Attende lettura completa per max 5*2,5 sec
            for (int i=0; attesa && i < 5;  i++) {
                try {
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
            if (attesa) { Log.i("attesa", "massime iterazioni"); }
            gattBLE.disconnect();
            gattBLE.close();
            return sensorData;
        }


        // Questo codice è eseguito nel thread chiamente al termine del task
        @Override
        protected void onPostExecute(Object[] sensorData) {
            // ogni tanto il context è null ...
            if (context != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage("Sensore: " + gattBLE.getDevice().toString() +
                        "\n" + "Temp: " + String.valueOf((double) sensorData[0]) +
                        "\n" + "Moviment:"+ String.valueOf(((double[]) sensorData[1])[0]).substring(0,6) + ":" + String.valueOf(((double[]) sensorData[1])[1]).substring(0,6)
                        + ":" + String.valueOf(((double[]) sensorData[1])[2]).substring(0,6) +
                        "\n" + "Humidity: " + String.valueOf((double) sensorData[2]) +
                        "\n" + "Pressione: " /*+ String.valueOf( (double) sensorData[3])*/)
                        .setTitle("Dati sensore letti");
                AlertDialog dialog = builder.create();
                dialog.show();
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
            } else { attesa = false; }

            // Init Sensore ...
        }
    }