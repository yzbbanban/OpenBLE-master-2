package com.nokelock.bean;

import android.bluetooth.BluetoothDevice;

import org.litepal.crud.DataSupport;

import java.util.Arrays;

/**
 * 蓝牙设备
 * Created by sunshine on 2017/2/21.
 */
public class BleDevice {
    private BluetoothDevice device;
    private byte[] scanBytes;
    private int riss = 0;
    private String name;//名称

    public BleDevice() {
    }

    public BleDevice(BluetoothDevice device, byte[] scanBytes, int riss) {
        this.device = device;
        this.scanBytes = scanBytes;
        this.riss = riss;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public byte[] getScanBytes() {
        return scanBytes;
    }

    public void setScanBytes(byte[] scanBytes) {
        this.scanBytes = scanBytes;
    }

    public int getRiss() {
        return riss;
    }

    public void setRiss(int riss) {
        this.riss = riss;
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "device=" + device +
                ", scanBytes=" + Arrays.toString(scanBytes) +
                ", riss=" + riss +
                ", name='" + name + '\'' +
                '}';
    }
}
