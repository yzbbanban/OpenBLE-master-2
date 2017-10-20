package com.nokelock.nokelockble;

import android.app.Application;

import com.nokelock.service.BluetoothLeService;

/**
 * 作者: Sunshine
 * 时间: 2017/4/14.
 * 邮箱: 44493547@qq.com
 * 描述:
 */

public class App extends Application {

    private static App app;
    private BluetoothLeService bluetoothLeService;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static App getInstance(){
        return app;
    }

    public BluetoothLeService getBluetoothLeService(){
        return bluetoothLeService;
    }

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService){
        this.bluetoothLeService = bluetoothLeService;
    }
}
