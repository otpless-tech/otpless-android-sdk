package com.otpless.web;

import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class WebJsInterface {

    private final OtplessWebListener mListener;

    public WebJsInterface(final OtplessWebListener listener) {
        mListener = listener;
    }

    @Nullable
    private Integer getInt(final JSONObject obj, final String key) {
        try {
            return obj.getInt(key);
        } catch (JSONException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Nullable
    private Double getDouble(final JSONObject obj, final String key) {
        try {
            return obj.getDouble(key);
        } catch (JSONException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @NonNull
    private String getString(final JSONObject obj, final String key) {
        return obj.optString(key);
    }

    @Nullable
    private JSONObject getJson(final JSONObject obj, final String key) {
        return obj.optJSONObject(key);
    }

    private JSONArray getJsonList(final JSONObject obj, final String key) {
        return obj.optJSONArray(key);
    }

    @JavascriptInterface
    public void nativeSupport(final String jsObjStr) {
        try {
            final JSONObject jsonObject = new JSONObject(jsObjStr);
            final Integer action = getInt(jsonObject, "actionCode");
            if (action == null) return;
            switch (action) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
