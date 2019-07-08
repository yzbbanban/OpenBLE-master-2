package com.nokelock.utils.retrofit;

import android.util.Log;

import com.nokelock.bean.ResultCode;
import com.nokelock.constant.ServiceResult;
import com.nokelock.utils.LogUtil;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by brander on 2017/8/17.
 */

public abstract class MyCallback<T> implements Callback<T> {
    private static final String TAG = "MyCallback";

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        Log.i(TAG, "onResponse: " + response.body());
        if (response.raw().code() == 200) {//200是服务器有合理响应
            LogUtil.info(TAG, "code: " + response.body());
            LogUtil.info(TAG, "设备信息更新成功");
            onSuc(response);

        } else {//失败响应
            LogUtil.info(TAG, "失败响应");
            onFailure(call, new RuntimeException("response error,detail = " + response.raw().toString()));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {//网络问题会走该回调
        LogUtil.info(TAG, "code failure: " + t.getMessage());
        if (t instanceof SocketTimeoutException) {
            //
        } else if (t instanceof ConnectException) {
            //
        } else if (t instanceof RuntimeException) {
            //
        }
        onFail(t.getMessage());
    }

    public abstract void onSuc(Response<T> response);

    public abstract void onFail(String message);

}
