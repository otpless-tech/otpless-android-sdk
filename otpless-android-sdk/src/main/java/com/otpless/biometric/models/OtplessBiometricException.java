package com.otpless.biometric.models;

public class OtplessBiometricException extends  Exception {

    private final String reason;

    public OtplessBiometricException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
