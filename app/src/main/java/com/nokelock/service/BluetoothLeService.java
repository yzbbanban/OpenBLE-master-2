package com.nokelock.service;

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
import android.os.IBinder;
import android.util.Log;

import com.nokelock.utils.HexUtils;
import com.nokelock.utils.SampleGattAttributes;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 蓝牙服务类
 * Created by sunshine on 2017/3/2.
 */

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic write_characteristic;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {// 连接成功
                mBluetoothGatt.discoverServices();
                broadcastUpdate(SampleGattAttributes.ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {// 断开连接
                gatt.close();
                broadcastUpdate(SampleGattAttributes.ACTION_GATT_DISCONNECTED);
            }
        }

        /**
         * 发现服务
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //获取服务的对象并判断是否为空
                BluetoothGattService service = gatt.getService(SampleGattAttributes.bltServerUUID);
                if (null == service) return;
                //获取写入特征值和通知特征值的对象并判断是否为空
                BluetoothGattCharacteristic read_characteristic = service.getCharacteristic(SampleGattAttributes.readDataUUID);
                write_characteristic = service.getCharacteristic(SampleGattAttributes.writeDataUUID);
                if (null == read_characteristic || null == write_characteristic) return;
                //开启通知属性
                int properties = read_characteristic.getProperties();
                if ((properties | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    gatt.setCharacteristicNotification(read_characteristic, true);
                    BluetoothGattDescriptor descriptor = read_characteristic.getDescriptor(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
                    if (null != descriptor) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
                //发送广播通知前端
                broadcastUpdate(SampleGattAttributes.ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * 通知的回调
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(SampleGattAttributes.ACTION_BLE_REAL_DATA, HexUtils.bytesToHexString(characteristic.getValue()));
        }
    };

    /**
     * 发送广播
     *
     * @param action 广播的Action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        intent.putExtra("address", mBluetoothDeviceAddress);
        sendBroadcast(intent);
    }

    /**
     * 发送广播
     *
     * @param action 广播的Action
     * @param data   要携带的数据
     */
    private void broadcastUpdate(String action, String data) {
        Intent intent = new Intent(action);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }


    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initBluetooth() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (null == mBluetoothAdapter) return false;
//        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
//        }
        return true;
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * 蓝牙链接
     *
     * @param address 执行链接的MAC地址
     * @return 是否连接成功
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * 断开
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }


    /**
     * 关闭蓝牙，并置空控制器
     */
    public void close() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 写入指令的方法
     *
     * @param bytes 写入指令
     */
    public boolean writeCharacteristic(byte[] bytes) {
        if (mBluetoothGatt == null || write_characteristic == null) {
            return false;
        }
        byte[] miWen = Encrypt(bytes, SampleGattAttributes.key);
        if (miWen != null) {
            write_characteristic.setValue(miWen);
            Log.e(BluetoothLeService.class.getSimpleName(),"bytes:"+HexUtils.bytesToHexString(bytes));
            return mBluetoothGatt.writeCharacteristic(write_characteristic);
        }
        return false;
    }

    /**
     * 写入指令的方法
     *
     * @param bytes 写入指令
     */
    public boolean writeCharacteristic2(byte[] bytes) {
        if (mBluetoothGatt == null || write_characteristic == null) {
            return false;
        }
//        byte[] miWen = Encrypt(bytes, SampleGattAttributes.key);
//        if (miWen != null) {
            write_characteristic.setValue(bytes);
            Log.e(BluetoothLeService.class.getSimpleName(),"bytes:"+HexUtils.bytesToHexString(bytes));
            return mBluetoothGatt.writeCharacteristic(write_characteristic);
//        }
//        return false;
    }

    // 加密
    public static byte[] Encrypt(byte[] sSrc, byte[] sKey) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(sKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc);
            return encrypted;//此处使用BASE64做转码功能，同时能起到2次加密的作用。
        } catch (Exception ex) {
            return null;
        }
    }

    // 解密
    public static byte[] Decrypt(byte[] sSrc, byte[] sKey) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(sKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] dncrypted = cipher.doFinal(sSrc);
            return dncrypted;
        } catch (Exception ex) {
            return null;
        }
    }
}
