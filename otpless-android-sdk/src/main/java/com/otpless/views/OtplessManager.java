package com.otpless.views;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

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
        this.mOtpImpl.initWebLauncher(activity);
    }

    @SuppressWarnings("unused")
    public void start(final OtplessUserDetailCallback callback) {
        this.mOtpImpl.startOtpless(callback, null);
    }

    @SuppressWarnings("unused")
    public void start(final OtplessUserDetailCallback callback, @NonNull final JSONObject params) {
        this.mOtpImpl.startOtpless(callback, params);
    }

    @SuppressWarnings("unused")
    public void showFabButton(boolean isToShow) {
        this.mOtpImpl.showOtplessFab(isToShow);
    }

    @SuppressWarnings("unused")
    public void setFabPosition(final FabButtonAlignment alignment) {
        this.mOtpImpl.setFabConfig(alignment, -1, -1);
    }

    @SuppressWarnings("unused")
    public void setFabPosition(final FabButtonAlignment alignment, int sideMargin) {
        this.mOtpImpl.setFabConfig(alignment, sideMargin, -1);
    }

    @SuppressWarnings("unused")
    public void setFabPosition(final FabButtonAlignment alignment, int sideMargin, int bottomMargin) {
        this.mOtpImpl.setFabConfig(alignment, sideMargin, bottomMargin);
    }
}
