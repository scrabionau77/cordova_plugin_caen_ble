//package android;
package com.maestrale.maecaenble;

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
import com.caen.RFIDLibrary.CAENRFIDEventListener;
import com.caen.RFIDLibrary.CAENRFIDNotify;

public class CaenBle extends CordovaPlugin {

    private static final int MY_PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_ENABLE_BT = 200;
    private static final int REQUEST_BT_PERMISSION = 201;
    private static final int REQUEST_BT_ADMIN_PERMISSION = 202;
    private static final int REQUEST_BT_SCAN_PERMISSION = 203;
    private static final int REQUEST_BT_CONNECT_PERMISSION = 204;
    private static final int REQUEST_LOCATION_PERMISSION = 205;
    UUID MY_CHARACTERISTIC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private CallbackContext callbackContext;
    private CallbackContext connectionCallbackContext;
    private CallbackContext callbackContextConnect;
    private BluetoothGatt gatt;
    private Queue<BluetoothGattCharacteristic> readQueue = new LinkedList<>();
    CAENRFIDReader r = new CAENRFIDReader();
    private Handler tagCheckHandler;
    private Runnable tagCheckRunnable;

    static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
        if (bytes == null)
            return "NULL";
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

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
        } else if ("stopDiscovery".equals(action)) {
            stopDiscovery(callbackContext);
            return true;
        } else if ("disconnectDevice".equals(action)) {
            disconnectDevice(callbackContext);
            return true;
        } else if ("requestPermissions".equals(action)) {
            requestPermissions();
            callbackContext.success("Permessi richiesti");
            return true;
        } else if (action.equals("startTagCheck")) {
            startTagCheck(callbackContext);
            return true;
        } else if (action.equals("stopTagCheck")) {
            stopTagCheck(callbackContext);
            return true;
        } else if ("registerConnectionCallback".equals(action)) {
            this.connectionCallbackContext = callbackContext;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } else if("checkBtOn".equals(action)){
            checkAndRequestBluetoothStatus();
            return true;
        }

        return false;
    }

    private void discoverDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN },
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        bluetoothLeScanner.startScan(scanCallback);
        Log.d("MyBluetoothPlugin", "Scansione iniziata...");
    }

    private void stopDiscovery(CallbackContext callbackContext) {
        if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN },
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
     * La scanCallback prende il nome e l'address dei dispositivi
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            JSONObject deviceInfo = new JSONObject();
            try {
                if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(),
                                Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(),
                                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(),
                                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(cordova.getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN },
                            MY_PERMISSIONS_REQUEST_CODE);
                }
                deviceInfo.put("name", device.getName());
                deviceInfo.put("address", device.getAddress());
                // callbackContext.success(deviceInfo);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, deviceInfo);
                pluginResult.setKeepCallback(true); // Mantieni la callback per ulteriori risultati
                callbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                // callbackContext.error("Error processing device info.");
                PluginResult pluginResult = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
                callbackContext.sendPluginResult(pluginResult);
            }
        }
    };

    /**
     * Metodo per la connessione al dispositivo skID
     */
    private void connectToDevice(String address) throws CAENRFIDException {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN },
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        gatt = device.connectGatt(cordova.getActivity(), false, gattCallback);
        if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { Manifest.permission.BLUETOOTH_CONNECT }, MY_PERMISSIONS_REQUEST_CODE);
        } else {
            gatt.discoverServices();
            // gatt.connect();
            r.Connect(cordova.getContext(), device);
            callbackContext.success("Connected");
            Log.d("MyBluetoothPlugin", "Connesso al dispositivo con indirizzo " + address);
        }
    }

    /**
     * La gattCallback viene usata per stampare lo stato della connessione con il
     * dispositivo
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(cordova.getActivity(),
                        new String[] { Manifest.permission.BLUETOOTH_CONNECT }, MY_PERMISSIONS_REQUEST_CODE);
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("MyBluetoothPlugin", "Connesso al dispositivo");
                Log.d("MyBluetoothPlugin", "Lo stato della connessione PRIMA del gatt.discoverServices è " + status);
                gatt.discoverServices();
                Log.d("MyBluetoothPlugin", "Lo stato della connessione DOPO del gatt.discoverServices è " + status);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("MyBluetoothPlugin", "Disconnesso dal dispositivo");
                Log.d("MyBluetoothPlugin", "Lo stato della connessione è " + status);
                sendConnectionStatus("Disconnected");
            }
        }

    };

    private void sendConnectionStatus(String status) {
        if (connectionCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, status);
            result.setKeepCallback(true);
            connectionCallbackContext.sendPluginResult(result);
        }
    }

    /**
     * Questi 2 metodi venivano utilizzati precedentemente per scoprire i servizi
     * che il
     * dispositivo offriva per lavolarli in maniera custom in attesa dell'sdk 5.0
     */
    private void readNextCharacteristic(BluetoothGatt gatt) {
        if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT },
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
                if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(),
                                Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        ||
                        ContextCompat.checkSelfPermission(cordova.getActivity(),
                                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(cordova.getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.BLUETOOTH_CONNECT },
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
        if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN },
                    MY_PERMISSIONS_REQUEST_CODE);
        }
        if (gatt != null) {
            try {
                gatt.disconnect();
                r.Disconnect();
                gatt.close();
                gatt = null;
                Log.d("MyBluetoothPlugin", "Disconnesso con successo!");
                callbackContext.success("Disconnesso con successo.");
            } catch (CAENRFIDException e) {
                throw new RuntimeException(e);
            }
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
        Log.d("MyBluetoothPlugin", "Sto provando a chiedere i permessi");

        if (ContextCompat.checkSelfPermission(cordova.getActivity(),
                Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        cordova.getActivity(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(cordova.getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(cordova.getActivity(),
                    new String[] { Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_CODE);
        }

    }

    /**
     * Metodo per capire se il bluetooth è attivato o no
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == cordova.getActivity().RESULT_OK) {
                callbackContext.success("Bluetooth abilitato con successo.");
            } else {
                callbackContext.error("Bluetooth non abilitato dall'utente.");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*
     * Metodo che controlla lo stato (acceso o spento) del bluetooth
     */
    private void checkAndRequestBluetoothStatus() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            callbackContext.error("Bluetooth non supportato su questo dispositivo.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            cordova.startActivityForResult(this, enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            callbackContext.success("Bluetooth già abilitato.");
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
            case REQUEST_BT_PERMISSION:
                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso al BT concesso");
                } else {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso al BT non concesso");
                }
                break;
            case REQUEST_BT_ADMIN_PERMISSION:
                if (grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso al BT Admin concesso");
                } else {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso al BT Admin non concesso");
                }
                break;
            case REQUEST_BT_SCAN_PERMISSION:
                if (grantResults.length > 0 && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso al BT Scan concesso");
                } else {
                    Log.d("MyBluetoothPlugin", "Permesso di accesso al BT Scan non concesso");
                }
                break;
        }
    }

    private CallbackContext rfidCallbackContext;
    private Handler handler = new Handler();
    private boolean isScanning = false;

    /**
     * Metodo per l'elaborazione degli EPC e degli RSSI
     */
    private void getSourcesTag(CallbackContext callbackContext) throws CAENRFIDException {
        CAENRFIDLogicalSource[] sourcesTag = r.GetSources();
        CAENRFIDTag[] tags = sourcesTag[0].InventoryTag();
        Log.d("MyBluetoothPlugin", "Questo è l'array di tags: " + Arrays.toString(tags));
        if (tags == null || tags.length == 0) {
            Log.d("MyBluetoothPlugin", "Nessun tag nelle vicinanze");
        } else {
            Log.d("MyBluetoothPlugin", "Questa è la sua lunghezza " + tags.length);
            ArrayList<JSONObject> estratti = new ArrayList<>();
            for (CAENRFIDTag tag : tags) {
                byte[] epc = tag.GetId();
                Log.d("MyBluetoothPlugin", "Questo è l'array di byte (EPC): " + Arrays.toString(epc));
                StringBuilder hex_number = new StringBuilder();
                for (byte b : epc) {
                    hex_number.append(String.format("%02X", b));
                }
                Log.d("MyBluetoothPlugin", "Questa è la stringa in hex " + hex_number);
                StringBuilder asciiString = new StringBuilder();
                for (int i = 0; i < hex_number.length(); i += 2) {
                    String hexChar = hex_number.substring(i, i + 2);
                    asciiString.append((char) Integer.parseInt(hexChar, 16));
                }
                Log.d("MyBluetoothPlugin", "Questo è la stringa in ascii: " + asciiString);
                String rssi = String.valueOf(tag.GetRSSI());
                try {
                    JSONObject tagInfo = new JSONObject();
                    tagInfo.put("hex_number", hex_number.toString());
                    tagInfo.put("rssi", rssi);
                    estratti.add(tagInfo);
                } catch (JSONException e) {
                    Log.e("MyBluetoothPlugin", "Errore nella creazione dell'oggetto JSON", e);
                }
            }
            JSONArray jsonArray = new JSONArray(estratti);
            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonArray);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            // callbackContext.success(jsonArray);
            Log.d("MyBluetoothPlugin",
                    "Ho estratto questi tag [EPC, RSSI] (i primi 2 valori corrispondono ad un tag e così via): "
                            + estratti);
        }
    }

    CAENRFIDEventListener caenrfidEventListener = evt -> {
        Log.d("MyBluetoothPlugin", "Entro nel listener");
        CAENRFIDNotify tag = evt.getData().get(0);
        byte[] epc = tag.getTagID();

        StringBuilder hex_number = new StringBuilder();
        for (byte b : epc) {
            hex_number.append(String.format("%02X", b));
        }

        try {
            JSONObject tagInfo = new JSONObject();
            tagInfo.put("hex_number", hex_number.toString());
            tagInfo.put("rssi", String.valueOf(tag.getRSSI()));
            Log.d("MyBluetoothPlugin", "Questo è il mio tag: " + tagInfo);
            PluginResult result = new PluginResult(PluginResult.Status.OK, tagInfo);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        } catch (JSONException e) {
            Log.e("MyBluetoothPlugin", "Errore nella creazione dell'oggetto JSON", e);
        }
    };

    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                getSourcesTag(rfidCallbackContext);
            } catch (CAENRFIDException e) {
                rfidCallbackContext.error("Errore nella scansione dei tag");
            }

            if (isScanning) {
                handler.postDelayed(this, 500);
            }
        }
    };

    private void startTagCheck(CallbackContext callbackContext) {
        this.rfidCallbackContext = callbackContext;

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    isScanning = true;
                    handler.post(scanRunnable);

                    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } catch (Exception e) {
                    callbackContext.error("Errore nell'avvio della scansione");
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void stopTagCheck(CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    isScanning = false;
                    handler.removeCallbacks(scanRunnable);
                    callbackContext.success("Scanning stopped");
                } catch (Exception e) {
                    callbackContext.error("Errore nell'arresto della scansione");
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
