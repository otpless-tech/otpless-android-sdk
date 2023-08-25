package com.otpless.main;

import android.app.Activity;

public class OtplessManager {

    private static OtplessManager sInstance = null;
    private boolean mHasPageLoaderEnabled = true;

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

    public OtplessView getOtplessView(final Activity activity) {
        return new OtplessViewImpl(activity);
    }

    public void setPageLoaderVisible(final boolean isVisible) {
        this.mHasPageLoaderEnabled = isVisible;
    }

    public boolean isToShowPageLoader() {
        return this.mHasPageLoaderEnabled;
    }
}
