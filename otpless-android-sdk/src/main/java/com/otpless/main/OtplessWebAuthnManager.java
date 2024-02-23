package com.otpless.main;

import android.content.Intent;

import com.google.android.gms.fido.common.Transport;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.otpless.network.ApiCallback;

import org.json.JSONException;
import org.json.JSONObject;

public interface OtplessWebAuthnManager {
    void initRegistration(final JSONObject request, ApiCallback<String> callback) throws Exception;

    void initLogin(final JSONObject request, ApiCallback<String> callback) throws Exception;

    void onActivityResult(final int requestCode, final int resultCode, final Intent intent);
}
