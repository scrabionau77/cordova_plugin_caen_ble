//package android;
package CaenBle;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.caen.RFIDLibrary.*;


import com.caen.RFIDLibrary.CAENRFIDException;
import com.caen.RFIDLibrary.CAENRFIDLogicalSource;
import com.caen.RFIDLibrary.CAENRFIDReader;

public class CaenBle extends CordovaPlugin {

    private static final int MY_PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_ENABLE_BT = 200;
    private static final int REQUEST_LOCATION_PERMISSION = 201;
    UUID MY_CHARACTERISTIC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private CallbackContext callbackContext;
    private BluetoothGatt gatt;
    private Queue<BluetoothGattCharacteristic> readQueue = new LinkedList<>();
    CAENRFIDReader r = new CAENRFIDReader();
    private Handler tagCheckHandler;
    private Runnable tagCheckRunnable;



    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if ("discoverDevices".equals(action)) {
            discoverDevices();
            return true;
        } else if ("connectToDevice".equals(action)) {
            String address = args.getString(0);
            try {
                connectToDevice(address);
            } catch (CAENRFIDException e) {
                throw new RuntimeException(e);
            }
            return true;
        }else if ("stopDiscovery".equals(action)) {
            stopDiscovery(callbackContext);
            return true;
        }else if ("disconnectDevice".equals(action)) {
            disconnectDevice(callbackContext);
            return true;
        }else if ("requestPermissions".equals(action)) {
            requestPermissions();
            callbackContext.success("Permessi richiesti");
            return true;
        }else if (action.equals("startTagCheck")) {
            startTagCheck(callbackContext);
            return true;
        } else if (action.equals("stopTagCheck")) {
            stopTagCheck(callbackContext);
            return true;
        }

        return false;
    }

    private void discoverDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT},
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        bluetoothLeScanner.startScan(scanCallback);
        Log.d("MyBluetoothPlugin", "Scansione iniziata...");
    }

    private void stopDiscovery(CallbackContext callbackContext) {
        if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT},
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
            Log.d("MyBluetoothPlugin", "Scansione arrestata");
            callbackContext.success("Scansione arrestata");
        } else {
            Log.d("MyBluetoothPlugin", "Scansione non iniziata o scanner non disponibile");
            callbackContext.error("Scansione non iniziata o scanner non disponibile");
        }
    }

    /**
     * La scanCallback prende il nome e l'address dei dispositivi che trova
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            JSONObject deviceInfo = new JSONObject();
            try {
                if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(cordova.getActivity(),
                            new String[]{Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.BLUETOOTH_CONNECT},
                            MY_PERMISSIONS_REQUEST_CODE);
                }
                deviceInfo.put("name", device.getName());
                deviceInfo.put("address", device.getAddress());
                callbackContext.success(deviceInfo);
            } catch (JSONException e) {
                callbackContext.error("Error processing device info.");
            }
        }
    };

    /**
     * Metodo per la connessione al dispositivo skID
     */
    private void connectToDevice(String address) throws CAENRFIDException {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT},
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        gatt = device.connectGatt(cordova.getActivity(), false, gattCallback);
        if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MY_PERMISSIONS_REQUEST_CODE);
        } else {
            gatt.discoverServices();
            //gatt.connect();
            r.Connect(cordova.getContext(), device);
            Log.d("MyBluetoothPlugin", "Connesso al dispositivo con indirizzo " + address);
        }
    }

    /**
     * La gattCallback viene usata per stampare lo stato della connessione con il dispositivo
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(cordova.getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MY_PERMISSIONS_REQUEST_CODE);
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("MyBluetoothPlugin", "Connesso al dispositivo");
                Log.d("MyBluetoothPlugin", "Lo stato della connessione PRIMA del gatt.discoverServices è " + status);
                gatt.discoverServices();
                Log.d("MyBluetoothPlugin", "Lo stato della connessione DOPO del gatt.discoverServices è " + status);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("MyBluetoothPlugin", "Disconnesso dal dispositivo");
                Log.d("MyBluetoothPlugin", "Lo stato della connessione è " + status);
            }
        }

    };

    /**
     * Questi 2 metodi venivano utilizzati precedentemente per scoprire i servizi che il
     * dispositivo offriva per lavolarli in maniera custom in attesa dell'sdk 5.0
     */
    private void readNextCharacteristic(BluetoothGatt gatt) {
        if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT},
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        if (!readQueue.isEmpty()) {
            BluetoothGattCharacteristic characteristic = readQueue.poll();
            gatt.readCharacteristic(characteristic);
        } else {
            Log.d("MyBluetoothPlugin", "Tutte le caratteristiche sono state lette.");
        }
    }

    private void readCharacteristicInLoop(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final int delay = 1000; // ritardo in millisecondi (1 secondo)

        handler.postDelayed(new Runnable() {
            public void run() {
                if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(cordova.getActivity(),
                            new String[]{Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.BLUETOOTH_CONNECT},
                            MY_PERMISSIONS_REQUEST_CODE);
                }
                // Leggi la caratteristica
                gatt.readCharacteristic(characteristic);

                // Riavvia il runnable con un ritardo
                handler.postDelayed(this, delay);
            }
        }, delay);
    }


    /**
     * Metodo per la disconnessione del dispositivo
     */
    private void disconnectDevice(CallbackContext callbackContext) {
        if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT},
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            gatt = null;
            Log.d("MyBluetoothPlugin", "Disconnesso con successo!");
            callbackContext.success("Disconnesso con successo.");
        } else {
            Log.d("MyBluetoothPlugin", "Nessun dispositivo connesso!");
            callbackContext.error("Nessun dispositivo connesso.");
        }
    }

    /**
     * Metodo per la gestione dei permessi (posizione e bluetooth)
     */
    public void requestPermissions() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            callbackContext.error("Il dispositivo non supporta il Bluetooth.");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            cordova.startActivityForResult(this, enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    /**
     * Metodo per capire se il bluetooth è attivato o no
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("MyBluetoothPlugin", "Bluetooth abilitato");
                } else {
                    Log.d("MyBluetoothPlugin", "Bluetooth non abilitato");
                }
                break;
        }
    }

    /**
     * Gestione dei risultati della richiesta dei permessi
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso alla posizione concesso");
                } else {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso alla posizione non concesso");
                }
                break;
        }
    }

    /**
     * Metodo per l'elaborazione degli EPC e degli RSSI
     */
    private void getSourcesTag(CallbackContext callbackContext) throws CAENRFIDException {
        CAENRFIDLogicalSource[] sourcesTag = r.GetSources();
        CAENRFIDTag[] tags = sourcesTag[0].InventoryTag();
        Log.d("MyBluetoothPlugin", "Questo è l'array di tags: " + Arrays.toString(tags));
        if(tags == null || tags.length == 0){
            callbackContext.error("Nessun tag nelle vicinanze");
            Log.d("MyBluetoothPlugin", "Nessun tag nelle vicinanze");
            return;
        }
        else{
            ArrayList<String> estratti = new ArrayList<>();
            for (CAENRFIDTag tag : tags){
                byte[] epc = tag.GetId();
                Log.d("MyBluetoothPlugin", "Questo è l'array di byte (EPC): " + Arrays.toString(epc));
                StringBuilder hex_number = new StringBuilder();
                for (byte b : epc){
                    hex_number.append(String.format("%02X", b));
                }
                Log.d("MyBluetoothPlugin", "Questa è la stringa in hex " + hex_number);
                StringBuilder asciiString = new StringBuilder();
                for(int i = 0; i < hex_number.length(); i+=2){
                    String hexChar = hex_number.substring(i, i + 2);
                    asciiString.append((char) Integer.parseInt(hexChar, 16));
                }
                Log.d("MyBluetoothPlugin", "Questo è la stringa in ascii: " + asciiString);
                String rssi = String.valueOf(tag.GetRSSI());
                estratti.add(String.valueOf(asciiString));
                estratti.add(rssi);
            }
            JSONArray jsonArray = new JSONArray(estratti);
            callbackContext.success(jsonArray);
            Log.d("MyBluetoothPlugin", "Ho estratto questi tag [EPC, RSSI] (i primi 2 valori corrispondono ad un tag e così via): " + estratti);
        }
    }

    /**
     * Metodo per avviare il controllo periodico dei tag
     */
    private void startTagCheck(CallbackContext callbackContext) {

        tagCheckHandler = new Handler();
        callbackContext.success("Scansione avviata");
        tagCheckRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    getSourcesTag(callbackContext);
                } catch (CAENRFIDException e) {
                    callbackContext.error("Errore nell'avvio della scansione");
                    throw new RuntimeException(e);
                }
                tagCheckHandler.postDelayed(this, 5000); // Esegue ogni 5 secondi
            }
        };
        tagCheckHandler.post(tagCheckRunnable);
    }

    /**
     * Metodo per fermare il controllo periodico dei tag
     */
    private void stopTagCheck(CallbackContext callbackContext) {
        if (tagCheckHandler != null) {
            tagCheckHandler.removeCallbacks(tagCheckRunnable);
            tagCheckHandler = null;
            tagCheckRunnable = null;
            callbackContext.success("Scansione Terminata");
        }
        else{
            callbackContext.error("Errore nel terminare la scansione");
        }
    }
}
