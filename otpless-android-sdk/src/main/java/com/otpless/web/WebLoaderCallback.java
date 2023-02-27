package com.otpless.web;

import androidx.annotation.NonNull;

public interface WebLoaderCallback {
    void showLoader(final String message);

    void hideLoader();

    void subscribeBackPress(final boolean subscribe);

    void openDeeplink(@NonNull final String deeplink);

    void saveString(@NonNull final String infoKey, @NonNull final String infoValue);

    void getString(@NonNull final String infoKey);

    void appInfo();
}
