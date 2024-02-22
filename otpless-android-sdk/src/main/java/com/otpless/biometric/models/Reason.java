package com.otpless.biometric.models;

public enum Reason {
    /**
     * Status indicating that the biometric key failed to generate. Biometrics cannot proceed.
     */
    BIOMETRIC_KEY_GENERATION_FAILED("Biometric key generation failed."),

    /**
     * Status indicating that this device has no biometric hardware.
     */
    BIOMETRIC_ERROR_NO_HARDWARE("No biometric features available on this device."),

    /**
     * Status indicating that biometric hardware features are currently unavailable.
     */
    BIOMETRIC_ERROR_HW_UNAVAILABLE("Biometric features are currently unavailable."),

    /**
     * Status indicating that there are no biometrics currently enrolled on device.
     */
    BIOMETRIC_ERROR_NONE_ENROLLED("No biometrics currently enrolled on device."),

    /**
     * Status indicating that a security vulnerability has been discovered with one or more hardware sensors.
     */
    BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED(
            "A security vulnerability has been discovered with one or more hardware sensors."
    ),

    /**
     * Status indicating that the requested options are incompatible with the current Android version.
     */
    BIOMETRIC_ERROR_UNSUPPORTED(
            "The requested biometrics options are incompatible with the current Android version."
    ),

    /**
     * Status indicating that we do not know if biometrics are supported on this device.
     */
    BIOMETRIC_STATUS_UNKNOWN("Unable to determine whether the user can authenticate.")
    ;
    public final String message;
    Reason(final String message) {
        this.message = message;
    }
}
