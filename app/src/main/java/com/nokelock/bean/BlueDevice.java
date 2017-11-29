package com.nokelock.bean;


import org.litepal.crud.DataSupport;

import java.util.Arrays;

/**
 * 蓝牙设备
 * Created by sunshine on 2017/2/21.
 */
public class BlueDevice extends DataSupport {
    private long id;
    private String device;//address
    private byte[] scanBytes;
    private int riss = 0;
    private String name;//名称

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
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
        return "BlueDevice{" +
                "id=" + id +
                ", device='" + device + '\'' +
                ", scanBytes=" + Arrays.toString(scanBytes) +
                ", riss=" + riss +
                ", name='" + name + '\'' +
                '}';
    }
}
