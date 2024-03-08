package com.otpless.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public class HeadlessResponse {

    @NonNull
    private final String responseType;
    @Nullable
    private final JSONObject data;
    @Nullable
    private final String error;

    public HeadlessResponse(
            @NonNull String request, @Nullable JSONObject data, @Nullable String error
    ) {
        this.responseType = request;
        this.data = data;
        this.error = error;
    }

    @NonNull
    public String getResponseType() {
        return responseType;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }

    @Nullable
    public String getError() {
        return error;
    }
}