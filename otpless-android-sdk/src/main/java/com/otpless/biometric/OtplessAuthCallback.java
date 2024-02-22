package com.otpless.biometric;

import javax.crypto.Cipher;

interface OtplessAuthCallback {
    void onAuthSuccess(final Cipher cipher);

    void onAuthFail(final Exception exception);
}
