package com.otpless.dto;

import androidx.annotation.NonNull;

import com.otpless.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class OtplessRequest {

    @NonNull
    private String cid = "";
    @NonNull
    private String uxmode = "";

    public OtplessRequest setCid(@NonNull String cid) {
        this.cid = cid;
        return this;
    }

    public OtplessRequest setUxmode(@NonNull String uxmode) {
        this.uxmode = uxmode;
        return this;
    }

    public JSONObject toJsonObj() {
        final JSONObject extra = new JSONObject();
        try {
            extra.put("method", "get");
            final JSONObject params = new JSONObject();
            if (Utility.isValid(cid)) {
                params.put("cid", cid);
            }
            if (Utility.isValid(uxmode)) {
                params.put("uxmode", uxmode);
            }
            extra.put("params", params);
        } catch (JSONException ignore) {
        }
        return extra;
    }
}
