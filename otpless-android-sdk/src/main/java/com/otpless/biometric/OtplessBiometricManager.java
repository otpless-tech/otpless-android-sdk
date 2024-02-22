package com.otpless.biometric;

import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class OtplessBiometricManager {

    public static Biometric getOtplessBiometric(final FragmentActivity context) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        return new BiometricImpl(context);
    }
}
