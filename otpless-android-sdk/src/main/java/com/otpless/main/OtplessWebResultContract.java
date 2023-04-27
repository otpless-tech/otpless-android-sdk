package com.otpless.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.dto.OtplessResponse;

public class OtplessWebResultContract extends ActivityResultContract<Void, OtplessResponse> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void input) {
        final Intent intent = new Intent(context, OtplessWebActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    @Override
    public OtplessResponse parseResult(int resultCode, @Nullable Intent intent) {
        if (intent == null) return null;
        OtplessResponse userDetail = new OtplessResponse();
        if (resultCode == Activity.RESULT_CANCELED) {
            userDetail.setMessage("user cancelled.");
            userDetail.setStatus("failed");
            return userDetail;
        }
        final boolean success = intent.getBooleanExtra("success", false);
        if (success) {
            userDetail.setStatus("success");
            final String waid = intent.getStringExtra("waId");
            userDetail.setWaId(waid);
            final String userNumber = intent.getStringExtra("userNumber");
            userDetail.setUserNumber(userNumber);
        } else {
            userDetail.setStatus("failed");
            String error = intent.getStringExtra("error");
            if (error == null) {
                error = "Something went wrong";
            }
            userDetail.setMessage(error);
        }
        return userDetail;
    }
}
