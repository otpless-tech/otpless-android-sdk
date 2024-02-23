package com.otpless.network;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

public interface EventApi {

    @GET("/prod/appevent")
    @Headers({ "Content-Type: application/json" })
    Call<JSONObject> pushEvents(@QueryMap Map<String, String> map);
}
