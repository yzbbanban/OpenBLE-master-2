package com.nokelock.nokelockble;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nokelock.service.BluetoothLeService;
import com.nokelock.utils.HexUtils;
import com.nokelock.utils.MPermissionsActivity;
import com.nokelock.utils.SampleGattAttributes;

public class LockManageActivity extends MPermissionsActivity implements View.OnClickListener {

    private byte[] token = new byte[4];
    private byte CHIP_TYPE;
    private byte DEV_TYPE;
    private TextView deviceName;
    private TextView deviceMac;
    private TextView deviceBattery;
    private TextView deviceVersion;
    private TextView deviceCz;
    private TextView deviceStatus;
    private TextView openCount;
    byte[] gettoken = {0x06, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] sendDataBytes = null;
    private ProgressDialog progressDialog;
    private boolean isAuto = false;
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_manage);
        registerReceiver(broadcastReceiver, SampleGattAttributes.makeGattUpdateIntentFilter());
        initWidget();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        App.getInstance().getBluetoothLeService().close();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        deviceName = (TextView) findViewById(R.id.tv_name);
        deviceMac = (TextView) findViewById(R.id.tv_address);
        deviceBattery = (TextView) findViewById(R.id.tv_battery);
        deviceVersion = (TextView) findViewById(R.id.tv_version);
        deviceCz = (TextView) findViewById(R.id.tv_cz);
        deviceStatus = (TextView) findViewById(R.id.tv_status);
        openCount = (TextView) findViewById(R.id.open_count);
        findViewById(R.id.bt_open).setOnClickListener(this);
        findViewById(R.id.bt_close).setOnClickListener(this);
        findViewById(R.id.bt_status).setOnClickListener(this);
        findViewById(R.id.bt_update_password).setOnClickListener(this);
        ((CheckBox) findViewById(R.id.bt_auto)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAuto = isChecked;
            }
        });
        String name = getIntent().getStringExtra("name");
        if (!TextUtils.isEmpty(name)) {
            deviceName.setText("Name：" + name);
        }
        String address = getIntent().getStringExtra("address");
        if (!TextUtils.isEmpty(address)) {
            progressDialog = ProgressDialog.show(this, null, "正在连接...");
            deviceMac.setText("Mac：" + address);
            App.getInstance().getBluetoothLeService().connect(address);
        }

    }

    /**
     * BLE通讯广播
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case SampleGattAttributes.ACTION_GATT_CONNECTED:
                    //链接
                    deviceStatus.setText("连接状态：已连接");
                    break;
                case SampleGattAttributes.ACTION_GATT_DISCONNECTED:
                    //断开
                    progressDialog.dismiss();
                    deviceStatus.setText("连接状态：已断开");
                    count = 0;
                    openCount.setText("开锁次数：" +count);
                    break;
                case SampleGattAttributes.ACTION_GATT_SERVICES_DISCOVERED:
                    //发现服务

                    handler.sendEmptyMessageDelayed(0, 2000);
                    break;
                case SampleGattAttributes.ACTION_BLE_REAL_DATA:
                    parseData(intent.getStringExtra("data"));
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    Log.e(LockManageActivity.class.getSimpleName(),"state_changed");
                    break;
            }
        }
    };

    /**
     * 解析锁反馈的指令
     *
     * @param value 反馈数据
     */
    private void parseData(String value) {
        byte[] values = HexUtils.hexStringToBytes(value);
        byte[] x = new byte[16];
        System.arraycopy(values, 0, x, 0, 16);
        byte[] decrypt = BluetoothLeService.Decrypt(x, SampleGattAttributes.key);
        String decryptString = HexUtils.bytesToHexString(decrypt).toUpperCase();
        Log.e(LockManageActivity.class.getSimpleName(),"value:"+decryptString);
        if (decryptString.startsWith("0602")) {//token
            if (decrypt != null && decrypt.length == 16) {
                if (decrypt[0] == 0x06 && decrypt[1] == 0x02) {
                    token[0] = decrypt[3];
                    token[1] = decrypt[4];
                    token[2] = decrypt[5];
                    token[3] = decrypt[6];
                    CHIP_TYPE = decrypt[7];
                    DEV_TYPE = decrypt[10];
                    deviceVersion.setText("当前版本："+Integer.parseInt(decryptString.substring(16, 18), 16) + "." + Integer.parseInt(decryptString.substring(18, 20), 16));
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }
            handler.sendEmptyMessage(1);
        } else if (decryptString.startsWith("0202")) {//电量
            progressDialog.dismiss();
            if (decryptString.startsWith("020201ff")) {
                deviceCz.setText("获取电量失败");
            } else {
                String battery = decryptString.substring(6, 8);
                deviceBattery.setText("当前电量：" + Integer.parseInt(battery, 16));
            }
        } else if (decryptString.startsWith("0502")) {//开锁
            if (decryptString.startsWith("05020101")) {
                deviceCz.setText("开锁失败");
            } else {
                count++;
                deviceCz.setText("开锁成功");
                openCount.setText("开锁次数：" +count);
            }
        } else if (decryptString.startsWith("050F")) {//锁状态
            if (decryptString.startsWith("050F0101")) {
                deviceCz.setText("当前操作：锁已关闭");
            } else {
                deviceCz.setText("当前操作：锁已开启");
            }
        } else if (decryptString.startsWith("050D")) {//复位
            if (decryptString.startsWith("050D0101")) {
                deviceCz.setText("当前操作：复位失败");
            } else {
                deviceCz.setText("当前操作：复位成功");
            }
        }else if (decryptString.startsWith("0508")){//上锁
            if (decryptString.startsWith("05080101")){
                deviceCz.setText("当前操作：上锁失败");
            }else {
                deviceCz.setText("当前操作：上锁成功");
                if (isAuto){
                    handler.sendEmptyMessageDelayed(2,1000);
                }

            }
        }else if (decryptString.startsWith("0505")){
            if (decryptString.startsWith("05050101")){
                deviceCz.setText("当前操作：修改密码失败");
            }else {
                deviceCz.setText("当前操作：修改密码成功");
            }
        }else if (decryptString.startsWith("CB0503")){
            App.getInstance().getBluetoothLeService().writeCharacteristic(new byte[]{0x05, 0x04, 0x06,SampleGattAttributes.password[0],SampleGattAttributes.password[1],SampleGattAttributes.password[2],SampleGattAttributes.password[3],SampleGattAttributes.password[4],SampleGattAttributes.password[5], token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00});
        }
    }


    @Override
    public void onClick(View v) {
        sendDataBytes = null;
        switch (v.getId()) {
            case R.id.bt_open://开锁
                sendDataBytes = new byte[]{0x05, 0x01, 0x06, SampleGattAttributes.password[0],SampleGattAttributes.password[1],SampleGattAttributes.password[2],SampleGattAttributes.password[3],SampleGattAttributes.password[4],SampleGattAttributes.password[5],   token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00};
                App.getInstance().getBluetoothLeService().writeCharacteristic(sendDataBytes);

                break;
            case R.id.bt_status://获取锁状态
                sendDataBytes = new byte[]{0x05, 0x0E, 0x01, 0X01, token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                App.getInstance().getBluetoothLeService().writeCharacteristic(sendDataBytes);

                break;
            case R.id.bt_close://复位
                sendDataBytes = new byte[]{0x05, 0x0c, 0x01, 0x01, token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                App.getInstance().getBluetoothLeService().writeCharacteristic(sendDataBytes);

                break;
            case R.id.bt_update_password://修改密码
                App.getInstance().getBluetoothLeService().writeCharacteristic(new byte[]{0x05, 0x03, 0x06,SampleGattAttributes.password[0],SampleGattAttributes.password[1],SampleGattAttributes.password[2],SampleGattAttributes.password[3],SampleGattAttributes.password[4],SampleGattAttributes.password[5],  token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00});

                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    App.getInstance().getBluetoothLeService().writeCharacteristic(gettoken);
                    break;
                case 1://获取电量
                    byte[] batteryBytes = {0x02, 0x01, 0x01, 0x01, token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    App.getInstance().getBluetoothLeService().writeCharacteristic(batteryBytes);
                    break;
                case 2://开锁
                    sendDataBytes = new byte[]{0x05, 0x01, 0x06, SampleGattAttributes.password[0], SampleGattAttributes.password[1], SampleGattAttributes.password[2], SampleGattAttributes.password[3], SampleGattAttributes.password[4], SampleGattAttributes.password[5], token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00};
                    App.getInstance().getBluetoothLeService().writeCharacteristic(sendDataBytes);
                    break;
            }
        }
    };
}
