package com.otpless.views;


import android.app.Activity;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.otpless.main.OtplessEventData;
import com.otpless.main.OtplessEventCallback;

import org.json.JSONObject;

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

    private final OtplessLegacyImpl mOtpImpl;

    private OtplessManager() {
        this.mOtpImpl = new OtplessLegacyImpl();
    }

    public void init(final ComponentActivity activity) {
        this.mOtpImpl.initWebLauncher(activity);
    }

    public void start(final ComponentActivity activity, final OtplessUserDetailCallback callback) {
        this.init(activity);
        this.start(callback);
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

    public void setFabText(final String text) {
        if (text == null || text.length() == 0) return;
        this.mOtpImpl.setFabText(text);
    }

    public void startLegacy(final Activity activity, final OtplessUserDetailCallback callback) {
        this.mOtpImpl.start(activity, callback, null);
    }

    public void startLegacy(final Activity activity, final OtplessUserDetailCallback callback, final JSONObject jsonObject) {
        this.mOtpImpl.start(activity, callback, jsonObject);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        this.mOtpImpl.parseData(requestCode, resultCode, data);
    }

    public void setEventCallback(final OtplessEventCallback callback) {
        this.mOtpImpl.setEventCallback(callback);
    }

    public void sendOtplessEvent(final OtplessEventData event) {
        this.mOtpImpl.sendOtplessEvent(event);
    }

    public void setPageLoaderVisible(final boolean isVisible) {
        this.mHasPageLoaderEnabled = isVisible;
    }

    public boolean isToShowPageLoader() {
        return this.mHasPageLoaderEnabled;
    }

    public void onSignInCompleted() {
        this.mOtpImpl.onSignInCompleted();
    }
}
