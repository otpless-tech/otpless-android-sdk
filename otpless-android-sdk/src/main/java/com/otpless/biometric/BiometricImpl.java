package com.otpless.biometric;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.FragmentActivity;

import com.otpless.biometric.models.AuthenticateParameters;
import com.otpless.biometric.models.BiometricAvailability;
import com.otpless.biometric.models.BiometricAvailabilityStatus;
import com.otpless.biometric.models.RegisterParameters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

class BiometricImpl implements Biometric {

    private static final String REGISTRATION_DATA = "otpless_registration_data";
    private static final String REGISTRATION_IV_DATA = "otpless_registration_iv_data";
    private static final String ALLOWED_AUTHENTICATOR = "otpless_allowed_authenticator_data";

    private final Context context;
    private final BiometricProvider biometricProvider;

    private final StorageManager storageManager;
    private final Handler uiHandler;

    BiometricImpl(Context context) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        this.context = context;
        biometricProvider = new BiometricProviderImpl();
        storageManager = new StorageManager(context);
        uiHandler = new Handler(Looper.getMainLooper());
    }

    private int getAllowedAuthenticators(boolean allowDeviceCredentials) {
        int allowedAuthenticator;
        if (allowDeviceCredentials && Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            allowedAuthenticator = BIOMETRIC_STRONG | DEVICE_CREDENTIAL;
        } else {
            allowedAuthenticator = BIOMETRIC_STRONG;
        }
        return allowedAuthenticator;
    }

    @Override
    public boolean isRegistrationAvailable(FragmentActivity context) {
        return registrationExists() &&
                areBiometricsAvailable(context, false).availabilityStatus != BiometricAvailabilityStatus.REGISTRATION_REVOKED;
    }

    private boolean registrationExists() {
        final String data = storageManager.getKey(REGISTRATION_DATA);
        return data.length() > 0;
    }

    @Override
    public BiometricAvailability areBiometricsAvailable(FragmentActivity context, boolean allowDeviceCredentials) {
        final int allowedAuthenticators = getAllowedAuthenticators(allowDeviceCredentials);
        try {
            biometricProvider.ensureSecretKeyIsAvailable(allowedAuthenticators);
            final int result = biometricProvider.areBiometricsAvailable(context, allowedAuthenticators);
            if (result == BiometricManager.BIOMETRIC_SUCCESS) {
                if (registrationExists()) {
                    return new BiometricAvailability(BiometricAvailabilityStatus.AVAILABLE_REGISTERED);
                } else {
                    return new BiometricAvailability(BiometricAvailabilityStatus.AVAILABLE_NO_REGISTRATIONS);
                }
            }
            return BiometricAvailability.fromReason(result);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | NoSuchProviderException |
                 InvalidKeyException e) {
            return new BiometricAvailability(BiometricAvailabilityStatus.REGISTRATION_REVOKED, null);
        } catch (UnrecoverableKeyException | KeyStoreException e) {
            return new BiometricAvailability(BiometricAvailabilityStatus.REGISTRATION_REVOKED, null);
        }
    }

    @Override
    public boolean removeRegistration() {
        storageManager.deleteKey(
                REGISTRATION_DATA, REGISTRATION_IV_DATA, ALLOWED_AUTHENTICATOR
        );
        biometricProvider.deleteSecretKey();
        return true;
    }

    @Override
    public boolean isUsingKeyStore() {
        return true;
    }

    @Override
    public void register(RegisterParameters registerParameters, String data,
                         SingleArgCallback<String> successCallback, SingleArgCallback<Exception> errorCallback) {
        if (isRegistrationAvailable(registerParameters.context)) {
            removeRegistration();
        }
        final int allowedAuthenticator = getAllowedAuthenticators(registerParameters.allowDeviceCredentials);
        biometricProvider.showBiometricPromptForRegistration(
                registerParameters.context, allowedAuthenticator, registerParameters.promptData, new OtplessAuthCallback() {
                    @Override
                    public void onAuthSuccess(Cipher cipher) {
                        try {
                            final byte[] output = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
                            final String base64OfEncrypted = Base64.encodeToString(
                                    output, Base64.NO_WRAP | Base64.NO_PADDING
                            );
                            storageManager.saveKey(REGISTRATION_DATA, base64OfEncrypted);
                            final String base64OfIv = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP | Base64.NO_PADDING);
                            storageManager.saveKey(REGISTRATION_IV_DATA, base64OfIv);
                            storageManager.saveInt(ALLOWED_AUTHENTICATOR, allowedAuthenticator);
                            uiHandler.post(() -> successCallback.invoke(base64OfEncrypted));
                        } catch (BadPaddingException | IllegalBlockSizeException e) {
                            uiHandler.post(() -> errorCallback.invoke(e));
                        }
                    }

                    @Override
                    public void onAuthFail(Exception exception) {
                        uiHandler.post(() -> errorCallback.invoke(exception));
                    }
                }
        );
    }

    @Override
    public void authenticate(AuthenticateParameters authenticateParameter,
                             SingleArgCallback<String> successCallback,
                             SingleArgCallback<Exception> errorCallback) {
        if (!registrationExists()) {
            errorCallback.invoke(new Exception("registration do not exist"));
        }
        final byte[] iv = Base64.decode(storageManager.getKey(REGISTRATION_IV_DATA), Base64.NO_WRAP | Base64.NO_PADDING);
        final int allowedAuthenticator = storageManager.getIntKey(ALLOWED_AUTHENTICATOR);
        biometricProvider.showBiometricPromptForAuthentication(authenticateParameter.context,
                allowedAuthenticator, iv,
                authenticateParameter.promptData,
                new OtplessAuthCallback() {
                    @Override
                    public void onAuthSuccess(Cipher cipher) {
                        final byte[] encryptedBytes = Base64.decode(storageManager.getKey(REGISTRATION_DATA), Base64.NO_WRAP | Base64.NO_PADDING);
                        try {
                            final byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                            final String str = new String(decryptedBytes, StandardCharsets.UTF_8);
                            uiHandler.post(() -> successCallback.invoke(str));
                        } catch (BadPaddingException | IllegalBlockSizeException e) {
                            uiHandler.post(() -> errorCallback.invoke(e));
                        }
                    }

                    @Override
                    public void onAuthFail(Exception exception) {
                        uiHandler.post(() -> errorCallback.invoke(exception));
                    }
                });
    }
}

class StorageManager {

    private static final String OTPLESS_SHARED_PREF = "otpless_shared_pref";
    private final Context context;

    StorageManager(Context context) {
        this.context = context;
    }

    void saveKey(@NonNull String key, @NonNull String value) {
        final SharedPreferences preferences = context.getSharedPreferences(OTPLESS_SHARED_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    void saveInt(@NonNull String key, int value) {
        final SharedPreferences preferences = context.getSharedPreferences(OTPLESS_SHARED_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    String getKey(@NonNull String key) {
        final SharedPreferences preferences = context.getSharedPreferences(OTPLESS_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    int getIntKey(@NonNull String key) {
        final SharedPreferences preferences = context.getSharedPreferences(OTPLESS_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getInt(key, -1);
    }

    void deleteKey(String... keys) {
        final SharedPreferences preferences = context.getSharedPreferences(OTPLESS_SHARED_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }
}
