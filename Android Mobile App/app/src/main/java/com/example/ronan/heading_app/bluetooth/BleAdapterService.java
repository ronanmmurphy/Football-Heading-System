package com.example.ronan.heading_app.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.ronan.heading_app.Constants;

import java.util.List;
import java.util.UUID;

public class BleAdapterService extends Service {

    private BluetoothAdapter bluetooth_adapter;
    private BluetoothGatt bluetooth_gatt_1;
    private BluetoothGatt bluetooth_gatt_2;
    private BluetoothManager bluetooth_manager;
    private Handler activity_handler_1 = null;
    private Handler activity_handler_2 = null;
    private BluetoothDevice device;
    private BluetoothGattDescriptor descriptor;
    private final IBinder binder = new LocalBinder();

    public boolean isConnected() {
        return connected;
    }

    private boolean connected = false;

    // messages sent back to activity
    public static final int GATT_CONNECTED = 1;
    public static final int GATT_DISCONNECT = 2;
    public static final int GATT_SERVICES_DISCOVERED = 3;
    public static final int GATT_CHARACTERISTIC_READ = 4;
    public static final int GATT_CHARACTERISTIC_WRITTEN = 5;
    public static final int GATT_REMOTE_RSSI = 6;
    public static final int MESSAGE = 7;
    public static final int NOTIFICATION_OR_INDICATION_RECEIVED = 8;

    // message parms
    public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
    public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
    public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
    public static final String PARCEL_VALUE = "VALUE";
    public static final String PARCEL_RSSI = "RSSI";
    public static final String PARCEL_TEXT = "TEXT";

    // service uuids
    public static String HEAD_ARD_SERVICE = "19B10000-E8F2-537E-4F6C-D104768A1214";


    // service characteristics
    public static String RUN_CHARACTERISTIC = "19B10001-E8F2-537E-4F6C-D104768A1214";
    public static String TRANSFER_CHARACTERISTIC = "19b10002-e8f2-537e-4f6c-d104768a1214";


    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    public class LocalBinder extends Binder {
        public BleAdapterService getService() {
            return BleAdapterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }



    // set activity the will receive the messages
    public void setActivityHandler_1(Handler handler) {
        activity_handler_1 = handler;
    }

    public void setActivityHandler_2(Handler handler) {
        activity_handler_2 = handler;
    }




    private void sendConsoleMessage_1(String text) {
        Message msg = Message.obtain(activity_handler_1, MESSAGE);
        Bundle data = new Bundle();
        data.putString(PARCEL_TEXT, text);
        msg.setData(data);
        msg.sendToTarget();
    }

    private void sendConsoleMessage_2(String text) {
        Message msg = Message.obtain(activity_handler_2, MESSAGE);
        Bundle data = new Bundle();
        data.putString(PARCEL_TEXT, text);
        msg.setData(data);
        msg.sendToTarget();
    }



    @Override
    public void onCreate() {
        if (bluetooth_manager == null) {
            bluetooth_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetooth_manager == null) {
                return;
            }
        }
        bluetooth_adapter = bluetooth_manager.getAdapter();
        if (bluetooth_adapter == null) {
            return;
        }
    }



    // connect_1 to the device
    public boolean connect_1(final String address) {

        if (bluetooth_adapter == null || address == null) {
            sendConsoleMessage_1("connect_1: bluetooth_adapter=null");
            return false;
        }

        device = bluetooth_adapter.getRemoteDevice(address);
        if (device == null) {
            sendConsoleMessage_1("connect_1: device=null");
            return false;
        }

        bluetooth_gatt_1 = device.connectGatt(this, false, gatt_callback_1);
        return true;
    }

    // connect_2 to the device
    public boolean connect_2(final String address) {

        if (bluetooth_adapter == null || address == null) {
            sendConsoleMessage_2("connect_2: bluetooth_adapter=null");
            return false;
        }

        device = bluetooth_adapter.getRemoteDevice(address);
        if (device == null) {
            sendConsoleMessage_2("connect_2: device=null");
            return false;
        }

        bluetooth_gatt_2 = device.connectGatt(this, false, gatt_callback_2);
        return true;
    }




    // disconnect_1 from device
    public void disconnect_1() {
        sendConsoleMessage_1("disconnecting");
        if (bluetooth_adapter == null || bluetooth_gatt_1 == null) {
            sendConsoleMessage_1("disconnect_1: bluetooth_adapter|bluetooth_gatt_1 null");
            return;
        }

        bluetooth_gatt_1.disconnect();
    }

    // disconnect_2 from device
    public void disconnect_2() {
        sendConsoleMessage_2("disconnecting");
        if (bluetooth_adapter == null || bluetooth_gatt_2 == null) {
            sendConsoleMessage_2("disconnect_2: bluetooth_adapter|bluetooth_gatt_2 null");
            return;
        }

        bluetooth_gatt_2.disconnect();

    }





    public void discoverServices_1() {
        if (bluetooth_adapter == null || bluetooth_gatt_1 == null) {
            return;
        }
        Log.d(Constants.TAG, "Discovering GATT services on device 1");
        bluetooth_gatt_1.discoverServices();
    }

    public void discoverServices_2() {
        if (bluetooth_adapter == null || bluetooth_gatt_2 == null) {
            return;
        }
        Log.d(Constants.TAG, "Discovering GATT services on device 2");
        bluetooth_gatt_2.discoverServices();
    }




    public List<BluetoothGattService> getSupportedGattServices_1() {
        if (bluetooth_gatt_1 == null)
            return null;
        return bluetooth_gatt_1.getServices();
    }

    public List<BluetoothGattService> getSupportedGattServices_2() {
        if (bluetooth_gatt_2 == null)
            return null;
        return bluetooth_gatt_2.getServices();
    }





    public boolean readCharacteristic_1(String serviceUuid, String characteristicUuid) {
        Log.d(Constants.TAG, "readCharacteristic_1:" + characteristicUuid + " of " + serviceUuid);
        if (bluetooth_adapter == null || bluetooth_gatt_1 == null) {
            sendConsoleMessage_1("readCharacteristic_1: bluetooth_adapter|bluetooth_gatt_1 null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt_1.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage_1("readCharacteristic_1: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService
                .getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage_1("readCharacteristic_1: gattChar null");
            return false;
        }
        return bluetooth_gatt_1.readCharacteristic(gattChar);
    }

    public boolean readCharacteristic_2(String serviceUuid, String characteristicUuid) {
        Log.d(Constants.TAG, "readCharacteristic_2:" + characteristicUuid + " of " + serviceUuid);
        if (bluetooth_adapter == null || bluetooth_gatt_2 == null) {
            sendConsoleMessage_2("readCharacteristic_2: bluetooth_adapter|bluetooth_gatt_2 null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt_2.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage_2("readCharacteristic_2: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService
                .getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage_2("readCharacteristic_2: gattChar null");
            return false;
        }
        return bluetooth_gatt_2.readCharacteristic(gattChar);
    }





    public boolean writeCharacteristic_1(String serviceUuid, String characteristicUuid, byte[] value) {

        Log.d(Constants.TAG, "writeCharacteristic_1:" + characteristicUuid + " of " + serviceUuid);
        if (bluetooth_adapter == null || bluetooth_gatt_1 == null) {
            sendConsoleMessage_1("writeCharacteristic_1: bluetooth_adapter|bluetooth_gatt_1 null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt_1.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage_1("writeCharacteristic_1: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage_1("writeCharacteristic_1: gattChar null");
            return false;
        }
        gattChar.setValue(value);

        return bluetooth_gatt_1.writeCharacteristic(gattChar);

    }


    public boolean writeCharacteristic_2(String serviceUuid, String characteristicUuid, byte[] value) {

        Log.d(Constants.TAG, "writeCharacteristic_2:" + characteristicUuid + " of " + serviceUuid);
        if (bluetooth_adapter == null || bluetooth_gatt_2 == null) {
            sendConsoleMessage_2("writeCharacteristic_2: bluetooth_adapter|bluetooth_gatt_2 null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt_2.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage_2("writeCharacteristic_2: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage_2("writeCharacteristic_2: gattChar null");
            return false;
        }
        gattChar.setValue(value);

        return bluetooth_gatt_2.writeCharacteristic(gattChar);

    }





    public boolean setIndicationsState_1(String serviceUuid, String characteristicUuid, boolean enabled) {

        if (bluetooth_adapter == null || bluetooth_gatt_1 == null) {
            sendConsoleMessage_1("setIndicationsState_1: bluetooth_adapter|bluetooth_gatt_1 null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt_1.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage_1("setIndicationsState_1: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage_1("setIndicationsState_1: gattChar null");
            return false;
        }

        bluetooth_gatt_1.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        descriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        boolean ok = bluetooth_gatt_1.writeDescriptor(descriptor);

        return ok;
    }

    public boolean setIndicationsState_2(String serviceUuid, String characteristicUuid, boolean enabled) {

        if (bluetooth_adapter == null || bluetooth_gatt_2 == null) {
            sendConsoleMessage_2("setIndicationsState_2: bluetooth_adapter|bluetooth_gatt_2 null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt_2.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage_2("setIndicationsState_2: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage_2("setIndicationsState_2: gattChar null");
            return false;
        }

        bluetooth_gatt_2.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        descriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        boolean ok = bluetooth_gatt_2.writeDescriptor(descriptor);

        return ok;
    }






    private final BluetoothGattCallback gatt_callback_1 = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(Constants.TAG, "onConnectionStateChange: status=" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(Constants.TAG, "onConnectionStateChange: CONNECTED");
                connected = true;
                Message msg = Message.obtain(activity_handler_1, GATT_CONNECTED);
                msg.sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(Constants.TAG, "onConnectionStateChange: DISCONNECTED");
                Message msg = Message.obtain(activity_handler_1, GATT_DISCONNECT);
                msg.sendToTarget();
                if (bluetooth_gatt_1 != null) {
                    Log.d(Constants.TAG, "Closing and destroying BluetoothGatt object");
                    connected = false;
                    bluetooth_gatt_1.close();
                    bluetooth_gatt_1 = null;
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendConsoleMessage_1("RSSI read OK");
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_RSSI, rssi);
                Message msg = Message.obtain(activity_handler_1, GATT_REMOTE_RSSI);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage_1("RSSI read err:" + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            sendConsoleMessage_1("Services Discovered");
            Message msg = Message.obtain(activity_handler_1, GATT_SERVICES_DISCOVERED);
            msg.sendToTarget();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler_1, GATT_CHARACTERISTIC_READ);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                Log.d(Constants.TAG, "failed to read characteristic:" + characteristic.getUuid().toString()
                        + " of service " + characteristic.getService().getUuid().toString() + " : status=" + status);
                sendConsoleMessage_1("characteristic read err:" + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Bundle bundle = new Bundle();
            bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
            bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
            bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
            // notifications and indications are both communicated from here in this way
            Message msg = Message.obtain(activity_handler_1, NOTIFICATION_OR_INDICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

        }


        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Constants.TAG, "onCharacteristicWrite");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler_1, GATT_CHARACTERISTIC_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage_1("characteristic write err:" + status);
            }
        }
    };


    private final BluetoothGattCallback gatt_callback_2 = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(Constants.TAG, "onConnectionStateChange: status=" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(Constants.TAG, "onConnectionStateChange: CONNECTED");
                connected = true;
                Message msg = Message.obtain(activity_handler_2, GATT_CONNECTED);
                msg.sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(Constants.TAG, "onConnectionStateChange: DISCONNECTED");
                Message msg = Message.obtain(activity_handler_2, GATT_DISCONNECT);
                msg.sendToTarget();
                if (bluetooth_gatt_2 != null) {
                    Log.d(Constants.TAG, "Closing and destroying BluetoothGatt object");
                    connected = false;
                    bluetooth_gatt_2.close();
                    bluetooth_gatt_2 = null;
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendConsoleMessage_2("RSSI read OK");
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_RSSI, rssi);
                Message msg = Message.obtain(activity_handler_2, GATT_REMOTE_RSSI);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage_2("RSSI read err:" + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            sendConsoleMessage_2("Services Discovered");
            Message msg = Message.obtain(activity_handler_2, GATT_SERVICES_DISCOVERED);
            msg.sendToTarget();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler_2, GATT_CHARACTERISTIC_READ);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                Log.d(Constants.TAG, "failed to read characteristic:" + characteristic.getUuid().toString()
                        + " of service " + characteristic.getService().getUuid().toString() + " : status=" + status);
                sendConsoleMessage_2("characteristic read err:" + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Bundle bundle = new Bundle();
            bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
            bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
            bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
            // notifications and indications are both communicated from here in this way
            Message msg = Message.obtain(activity_handler_2, NOTIFICATION_OR_INDICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

        }


        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(Constants.TAG, "onCharacteristicWrite");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler_2, GATT_CHARACTERISTIC_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage_2("characteristic write err:" + status);
            }
        }
    };

}
