package com.otpless.main;

import android.content.Intent;

import com.otpless.views.OtplessUserDetailCallback;

import org.json.JSONObject;

public interface OtplessView {
    // methods to start otpless
    void startOtpless(final JSONObject params);
    void startOtpless(final JSONObject params, final OtplessUserDetailCallback callback);
    // explicitly setting the callback
    void setCallback(final OtplessUserDetailCallback callback);

    // explicitly closing the view
    void closeView();
    boolean onBackPressed();
    void verifyIntent(Intent intent);

    void setEventCallback(final OtplessEventCallback callback);
}
