package com.otpless.biometric;

import androidx.fragment.app.FragmentActivity;

import com.otpless.biometric.models.PromptData;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;

import javax.crypto.NoSuchPaddingException;

public interface BiometricProvider {
    void showBiometricPromptForRegistration(final FragmentActivity context,
                                            final int allowedAuthenticators,
                                            final PromptData promptData,
                                            final OtplessAuthCallback callback);


    void showBiometricPromptForAuthentication(final FragmentActivity context,
                                              final int allowedAuthenticators,
                                              final byte[] iv,
                                              final PromptData promptData,
                                              final OtplessAuthCallback callback);


    int areBiometricsAvailable(final FragmentActivity context, final int allowedAuthenticators);

    void deleteSecretKey();

    void ensureSecretKeyIsAvailable(final int allowedAuthenticators) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException;
}
