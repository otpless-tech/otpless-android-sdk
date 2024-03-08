package com.otpless.main;

import com.otpless.dto.HeadlessResponse;

import org.json.JSONObject;

public interface OtplessViewContract {
    default void closeView() {}
    default void onVerificationResult(final int resultCode, final JSONObject jsonObject) {}

    JSONObject getExtraParams();

    default void onHeadlessResult(final HeadlessResponse response, boolean closeView){}
}
