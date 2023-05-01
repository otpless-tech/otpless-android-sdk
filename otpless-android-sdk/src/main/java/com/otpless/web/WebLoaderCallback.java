package com.otpless.web;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public interface WebLoaderCallback {
    void showLoader(final String message);

    void hideLoader();

    void subscribeBackPress(final boolean subscribe);

    void openDeeplink(@NonNull final String deeplink);

    void saveString(@NonNull final String infoKey, @NonNull final String infoValue);

    void getString(@NonNull final String infoKey);

    void appInfo();

    // key 11
    void waidVerificationStatus(@NonNull final JSONObject json);

    // key 12
    void changeWebViewHeight(@NonNull final Integer heightPercent);

    // key 13
    void extraParams();
}
