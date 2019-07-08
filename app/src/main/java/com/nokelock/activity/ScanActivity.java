package com.nokelock.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nokelock.adapter.DeviceAdapter;
import com.nokelock.app.App;
import com.nokelock.bean.BleDevice;
import com.nokelock.bean.BlueDevice;
import com.nokelock.constant.ExtraConstant;
import com.nokelock.nokelockble.R;
import com.nokelock.service.BluetoothLeService;
import com.nokelock.utils.LogUtil;
import com.nokelock.utils.ParseLeAdvData;
import com.nokelock.utils.SampleGattAttributes;
import com.nokelock.utils.SortComparator;
import com.nokelock.utils.ToastUtil;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ScanActivity extends MPermissionsActivity {

    private int REQUEST_CODE_SCAN = 100;

    private static final String TAG = "ScanActivity";

    private Button btnScan;

    public static final String CODE = "code";
    private BleDevice bleDevice;
    private Comparator comp;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private List<BleDevice> adapterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        btnScan = findViewById(R.id.btn_scan);
        initScan();
        initBLE();
        initWidget();
    }

    private void initBLE() {
        boolean bindService = bindService(new Intent(this, BluetoothLeService.class), connection, Context.BIND_AUTO_CREATE);
        if (bindService) {
            Log.w(MainActivity.class.getSimpleName(), "蓝牙初始化成功");
        }
    }


    private void initWidget() {
        comp = new SortComparator();

        new Thread(new DeviceThread()).start();

    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothLeService.initBluetooth()) {
                App.getInstance().setBluetoothLeService(bluetoothLeService);
                BluetoothAdapter bluetoothAdapter = bluetoothLeService.getmBluetoothAdapter();
                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, 1);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            App.getInstance().setBluetoothLeService(null);
        }
    };

    private void initScan() {
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
        requestPermission(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
    }

    private void startScan() {
        Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
        /*ZxingConfig是配置类  可以设置是否显示底部布局，闪光灯，相册，是否播放提示音  震动等动能
         * 也可以不传这个参数
         * 不传的话  默认都为默认不震动  其他都为true
         * */
        ZxingConfig config = new ZxingConfig();
        config.setShowbottomLayout(true);//底部布局（包括闪光灯和相册）
        config.setPlayBeep(true);//是否播放提示音
        config.setShake(true);//是否震动
        config.setShowAlbum(true);//是否显示相册
        config.setShowFlashLight(true);//是否显示闪光灯
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        startActivityForResult(intent, REQUEST_CODE_SCAN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                ToastUtil.showShortToast("扫描结果为：" + content);
                if (content.contains(CODE)) {
                    String msg = content.split(CODE + "/")[1];
                    LogUtil.info(TAG, "==msg===>" + msg);
                    //发送请求解析网页

                    //获取附近的蓝牙列表

                    App.getInstance().getBluetoothLeService().getmBluetoothAdapter().stopLeScan(leScanCallback);
                    String address = "";
                    for (BleDevice bd : adapterList) {
                        if (msg.equals(bd.getName())) {
                            address = bd.getDevice().getAddress();
                        }
                    }

                    //获取需要的数据
                    Intent intent = new Intent(ScanActivity.this, LockManageActivity.class);
                    intent.putExtra(ExtraConstant.CATEGORY_NAME, "025");
                    intent.putExtra(ExtraConstant.CATEGORY_NAME, "025");
                    intent.putExtra(ExtraConstant.FLOW_NUM, "WA07");
                    intent.putExtra(ExtraConstant.ORDER_NUM, "D00013");
                    intent.putExtra(ExtraConstant.DRIVER_NAME, "张汝军");
                    intent.putExtra(ExtraConstant.BOX, "2");
                    intent.putExtra(ExtraConstant.NAME, msg);
                    intent.putExtra(ExtraConstant.ADDRESS, address);
                    startActivity(intent);

                } else {
                    ToastUtil.showShortToast("扫描结果为：" + content);
                }

            }
        }
    }

    @Override
    public void permissionSuccess(int requestCode) {
        super.permissionSuccess(requestCode);
        if (requestCode == 101) {
            startScanDevice();
        }
    }

    private void startScanDevice() {
        BluetoothLeService bluetoothLeService = App.getInstance().getBluetoothLeService();
        if (bluetoothLeService != null) {
            final BluetoothAdapter bluetoothAdapter = bluetoothLeService.getmBluetoothAdapter();
            if (bluetoothAdapter == null) {
                return;
            }
            bluetoothDeviceList.clear();
            adapterList.clear();
            bleDeviceList.clear();
            bluetoothAdapter.startLeScan(new UUID[]{SampleGattAttributes.bltServerUUID}, leScanCallback);
            Log.i(TAG, "startScanDevice: ");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, 5000);
        }
    }


    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG, "bleDevice00: " + device);
            if (!bluetoothDeviceList.contains(device)) {
                bluetoothDeviceList.add(device);
                bleDevice = new BleDevice(device, scanRecord, rssi);
                bleDeviceList.add(bleDevice);
            }
        }
    };


    private boolean parseAdvData(int rssi, byte[] scanRecord) {
        if (rssi < -75) return false;
        byte[] bytes = ParseLeAdvData.adv_report_parse(ParseLeAdvData.BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA, scanRecord);
        if (null == bytes || bytes.length < 2) {
            return false;
        }
        if (bytes[0] == 0x01 && bytes[1] == 0x02) {
            return true;
        }
        return false;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://更新设备 保存到数据库
                    Collections.sort(adapterList, comp);
//                    for (int i = 0; i < adapterList.size(); i++) {
//                        BlueDevice device = new BlueDevice();
//                        device.setDevice(String.valueOf(adapterList.get(i).getDevice().getAddress()));
//                        device.setRiss(adapterList.get(i).getRiss());
//                        device.setScanBytes(adapterList.get(i).getScanBytes());
//                        device.setName(String.valueOf(adapterList.get(i).getDevice().getName()));

//                        BlueDevice blueDevice = DataSupport.where("device = ?",
//                                String.valueOf(adapterList.get(i).getDevice().getAddress())).findFirst(BlueDevice.class);
//                        if (blueDevice != null) {
//                            device.setName(blueDevice.getName());
//                            device.update(blueDevice.getId());
//                            adapterList.get(i).setName(blueDevice.getName());
//                        } else {
//                            device.save();
//                            adapterList.get(i).setName(
//                                    "Name:" +device.getDevice()
//                                            + "\nMAC:" + device.getName()
//                            );
//                        }
//                    }
                    Log.i(TAG, "initWidget: " + adapterList);

//                    List<BlueDevice> blueDevices = DataSupport.findAll(BlueDevice.class);
//                    Log.i(TAG, "size: " + blueDevices.size() + "\n data: " + blueDevices);

                    break;
            }
        }
    };

    class DeviceThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (bleDeviceList.size() > 0) {
                    BleDevice bleDevice = bleDeviceList.get(0);
                    Log.i(TAG, "bleDevice11: " + bleDevice);

                    if (null != bleDevice && parseAdvData(bleDevice.getRiss(), bleDevice.getScanBytes())) {
                        adapterList.add(bleDevice);
                        handler.sendEmptyMessage(0);
                    }

                    bleDeviceList.remove(0);
                }
            }
        }
    }

}
