package com.otpless.main;

import org.json.JSONObject;

public interface OtplessViewContract {
    default void closeView() {}
    default void onVerificationResult(final int resultCode, final JSONObject jsonObject) {}

    JSONObject getExtraParams();

    OtplessWebAuthnManager getWebAuthnManager();
}
