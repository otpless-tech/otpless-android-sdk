package com.otpless.main;

import androidx.annotation.NonNull;

public interface OtplessRandomIdGenerator {
    @NonNull String getInstallationId();
    @NonNull String getTrackingSessionId();
}
