package com.otpless.biometric;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import com.otpless.R;
import com.otpless.biometric.models.OtplessBiometricException;
import com.otpless.biometric.models.PromptData;
import com.otpless.utils.Utility;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

class BiometricProviderImpl implements BiometricProvider {

    static final String AUTHENTICATION_FAILED = "Authentication Failed";
    private static final String BIOMETRIC_KEY_NAME = "otpless_biometric_key";

    private final KeyStore keyStore;

    BiometricProviderImpl() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
    }

    private boolean allowedAuthenticatorsIncludeDeviceCredentials(int allowedAuthenticators) {
        return allowedAuthenticators == (BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
    }

    private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/" +
                        KeyProperties.BLOCK_MODE_CBC + "/" +
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
        );
    }

    private SecretKey getSecretKey(int allowedAuthenticators) throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableKeyException {
        if (!keyStore.containsAlias(BIOMETRIC_KEY_NAME)) {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            );
            final KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    BIOMETRIC_KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            );
            builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
            builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            builder.setUserAuthenticationRequired(true);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                int authenticationParameters;
                if (allowedAuthenticatorsIncludeDeviceCredentials(allowedAuthenticators)) {
                    authenticationParameters = KeyProperties.AUTH_BIOMETRIC_STRONG | KeyProperties.AUTH_DEVICE_CREDENTIAL;
                } else {
                    authenticationParameters = KeyProperties.AUTH_BIOMETRIC_STRONG;
                }
                builder.setUserAuthenticationParameters(0, authenticationParameters);
            }
            final KeyGenParameterSpec spec = builder.build();
            keyGenerator.init(spec);
            keyGenerator.generateKey();
        }
        return (SecretKey) keyStore.getKey(BIOMETRIC_KEY_NAME, null);
    }

    private @Nullable SecretKey getSecretKeyOrNull(int allowedAuthenticators) {
        try {
            return getSecretKey(allowedAuthenticators);
        } catch (Exception error) {
            Utility.debugLog(error);
            return null;
        }
    }

    private void showBiometricPrompt(
            @NonNull FragmentActivity context,
            @NonNull Cipher cipher,
            int authenticators,
            @Nullable PromptData promptData,
            @NonNull SingleArgCallback<BiometricPrompt.CryptoObject> successCallback,
            @NonNull SingleArgCallback<OtplessBiometricException> errorCallback
    ) {
        final Executor executor = Executors.newSingleThreadExecutor();
        final BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                errorCallback.invoke(new OtplessBiometricException(
                        "Authentication failed with error code: " + errorCode, errString.toString()
                ));
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (result.getCryptoObject() != null) {
                    successCallback.invoke(result.getCryptoObject());
                } else {
                    errorCallback.invoke(new OtplessBiometricException(
                            "Authentication failed: no crypto object obtained", AUTHENTICATION_FAILED
                    ));
                }
            }
        };
        final BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        if (promptData == null) {
            builder.setTitle(context.getResources().getString(R.string.otpless_biometric_prompt_title));
            builder.setSubtitle(context.getResources().getString(R.string.otpless_biometric_prompt_subtitle));
        } else {
            builder.setTitle(promptData.title);
            builder.setSubtitle(promptData.subTitle);
        }
        builder.setAllowedAuthenticators(authenticators);
        if (!allowedAuthenticatorsIncludeDeviceCredentials(authenticators)) {
            // can only show negative button if device credentials are not allowed
            final String negativeText;
            if (promptData != null) {
                negativeText = promptData.negativeText;
            } else {
                negativeText = context.getResources().getString(R.string.otpless_biometric_prompt_negative);
            }
            builder.setNegativeButtonText(negativeText);
        }
        final BiometricPrompt.PromptInfo promptInfo = builder.build();
        new BiometricPrompt(context, executor, callback)
                .authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
    }

    @Override
    public void showBiometricPromptForRegistration(final FragmentActivity context, final int allowedAuthenticators,
                                                   final PromptData promptData, final OtplessAuthCallback authCallback) {
        try {
            final Cipher cipher = getCipher();
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeyOrNull(allowedAuthenticators));
            showBiometricPrompt(context, cipher, allowedAuthenticators, promptData,
                    arg -> {
                        authCallback.onAuthSuccess(arg.getCipher());
                    }, authCallback::onAuthFail);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            authCallback.onAuthFail(e);
        }
    }

    @Override
    public void showBiometricPromptForAuthentication(FragmentActivity context, int allowedAuthenticators, byte[] iv,
                                                     PromptData promptData, final OtplessAuthCallback authCallback) {
        try {
            final Cipher cipher = getCipher();
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeyOrNull(allowedAuthenticators), new IvParameterSpec(iv));
            showBiometricPrompt(context, cipher, allowedAuthenticators, promptData,
                    arg -> {
                        authCallback.onAuthSuccess(arg.getCipher());
                    }, authCallback::onAuthFail);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            Utility.debugLog(e);
            authCallback.onAuthFail(e);
        }
    }

    @Override
    public int areBiometricsAvailable(FragmentActivity context, int allowedAuthenticators) {
        return BiometricManager.from(context).canAuthenticate(allowedAuthenticators);
    }

    @Override
    public void deleteSecretKey() {
        try {
            keyStore.deleteEntry(BIOMETRIC_KEY_NAME);
        } catch (KeyStoreException e) {
            Utility.debugLog(e);
        }
    }

    @Override
    public void ensureSecretKeyIsAvailable(int allowedAuthenticators) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException {
        final SecretKey key = getSecretKey(allowedAuthenticators);
        Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, key);
    }
}
