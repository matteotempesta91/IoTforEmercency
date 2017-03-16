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
                if (status == BluetoothGatt.GATT_SUCCESS) { sensOn = true; }
                else { attesa = false; }
        }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic car, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    int offset = 0;
                    Integer lowerByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
                    Integer upperByte = car.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.
                    double temp = ((upperByte << 8) + lowerByte) / 128.0;

                    //metodo per leggere il sensore di movimento
                    int x = car.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8,0);
                    int y = car.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8,1);
                    int z = car.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8,2) * -1;

                    double scaledX = x / 64.0;
                    double scaledY = y / 64.0;
                    double scaledZ = z / 64.0;

                    double[] mov = new double[]{scaledX,scaledY,scaledZ};

                    //metodo per leggere il sensore dell'umidità
                    //int a = shortUnsignedAtOffset(car, 2);

                    //Integer lowersByte = car.getIntValue(FORMAT_UINT8, offset);
                    //Integer uppersByte = car.getIntValue(FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.



                    // Ripete fino al primo valore non nullo
                    if (letture > 10 || temp != 0 ) {
                        attesa = false;
                        sensorData[0] = temp;
                        sensorData[1] = mov;
                        //sensorData[]

                    }
                    letture++;
                }
                else { attesa = false; }
            }
        };
    }

    // Effettua la connessione in background ed attende che i dati siano disponibili
    @Override
    protected Object[] doInBackground(BluetoothDevice... faro) {
        gattBLE = faro[0].connectGatt(context, false, gattCB);
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
                UUID mtempServiceUuid = UUID.fromString("f000aa00-0451-4000-b000-000000000000");
                UUID mtempDataUuid = UUID.fromString("f000aa01-0451-4000-b000-000000000000");
                UUID mmovServiceUuid = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
                UUID mmovDataUuid = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
                UUID mhumServiceUuid = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
                UUID mhumDataUuid = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
                BluetoothGattService temp = gattBLE.getService(mtempServiceUuid);
                BluetoothGattCharacteristic datatemp = temp.getCharacteristic(mtempDataUuid);
                BluetoothGattService mov = gattBLE.getService(mmovServiceUuid);
                BluetoothGattCharacteristic datamov = mov.getCharacteristic(mmovDataUuid);
                BluetoothGattService hum = gattBLE.getService(mhumServiceUuid);
                BluetoothGattCharacteristic datahum = mov.getCharacteristic(mhumDataUuid);
                gattBLE.readCharacteristic(datatemp);
                gattBLE.readCharacteristic(datamov);
                gattBLE.readCharacteristic(datahum);
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

            //Double d=new Double(sensorData[1].toString());
            //Log.i("tipo1:",String.valueOf((double) sensorData[0]));


            builder.setMessage("Sensore: " + gattBLE.getDevice().toString() +
                    "\n" + "Temp: " + String.valueOf((double) sensorData[0]) +
                    "\n" + "Moviment:"+ String.valueOf(((double[]) sensorData[1])[0]) + " " + String.valueOf(((double[]) sensorData[1])[1])
                    + " " + String.valueOf(((double[]) sensorData[1])[2]))
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
        UUID mmovServiceUuid = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
        UUID mmovConfigUuid = UUID.fromString("f000aa82-0451-4000-b000-000000000000");
        UUID mhumServiceUuid = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
        UUID mhumConfigUuid = UUID.fromString("f000aa22-0451-4000-b000-000000000000");

        BluetoothGattService temp = gatt.getService(mtempServiceUuid);
        BluetoothGattService mov = gatt.getService(mmovServiceUuid);
        BluetoothGattService hum = gatt.getService(mhumServiceUuid);

        // Nel caso il service non esiste, ritorna null
        if (temp != null && mov != null) {
            BluetoothGattCharacteristic config = temp.getCharacteristic(mtempConfigUuid);
            BluetoothGattCharacteristic config1 = mov.getCharacteristic(mmovConfigUuid);
            BluetoothGattCharacteristic config2 = hum.getCharacteristic(mhumConfigUuid);
            config.setValue(new byte[]{1});
            config1.setValue(new byte[]{1});
            config2.setValue(new byte[]{1});
            gatt.writeCharacteristic(config);
            gatt.writeCharacteristic(config1);
            gatt.writeCharacteristic(config2);
        } else { attesa = false; }

        // Init Sensore ...
    }
}
