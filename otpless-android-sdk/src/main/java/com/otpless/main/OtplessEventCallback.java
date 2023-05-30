package com.otpless.main;

@FunctionalInterface
public interface OtplessEventCallback {
    void onOtplessEvent(final OtplessEventData event);
}
