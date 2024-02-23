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
    private Boolean getBoolean(final JSONObject obj, final String key) {
        try {
            return obj.getBoolean(key);
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
    public void webNativeAssist(final String jsObjStr) {
        try {
            final JSONObject jsonObject = new JSONObject(jsObjStr);
            final Integer actionCode = getInt(jsonObject, "key");
            if (actionCode == null) return;

            switch (actionCode) {
                // to show loader
                case 1:
                    final String message = getString(jsonObject, "message");
                    this.mListener.showLoader(message);
                    break;
                // to hide loader
                case 2:
                    this.mListener.hideLoader();
                    break;
                // to subscribe back press
                case 3:
                    final Boolean subscribe = getBoolean(jsonObject, "subscribe");
                    if (subscribe == null) break;
                    this.mListener.subscribeBackPress(subscribe);
                    break;
                // save string
                case 4:
                    final String infoKey = getString(jsonObject, "infoKey");
                    if (infoKey.length() == 0) break;
                    final String infoValue = getString(jsonObject, "infoValue");
                    if (infoValue.length() == 0) break;
                    this.mListener.saveString(infoKey, infoValue);
                    break;
                // get string
                case 5:
                    final String infKey = getString(jsonObject, "infoKey");
                    if (infKey.length() == 0) break;
                    this.mListener.getString(infKey);
                    break;
                // parse deeplink
                case 7:
                    final String deeplink = getString(jsonObject, "deeplink");
                    if (deeplink.length() == 0) return;
                    // checkout for extras
                    final JSONObject extra = getJson(jsonObject, "extra");
                    this.mListener.openDeeplink(deeplink, extra);
                    break;
                // get app info
                case 8:
                    this.mListener.appInfo();
                    break;
                // verification status call key 11
                case 11:
                    final JSONObject json = getJson(jsonObject, "response");
                    if (json == null) break;
                    this.mListener.codeVerificationStatus(json);
                    break;
                // change the height of web view
                case 12:
                    final Integer heightPercent = getInt(jsonObject, "heightPercent");
                    if (heightPercent == null) return;
                    this.mListener.changeWebViewHeight(heightPercent);
                    break;
                case 13:
                    this.mListener.extraParams();
                    break;
                case 14:
                    this.mListener.closeActivity();
                    break;
                case 15:
                    final JSONObject eventData = getJson(jsonObject, "eventData");
                    if (eventData == null) return;
                    this.mListener.pushEvent(eventData);
                    break;
                case 16:
                    final Boolean otpR = getBoolean(jsonObject, "otpread");
                    boolean otpRead = false;
                    if (otpR != null) {
                        otpRead = otpR;
                    }
                    this.mListener.otpAutoRead(otpRead);
                    break;
                case 18:
                    this.mListener.phoneNumberSelection();
                    break;
                case 26:
                    final JSONObject registerWebAuthnRequest = getJson(jsonObject, "request");
                    if (registerWebAuthnRequest == null) return;
                    this.mListener.initWebAuthnRegistration(registerWebAuthnRequest);
                    break;
                case 27:
                    final JSONObject signInWebAuthnRequest = getJson(jsonObject, "request");
                    if (signInWebAuthnRequest == null) return;
                    this.mListener.initWebAuthnSignin(signInWebAuthnRequest);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
