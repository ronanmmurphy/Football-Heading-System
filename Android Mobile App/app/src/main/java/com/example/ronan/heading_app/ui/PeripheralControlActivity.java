package com.example.ronan.heading_app.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ronan.heading_app.Constants;
import com.example.ronan.heading_app.R;
import com.example.ronan.heading_app.bluetooth.BleAdapterService;
import com.example.ronan.heading_app.file.FileOperation;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import androidx.annotation.RequiresApi;

public class PeripheralControlActivity extends Activity {
    public static final String EXTRA_NAME_1 = "name1";
    public static final String EXTRA_ID_1 = "id1";
    public static final String EXTRA_NAME_2 = "name2";
    public static final String EXTRA_ID_2 = "id2";

    private FileWriter writer_1;
    private File currentLog_1;
    private File previousLog_1;

    private FileWriter writer_2;
    private File currentLog_2;
    private File previousLog_2;

    private String device_name_1;
    private String device_name_2;
    private String device_address_1;
    private String device_address_2;

    public int ard_voltage_max = 3300;
    public int ard_voltage_min = 0;
    public float ard_g_max = 3;
    public float ard_g_min = -3;

    private Timer mTimer;
    private boolean back_requested = false;
    private BleAdapterService bluetooth_le_adapter;
    private String titles_1 = "Timestamp"+ "," +"fsrLEFT" + "," + "fsrMIDDLE" + "," + "fsrRIGHT" + "," + "xpin" + "," +
            "ypin" + "," + "zpin" + "," + "ax" + "," + "ay" + "," + "az" + "," +"\n";

    private String titles_2 = "Timestamp"+ "," + "bx" + "," + "by" + "," + "bz" + "," +"\n";

    private final ServiceConnection service_connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler_1(message_handler_1);
            bluetooth_le_adapter.setActivityHandler_2(message_handler_2);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler message_handler_1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            String characteristic_uuid;
            byte[] b;


            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showToast(text);
                    break;

                case BleAdapterService.GATT_CONNECTED:
                    ((Button) PeripheralControlActivity.this.findViewById(R.id.connectButton)).setEnabled(false);
                    // we're connected
                    bluetooth_le_adapter.discoverServices_1();
                    break;

                case BleAdapterService.GATT_DISCONNECT:
                    ((Button) PeripheralControlActivity.this.findViewById(R.id.connectButton)).setEnabled(true);
                    ((Button) PeripheralControlActivity.this.findViewById(R.id.startbutton)).setEnabled(false);
                    ((Button) PeripheralControlActivity.this.findViewById(R.id.stopbutton)).setEnabled(false);
                    // stop the rssi reading timer
                    stopTimer();
                    if (back_requested) {
                        PeripheralControlActivity.this.finish();
                    }
                    break;

                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    // validate services and if ok....
                    List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices_1();
                    boolean heading_service = false;

                    for (BluetoothGattService svc : slist) {
                        Log.d(Constants.TAG, "UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
                        if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.HEAD_ARD_SERVICE)) {
                            heading_service = true;
                        }
                    }

                    if (heading_service) {
                        Toast.makeText(getApplicationContext(), "Device has expected services", Toast.LENGTH_SHORT).show();
                        ((Button) PeripheralControlActivity.this.findViewById(R.id.startbutton)).setEnabled(true);

                        if (bluetooth_le_adapter != null && bluetooth_le_adapter.isConnected()) {
                            if (!bluetooth_le_adapter.setIndicationsState_1(BleAdapterService.HEAD_ARD_SERVICE, BleAdapterService.TRANSFER_CHARACTERISTIC, true)) {
                                System.out.println("Subscribe failed");
                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Device does not have expected GATT services", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case BleAdapterService.GATT_CHARACTERISTIC_READ:

                    break;

                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:

                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);


                    if (characteristic_uuid.equalsIgnoreCase((BleAdapterService.TRANSFER_CHARACTERISTIC))) {
                        int timestamp = byteArrayToInt(b[0], b[1]);

                        float fsrLEFT = map(byteArrayToInt(b[2], b[3]), ard_voltage_min, ard_voltage_max, 0,ard_g_max);
                        float fsrMIDDLE = map(byteArrayToInt(b[4], b[5]), ard_voltage_min, ard_voltage_max, 0,ard_g_max);
                        float fsrRIGHT = map(byteArrayToInt(b[6], b[7]), ard_voltage_min, ard_voltage_max, 0,ard_g_max);

                        float xpin = map(byteArrayToInt(b[8], b[9]), ard_voltage_min, ard_voltage_max, ard_g_min,ard_g_max);
                        float ypin = map(byteArrayToInt(b[10], b[11]), ard_voltage_min, ard_voltage_max, ard_g_min,ard_g_max);
                        float zpin = map(byteArrayToInt(b[12], b[13]), ard_voltage_min, ard_voltage_max, ard_g_min,ard_g_max);

                        float ax = map(byteArrayToInt(b[14], b[15]), 0, 64000, -2,2);
                        float ay = map(byteArrayToInt(b[16], b[17]), 0, 64000, -2,2);
                        float az = map(byteArrayToInt(b[18], b[19]), 0, 64000, -2,2);



                        String line =
                                timestamp+ "," +fsrLEFT + "," + fsrMIDDLE + "," + fsrRIGHT + "," + xpin + "," +
                                        ypin + "," + zpin + "," + ax + "," + ay + "," + az + "," +"\n";

                        if (currentLog_1 != null) {
                            try {
                                writer_1.append(line);
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        }
                    }


                    break;
            }
        }
    };


    @SuppressLint("HandlerLeak")
    private Handler message_handler_2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            String characteristic_uuid;
            byte[] b;


            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showToast(text);
                    break;

                case BleAdapterService.GATT_CONNECTED:
                    // we're connected
                    bluetooth_le_adapter.discoverServices_2();
                    break;

                case BleAdapterService.GATT_DISCONNECT:

                    break;

                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    // validate services and if ok....
                    List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices_2();
                    boolean heading_service = false;

                    for (BluetoothGattService svc : slist) {
                        Log.d(Constants.TAG, "UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
                        if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.HEAD_ARD_SERVICE)) {
                            heading_service = true;
                        }
                    }

                    if (heading_service) {
                        Toast.makeText(getApplicationContext(), "Device has expected services", Toast.LENGTH_SHORT).show();

                        if (bluetooth_le_adapter != null && bluetooth_le_adapter.isConnected()) {
                            if (!bluetooth_le_adapter.setIndicationsState_2(BleAdapterService.HEAD_ARD_SERVICE, BleAdapterService.TRANSFER_CHARACTERISTIC, true)) {
                                System.out.println("Subscribe failed");
                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Device does not have expected GATT services", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case BleAdapterService.GATT_CHARACTERISTIC_READ:

                    break;

                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:

                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);


                    if (characteristic_uuid.equalsIgnoreCase((BleAdapterService.TRANSFER_CHARACTERISTIC))) {
                        int timestamp_2 = byteArrayToInt(b[0], b[1]);
                        float bx = map(byteArrayToInt(b[2], b[3]), 0,  64000, -2,2);
                        float by = map(byteArrayToInt(b[4], b[5]), 0,  64000, -2,2);
                        float bz = map(byteArrayToInt(b[6], b[7]), 0,  64000, -2,2);


                        String line = timestamp_2 + "," + bx + "," + by + "," + bz + "\n";

                        if (currentLog_2 != null) {
                            try {
                                writer_2.append(line);
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        }
                    }


                    break;
            }
        }
    };



    public void onSTART(View view) {
        ((Button) PeripheralControlActivity.this.findViewById(R.id.startbutton)).setEnabled(false);
        ((Button) PeripheralControlActivity.this.findViewById(R.id.stopbutton)).setEnabled(true);
        if(currentLog_1 == null) {
            try {
                currentLog_1 = FileOperation.createLogFile("Head Data");
                writer_1 = new FileWriter(currentLog_1);
                writer_1.append(titles_1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(currentLog_2 == null) {
            try {
                currentLog_2 = FileOperation.createLogFile("Ball Data");
                writer_2 = new FileWriter(currentLog_2);
                writer_2.append(titles_2);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bluetooth_le_adapter.writeCharacteristic_1(BleAdapterService.HEAD_ARD_SERVICE, BleAdapterService.RUN_CHARACTERISTIC, Constants.RUN_RUN);
        bluetooth_le_adapter.writeCharacteristic_2(BleAdapterService.HEAD_ARD_SERVICE, BleAdapterService.RUN_CHARACTERISTIC, Constants.RUN_RUN);
    }

    public void onSTOP(View view) throws IOException {
        ((Button) PeripheralControlActivity.this.findViewById(R.id.stopbutton)).setEnabled(false);
        bluetooth_le_adapter.writeCharacteristic_1(BleAdapterService.HEAD_ARD_SERVICE, BleAdapterService.RUN_CHARACTERISTIC, Constants.RUN_STOP);
        bluetooth_le_adapter.writeCharacteristic_2(BleAdapterService.HEAD_ARD_SERVICE, BleAdapterService.RUN_CHARACTERISTIC, Constants.RUN_STOP);
        try {
            writer_1.flush();
            writer_1.close();
            previousLog_1 = currentLog_1;
            currentLog_1 = null;

            writer_2.flush();
            writer_2.close();
            previousLog_2 = currentLog_2;
            currentLog_2 = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((Button) PeripheralControlActivity.this.findViewById(R.id.startbutton)).setEnabled(true);
    }




    public void onDisconnect(View view){
        onBackPressed();
    }

    public void onANALYSE(View view) {
        Intent intent = new Intent(PeripheralControlActivity.this, GraphActivity.class);
        intent.putExtra(GraphActivity.EXTRA_FILE, previousLog_1.toString());
        intent.putExtra(GraphActivity.EXTRA_FILE_1, previousLog_2.toString());
        startActivity(intent);

    }






    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        back_requested = true;
        if (bluetooth_le_adapter.isConnected()) {
            try {
                bluetooth_le_adapter.disconnect_1();
            } catch (Exception e) { }
            try{
                bluetooth_le_adapter.disconnect_2();
            } catch (Exception e) { }
        } else {
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral_control);

        // read intent data
        final Intent intent = getIntent();
        device_name_1 = intent.getStringExtra(EXTRA_NAME_1);
        device_address_1 = intent.getStringExtra(EXTRA_ID_1);

        device_name_2 = intent.getStringExtra(EXTRA_NAME_2);
        device_address_2 = intent.getStringExtra(EXTRA_ID_2);

        ((Button) this.findViewById(R.id.startbutton)).setEnabled(false);
        ((Button) this.findViewById(R.id.stopbutton)).setEnabled(false);

        // connect to the Bluetooth adapter service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, service_connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        unbindService(service_connection);
        bluetooth_le_adapter = null;
    }

    private void showToast(final String msg) {
        Log.d(Constants.TAG, msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    public void onConnect(View view) {
        if (bluetooth_le_adapter != null) {
            if (bluetooth_le_adapter.connect_1(device_address_1)) {
                ((Button) PeripheralControlActivity.this.findViewById(R.id.connectButton)).setEnabled(false);
            } else {
                showToast("onConnect: failed to connect to device 1");
            }
            if (bluetooth_le_adapter.connect_2(device_address_2)) {
            } else {
                showToast("onConnect: failed to connect to device 2");
            }
        } else {
            showToast("onConnect: bluetooth_le_adapter=null");
        }
    }


    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public String byteArrayAsHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        int l = bytes.length;
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < l; i++) {
            if ((bytes[i] >= 0) & (bytes[i] < 16))
                hex.append("0");
            hex.append(Integer.toString(bytes[i] & 0xff, 16).toUpperCase());
        }
        return hex.toString();
    }


    private int byteArrayToInt(byte byte1, byte byte2) {
        return byte2 & 0xFF | (byte1 & 0xFF) << 8;
    }
    public float map(int x, int in_min, int in_max, float out_min, float out_max){
        return ((float)x - (float)in_min) * (out_max - out_min) / ((float)in_max - (float)in_min) + out_min;
    }
}
