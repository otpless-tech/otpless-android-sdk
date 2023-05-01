package com.otpless.dto;

import org.json.JSONObject;

import java.io.Serializable;

public class OtplessResponse implements Serializable {

    private String errorMessage;
    private JSONObject data;


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OtplessResponse{" +
                "errorMessage='" + errorMessage + '\'' +
                ", data=" + data +
                '}';
    }
}
