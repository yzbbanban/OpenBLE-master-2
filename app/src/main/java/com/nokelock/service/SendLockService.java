package com.nokelock.service;

import com.nokelock.bean.ResultCode;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 发送验证码
 */
public interface SendLockService {
    @POST("ajaxks")
    @FormUrlEncoded
    Call<ResultCode<String>> call(@Field("exception") String exception);
}
