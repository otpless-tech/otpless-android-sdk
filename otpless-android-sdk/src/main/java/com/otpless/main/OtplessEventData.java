package com.otpless.main;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.Serializable;

public class OtplessEventData implements Serializable {

    private final JSONObject data;
    private final int eventCode;

    public OtplessEventData(final int eventCode, @Nullable final JSONObject data) {
        this.eventCode = eventCode;
        this.data = data;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }

    public int getEventCode() {
        return eventCode;
    }
}
