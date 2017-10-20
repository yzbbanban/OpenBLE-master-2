package com.nokelock.utils;

import com.nokelock.bean.BleDevice;

import java.util.Comparator;

/**
 * 作者: Sunshine
 * 时间: 2017/4/14.
 * 邮箱: 44493547@qq.com
 * 描述:
 */

public class SortComparator implements Comparator {
    @Override
    public int compare(Object o, Object t1) {
        BleDevice a = (BleDevice) o;
        BleDevice b = (BleDevice) t1;
        return (b.getRiss()- a.getRiss());
    }
}
