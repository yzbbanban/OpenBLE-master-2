package com.nokelock.activity;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.common.StringUtils;
import com.nokelock.app.App;
import com.nokelock.bean.ResultCode;
import com.nokelock.constant.ExtraConstant;
import com.nokelock.constant.Url;
import com.nokelock.nokelockble.R;
import com.nokelock.service.BluetoothLeService;
import com.nokelock.service.SendCodeService;
import com.nokelock.ui.CleanEditText;
import com.nokelock.utils.HexUtils;
import com.nokelock.utils.SampleGattAttributes;
import com.nokelock.utils.ToastUtil;
import com.nokelock.utils.retrofit.MyCallback;
import com.nokelock.utils.retrofit.RetrofitUtils;

import retrofit2.Call;
import retrofit2.Response;

public class LockManageActivity extends MPermissionsActivity implements View.OnClickListener {

    private byte[] token = new byte[4];
    private byte CHIP_TYPE;
    private byte DEV_TYPE;
    private TextView deviceName;
    //    private TextView deviceMac;
    private TextView deviceBattery;
    //    private TextView deviceVersion;
//    private TextView deviceCz;
    private TextView deviceStatus;
    //    private TextView openCount;
    private ProgressBar pb;
    byte[] gettoken = {0x06, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] sendDataBytes = null;
    private ProgressDialog progressDialog;
    private boolean isAuto = false;
    private int count = 0;
    private String name = "";

    private Button bt_refresh;
    private TextView tv_name;
    private LinearLayout largeLabel;
    private CleanEditText et_mobile;
    private CleanEditText et_code;
    private Button bt_code;
    private TextView tv_status;
    private TextView tv_battery;
    private Button bt_open;
    private Button bt_back;

    private TextView tv_category_name;
    private TextView tv_order_num;
    private TextView tv_flow_num;
    private TextView tv_driver_name;
    private TextView tv_box;
    private boolean flag = true;
    private int codeCount = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_manage);
        initView();
        registerReceiver(broadcastReceiver, SampleGattAttributes.makeGattUpdateIntentFilter());
        initWidget();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag = false;
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


        deviceName = findViewById(R.id.tv_name);
//        deviceMac = findViewById(R.id.tv_address);
        deviceBattery = findViewById(R.id.tv_battery);
//        deviceVersion = findViewById(R.id.tv_version);
//        deviceCz = findViewById(R.id.tv_cz);
        deviceStatus = findViewById(R.id.tv_status);
        et_code = findViewById(R.id.et_code);
//        openCount = findViewById(R.id.open_count);
        pb = findViewById(R.id.pb);
        findViewById(R.id.bt_open).setOnClickListener(this);
        findViewById(R.id.bt_code).setOnClickListener(this);
        findViewById(R.id.bt_back).setOnClickListener(this);
//        findViewById(R.id.bt_close).setOnClickListener(this);
//        findViewById(R.id.bt_status).setOnClickListener(this);
//        findViewById(R.id.bt_update_password).setOnClickListener(this);
//        ((CheckBox) findViewById(R.id.bt_auto)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                isAuto = isChecked;
//            }
//        });
        tv_category_name.setText(getIntent().getStringExtra(ExtraConstant.CATEGORY_NAME));
        tv_order_num.setText(getIntent().getStringExtra(ExtraConstant.ORDER_NUM));
        tv_flow_num.setText(getIntent().getStringExtra(ExtraConstant.FLOW_NUM));
        tv_driver_name.setText(getIntent().getStringExtra(ExtraConstant.DRIVER_NAME));
        tv_box.setText(getIntent().getStringExtra(ExtraConstant.BOX));

        name = getIntent().getStringExtra(ExtraConstant.NAME);
        String address = getIntent().getStringExtra(ExtraConstant.ADDRESS);
        if (!TextUtils.isEmpty(name)) {
            deviceName.setText(name + address);
        }
        if (!TextUtils.isEmpty(address)) {
            progressDialog = ProgressDialog.show(this, null, "正在连接...");
//            deviceMac.setText("Mac：" + address);
            App.getInstance().getBluetoothLeService().connect(address);
        }

        getDeviceStatus();
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
//                    openCount.setText("开锁次数：" +count);
                    break;
                case SampleGattAttributes.ACTION_GATT_SERVICES_DISCOVERED:
                    //发现服务

                    handler.sendEmptyMessageDelayed(0, 2000);
                    break;
                case SampleGattAttributes.ACTION_BLE_REAL_DATA:
                    parseData(intent.getStringExtra("data"));
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    Log.e(LockManageActivity.class.getSimpleName(), "state_changed");
                    break;
            }
        }
    };

    //获取锁状态
    private void getDeviceStatus() {
        sendDataBytes = new byte[]{0x05, 0x0E, 0x01, 0X01, token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        App.getInstance().getBluetoothLeService().writeCharacteristic(sendDataBytes);

    }

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
        Log.e(LockManageActivity.class.getSimpleName(), "value:" + decryptString);
        if (decryptString.startsWith("0602")) {//token
            if (decrypt != null && decrypt.length == 16) {
                if (decrypt[0] == 0x06 && decrypt[1] == 0x02) {
                    token[0] = decrypt[3];
                    token[1] = decrypt[4];
                    token[2] = decrypt[5];
                    token[3] = decrypt[6];
                    CHIP_TYPE = decrypt[7];
                    DEV_TYPE = decrypt[10];
//                    deviceVersion.setText("当前版本："+Integer.parseInt(decryptString.substring(16, 18), 16) + "." + Integer.parseInt(decryptString.substring(18, 20), 16));
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }
            handler.sendEmptyMessage(1);
        } else if (decryptString.startsWith("0202")) {//电量
            progressDialog.dismiss();
            if (decryptString.startsWith("020201ff")) {
                ToastUtil.showShortToast("获取电量失败");
//                deviceCz.setText("获取电量失败");
            } else {
                String battery = decryptString.substring(6, 8);
                pb.setProgress(Integer.parseInt(battery, 16));
                deviceBattery.setText("当前电量：" + Integer.parseInt(battery, 16) + "%");
            }
        } else if (decryptString.startsWith("0502")) {//开锁
            if (decryptString.startsWith("05020101")) {
//                deviceCz.setText("开锁失败");
                ToastUtil.showShortToast("开锁失败");

            } else {
                count++;
//                deviceCz.setText("开锁成功");
//                openCount.setText("开锁次数：" +count);
            }
        } else if (decryptString.startsWith("050F")) {//锁状态
            if (decryptString.startsWith("050F0101")) {
//                deviceCz.setText("当前操作：锁已关闭");
                ToastUtil.showShortToast("当前操作：锁已关闭");
            } else {
//                deviceCz.setText("当前操作：锁已开启");
                ToastUtil.showShortToast("当前操作：锁已开启");
            }
        } else if (decryptString.startsWith("050D")) {//复位
            if (decryptString.startsWith("050D0101")) {
//                deviceCz.setText("当前操作：复位失败");
                ToastUtil.showShortToast("当前操作：复位失败");
            } else {
//                deviceCz.setText("当前操作：复位成功");
                ToastUtil.showShortToast("当前操作：复位成功");
            }
        } else if (decryptString.startsWith("0508")) {//上锁
            if (decryptString.startsWith("05080101")) {
//                deviceCz.setText("当前操作：上锁失败");
                ToastUtil.showShortToast("当前操作：上锁失败");
            } else {
//                deviceCz.setText("当前操作：上锁成功");
                ToastUtil.showShortToast("当前操作：上锁成功");
                if (isAuto) {
                    handler.sendEmptyMessageDelayed(2, 1000);
                }

            }
        } else if (decryptString.startsWith("0505")) {
            if (decryptString.startsWith("05050101")) {
//                deviceCz.setText("当前操作：修改密码失败");
                ToastUtil.showShortToast("当前操作：修改密码失败");
            } else {
//                deviceCz.setText("当前操作：修改密码成功");
                ToastUtil.showShortToast("当前操作：修改密码成功");
            }
        } else if (decryptString.startsWith("CB0503")) {
            App.getInstance().getBluetoothLeService().writeCharacteristic(new byte[]{0x05, 0x04, 0x06, SampleGattAttributes.password[0], SampleGattAttributes.password[1], SampleGattAttributes.password[2], SampleGattAttributes.password[3], SampleGattAttributes.password[4], SampleGattAttributes.password[5], token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00});
        }
    }


    @Override
    public void onClick(View v) {
        sendDataBytes = null;
        switch (v.getId()) {
            case R.id.bt_open://开锁

                sendDataBytes = new byte[]{0x05, 0x01, 0x06, SampleGattAttributes.password[0], SampleGattAttributes.password[1], SampleGattAttributes.password[2], SampleGattAttributes.password[3], SampleGattAttributes.password[4], SampleGattAttributes.password[5], token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00};
                App.getInstance().getBluetoothLeService().writeCharacteristic(sendDataBytes);
                getDeviceStatus();
                break;
//            case R.id.bt_status://获取锁状态
//                getDeviceStatus();
//                break;
//            case R.id.bt_close://复位
//                sendDataBytes = new byte[]{0x05, 0x0c, 0x01, 0x01, token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//                App.getInstance().getBluetoothLeService().writeCharacteristic(sendDataBytes);
//
//                break;
//            case R.id.bt_update_password://修改密码
//                App.getInstance().getBluetoothLeService().writeCharacteristic(new byte[]{0x05, 0x03, 0x06,SampleGattAttributes.password[0],SampleGattAttributes.password[1],SampleGattAttributes.password[2],SampleGattAttributes.password[3],SampleGattAttributes.password[4],SampleGattAttributes.password[5],  token[0], token[1], token[2], token[3], 0x00, 0x00, 0x00});
//
//                break;
            case R.id.bt_back:
                finish();
                break;
            case R.id.bt_code:
                //发送验证码
                bt_code.setEnabled(false);
                String phone = et_mobile.getText().toString();
                if (phone == null || "".equals(phone)) {
                    ToastUtil.showShortToast("请输入手机号");
                    bt_code.setEnabled(true);
                } else {
                    sendMsg(phone, name);
                    new Thread() {
                        @Override
                        public void run() {
                            codeCount = 60;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                }
                            });
                            while (flag) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        bt_code.setText("" + codeCount);
                                    }
                                });
                                codeCount--;
                                if (codeCount == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            bt_code.setText("获取验证码");
                                        }
                                    });
                                    break;
                                }
                            }
                            codeCount = 60;

                        }
                    }.start();
                }
                break;
//            case R.id.bt_open:
//                break;
        }
    }

    private void sendMsg(String phone, String msg) {
        SendCodeService request = RetrofitUtils.getRetrofit(Url.SEND_CODE).create(SendCodeService.class);
        Call<String> call = request.call(phone, msg);
        call.enqueue(new MyCallback<String>() {
            @Override
            public void onSuc(Response<String> response) {
                Log.i("sss", "onSuc-->: " + response.code());
                ToastUtil.showShortToast("发送成功");
            }

            @Override
            public void onFail(String message) {
                Log.i("eee", "onFail: " + message);

            }
        });
    }

    /**
     * code: 617051955869
     * reason: qweq
     * phoneno: 18795980532
     *
     * @param phone
     * @param msg
     */
    private void sendLockMsg(String phone, String msg) {
        SendCodeService request = RetrofitUtils.getRetrofit(Url.SEND_CODE).create(SendCodeService.class);
        Call<String> call = request.call(phone, msg);
        call.enqueue(new MyCallback<String>() {
            @Override
            public void onSuc(Response<String> response) {
                Log.i("sss", "onSuc-->: " + response.code());
                ToastUtil.showShortToast("发送成功");
            }

            @Override
            public void onFail(String message) {
                Log.i("eee", "onFail: " + message);

            }
        });
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

    private void initView() {
        bt_back = (Button) findViewById(R.id.bt_back);
        tv_name = findViewById(R.id.tv_name);
        largeLabel = (LinearLayout) findViewById(R.id.largeLabel);

        et_mobile = (CleanEditText) findViewById(R.id.et_mobile);
        et_code = (CleanEditText) findViewById(R.id.et_code);
        bt_code = (Button) findViewById(R.id.bt_code);

        tv_status = findViewById(R.id.tv_status);
        tv_battery = findViewById(R.id.tv_battery);
        bt_open = (Button) findViewById(R.id.bt_open);

        tv_category_name = findViewById(R.id.tv_category_name);
        tv_order_num = findViewById(R.id.tv_order_num);
        tv_flow_num = findViewById(R.id.tv_flow_num);
        tv_driver_name = findViewById(R.id.tv_driver_name);
        tv_box = findViewById(R.id.tv_box);

        bt_back.setOnClickListener(this);
        bt_code.setOnClickListener(this);
        bt_open.setOnClickListener(this);


    }

    private void submit() {
        // validate
        String mobile = et_mobile.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = et_code.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO validate success, do something


    }
}
