package com.otpless.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.dto.OtplessResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class OtplessWebResultContract extends ActivityResultContract<JSONObject, OtplessResponse> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, JSONObject input) {
        final Intent intent = new Intent(context, OtplessWebActivity.class);
        if (input != null) {
            intent.putExtra("extra_json_params", input.toString());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    @NonNull
    @Override
    public OtplessResponse parseResult(int resultCode, @Nullable Intent intent) {
        final OtplessResponse result = new OtplessResponse();
        try {
            // handle cancel result code
            if (resultCode == Activity.RESULT_CANCELED) {
                result.setErrorMessage("user cancelled.");
                return result;
            }
            // handle success case
            if (resultCode == Activity.RESULT_OK && intent != null) {
                final String jsonString = intent.getStringExtra("data");
                final JSONObject data = new JSONObject(jsonString);
                result.setData(data);
                return result;
            }
            // handle other error case
            if (intent == null) {
                result.setErrorMessage("no intent data");
            } else {
                result.setErrorMessage("something went wrong.");
            }
            return result;
        } catch (JSONException e) {
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }
}
