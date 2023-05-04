package com.otpless.views;

import androidx.annotation.NonNull;

import com.otpless.dto.OtplessResponse;

@FunctionalInterface
public interface OtplessUserDetailCallback {
    void onOtplessUserDetail(@NonNull final OtplessResponse otplessUserDetail);
}
