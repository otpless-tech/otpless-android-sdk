package com.otpless.biometric.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;

public class BiometricAvailability {

    public final @NonNull BiometricAvailabilityStatus availabilityStatus;
    public final @Nullable Reason reason;

    public static BiometricAvailability fromReason(int reason) {
        final Reason rsn;
        switch (reason) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                rsn = Reason.BIOMETRIC_ERROR_NO_HARDWARE;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                rsn = Reason.BIOMETRIC_ERROR_HW_UNAVAILABLE;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                rsn = Reason.BIOMETRIC_ERROR_NONE_ENROLLED;
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                rsn = Reason.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED;
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                rsn = Reason.BIOMETRIC_ERROR_UNSUPPORTED;
                break;
            default:
                rsn = Reason.BIOMETRIC_STATUS_UNKNOWN;
                break;
        }
        return new BiometricAvailability(
                BiometricAvailabilityStatus.UNAVAILABLE, rsn
        );
    }

    public BiometricAvailability(@NonNull BiometricAvailabilityStatus availabilityStatus, @Nullable Reason reason) {
        if (availabilityStatus == BiometricAvailabilityStatus.UNAVAILABLE && reason == null) {
            throw new IllegalArgumentException("status unavailable can not be assigned without reason");
        }
        if (reason != null) {
            throw new IllegalArgumentException("reason is not required if status is not unavailable");
        }
        this.availabilityStatus = availabilityStatus;
        this.reason = reason;
    }

    public BiometricAvailability(@NonNull BiometricAvailabilityStatus availabilityStatus) {
        this(availabilityStatus, null);
    }
}
