package com.otpless.biometric;

import androidx.fragment.app.FragmentActivity;

import com.otpless.biometric.models.AuthenticateParameters;
import com.otpless.biometric.models.BiometricAvailability;
import com.otpless.biometric.models.RegisterParameters;

public interface Biometric {

    boolean isRegistrationAvailable(final FragmentActivity context);

    BiometricAvailability areBiometricsAvailable(
            FragmentActivity context,
            boolean allowDeviceCredentials);

    boolean removeRegistration();

    boolean isUsingKeyStore();

    void register(final RegisterParameters registerParameters,
                  String data,
                  SingleArgCallback<String> successCallback,
                  SingleArgCallback<Exception> errorCallback);

    void authenticate(final AuthenticateParameters authenticateParameter,
                      SingleArgCallback<String> successCallback,
                      SingleArgCallback<Exception> errorCallback);
}
