package com.otpless.biometric.models;

public enum BiometricAvailabilityStatus {
    /**
     * Status indicating that biometrics are not available on this device for some reason
     */
    UNAVAILABLE,

    /**
     * Status indicating that the biometric key has been revoked. Usually this means the user has added a new
     * biometric or deleted all existing biometrics.
     */
    REGISTRATION_REVOKED,

    /**
     * Status indicating that biometrics are available, but no registrations have been made yet
     */
    AVAILABLE_NO_REGISTRATIONS,

    /**
     * Status indicating that biometrics are available and there is already a biometric registration on device
     */
    AVAILABLE_REGISTERED
}
