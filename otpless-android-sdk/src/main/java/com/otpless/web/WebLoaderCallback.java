package com.otpless.web;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public interface WebLoaderCallback {
    void showLoader(final String message);

    void hideLoader();
}
