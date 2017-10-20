package com.nokelock.utils;

import android.util.Log;

/**
 * 作者: Sunshine
 * 时间: 2017/4/14.
 * 邮箱: 44493547@qq.com
 * 描述:
 */

public class ParseLeAdvData {
    private final static String TAG = "ParseLeAdvData";
    //LE 广播包数据类型
    public static final short BLE_GAP_AD_TYPE_FLAGS = 0x01;
    /**
     * < Flags for discoverability.
     */
    public static final short BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE = 0x02;
    /**
     * < Partial list of 16 bit service UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_COMPLETE = 0x03;
    /**
     * < Complete list of 16 bit service UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_MORE_AVAILABLE = 0x04;
    /**
     * < Partial list of 32 bit service UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_COMPLETE = 0x05;
    /**
     * < Complete list of 32 bit service UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE = 0x06;
    /**
     * < Partial list of 128 bit service UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE = 0x07;
    /**
     * < Complete list of 128 bit service UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_SHORT_LOCAL_NAME = 0x08;
    /**
     * < Short local device name.
     */
    public static final short BLE_GAP_AD_TYPE_COMPLETE_LOCAL_NAME = 0x09;
    /**
     * < Complete local device name.
     */
    public static final short BLE_GAP_AD_TYPE_TX_POWER_LEVEL = 0x0A;
    /**
     * < Transmit power level.
     */
    public static final short BLE_GAP_AD_TYPE_CLASS_OF_DEVICE = 0x0D;
    /**
     * < Class of device.
     */
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C = 0x0E;
    /**
     * < Simple Pairing Hash C.
     */
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R = 0x0F;
    /**
     * < Simple Pairing Randomizer R.
     */
    public static final short BLE_GAP_AD_TYPE_SECURITY_MANAGER_TK_VALUE = 0x10;
    /**
     * < Security Manager TK Value.
     */
    public static final short BLE_GAP_AD_TYPE_SECURITY_MANAGER_OOB_FLAGS = 0x11;
    /**
     * < Security Manager Out Of Band Flags.
     */
    public static final short BLE_GAP_AD_TYPE_SLAVE_CONNECTION_INTERVAL_RANGE = 0x12;
    /**
     * < Slave Connection Interval Range.
     */
    public static final short BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_16BIT = 0x14;
    /**
     * < List of 16-bit Service Solicitation UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_128BIT = 0x15;
    /**
     * < List of 128-bit Service Solicitation UUIDs.
     */
    public static final short BLE_GAP_AD_TYPE_SERVICE_DATA = 0x16;
    /**
     * < Service Data.
     */
    public static final short BLE_GAP_AD_TYPE_PUBLIC_TARGET_ADDRESS = 0x17;
    /**
     * < Public Target Address.
     */
    public static final short BLE_GAP_AD_TYPE_RANDOM_TARGET_ADDRESS = 0x18;
    /**
     * < Random Target Address.
     */
    public static final short BLE_GAP_AD_TYPE_APPEARANCE = 0x19;
    /**
     * < Appearance.
     */
    public static final short BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;
    /**
     * < Manufacturer Specific Data.
     */
    public ParseLeAdvData() {
        Log.d(TAG, "ParseLeAdvData init....");
    }
    ///////解析广播数据/////////////////////////
    public static final byte[] adv_report_parse(short type, byte[] adv_data) {
        int index = 0;
        int length;
        byte[] data;
        byte field_type = 0;
        byte field_length = 0;
        length = adv_data.length;
        while (index < length) {
            try {
                field_length = adv_data[index];
                field_type = adv_data[index + 1];
            } catch (Exception e) {
                Log.d(TAG, "There is a exception here.");
                return null;
            }
            if (field_type == (byte) type) {
                data = new byte[field_length - 1];
                byte i;
                for (i = 0; i < field_length - 1; i++) {
                    data[i] = adv_data[index + 2 + i];
                }
                return data;
            }
            index += field_length + 1;
            if (index >= adv_data.length) {
                return null;
            }
        }
        return null;
    }
}
