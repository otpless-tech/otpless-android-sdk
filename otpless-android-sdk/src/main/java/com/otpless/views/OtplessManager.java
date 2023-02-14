package com.otpless.views;

import android.content.Context;

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

    private final OtplessImpl mOtpImpl;

    private OtplessManager() {
        this.mOtpImpl = new OtplessImpl();
    }

    public void init(final FragmentActivity activity) {
        this.mOtpImpl.add(activity);
    }

    void launch(final Context context, final String link, final OtplessUserDetailCallback callback) {
        this.mOtpImpl.launch(context, link, callback);
    }
}
