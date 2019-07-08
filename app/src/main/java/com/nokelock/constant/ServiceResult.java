package com.nokelock.constant;


/**
 * Created by brander on 2017/7/28.
 */

public enum ServiceResult {
    GET_MESSAGE_SUCCESS("10000", "正常"),
    GET_MESSAGE_FALSE("10001", "上传参数问题"),
    GET_MESSAGE_SERVICE_ERROR("10002", "服务器问题"),
    GET_MESSAGE_TIMEOUT("10003", "网络访问超时，请重新上传"),
    GET_MESSAGE_NO_DATA("10004", "没有数据"),
    GET_MESSAGE_DEV_UPDATE_SUCCESS("10005", "设备信息更新成功");

    // 成员变量
    private String index;
    private String name;

    // 构造方法
    ServiceResult(String index, String name) {
        this.index = index;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
