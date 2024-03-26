package com.otpless.dto;

import androidx.annotation.NonNull;

import com.otpless.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OtplessRequest {

    @NonNull final String appId;
    @NonNull
    private String uxmode = "";

    @NonNull
    private String locale = "";

    private final HashMap<String, String> mExtras = new HashMap<>();

    public OtplessRequest(@NonNull final String appId) {
        this.appId = appId;
    }

    public OtplessRequest setUxmode(@NonNull String uxmode) {
        this.uxmode = uxmode;
        return this;
    }

    public OtplessRequest setLocale(@NonNull String locale) {
        this.locale = locale;
        return this;
    }

    public OtplessRequest addExtras(@NonNull final String key, @NonNull final  String value) {
        mExtras.put(key, value);
        return this;
    }

    @NonNull
    public JSONObject toJsonObj() {
        final JSONObject extra = new JSONObject();
        try {
            extra.put("method", "get");
            //region adding data in params
            final JSONObject params = new JSONObject();
            if (Utility.isValid(uxmode)) {
                params.put("uxmode", uxmode);
            }
            if (Utility.isValid(locale)) {
                params.put("locale", locale);
            }
            for (Map.Entry<String, String> entry: mExtras.entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }
            //endregion
            // adding params in extras
            extra.put("params", params);
        } catch (JSONException ignore) {
        }
        return extra;
    }

    @NonNull
    public String getAppId() {
        return appId;
    }
}
