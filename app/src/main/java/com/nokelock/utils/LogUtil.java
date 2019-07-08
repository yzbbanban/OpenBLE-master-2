package com.nokelock.utils;

import android.util.Log;

import com.nokelock.app.App;

/**
 * Created by brander on 2016/8/17.
 * 打印工具
 * 因为log打印比较消耗时间、内存，所以生成一个工具类用于显示log信息,
 * 若没有上线（就是在测试中），则执行log
 */
public class LogUtil {
    public synchronized static void info(String key, String message) {
        if (!App.isOnAppStore) {
            Log.i(key, message);
        }

    }

    public synchronized static void debug(String key, String message) {
        if (!App.isOnAppStore) {
            Log.d(key, message);
        }

    }

    public synchronized static void err(String key, String message) {
        if (!App.isOnAppStore) {
            Log.e(key, message);
        }

    }

}
