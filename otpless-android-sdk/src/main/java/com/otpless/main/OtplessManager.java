package com.otpless.main;

import androidx.fragment.app.FragmentActivity;

public class OtplessManager {

    private static OtplessManager sInstance = null;

    public static OtplessManager getInstance() {
        if (sInstance == null) {
            synchronized (OtplessManager.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                sInstance = new OtplessManager();
            }
        }
        return sInstance;
    }

    public OtplessView getOtplessView(final FragmentActivity activity) {
        return new OtplessViewImpl(activity);
    }
}
