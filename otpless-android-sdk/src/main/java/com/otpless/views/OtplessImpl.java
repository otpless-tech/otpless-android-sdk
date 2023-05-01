package com.otpless.views;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessWebResultContract;

import org.json.JSONObject;

class OtplessImpl {

    private OtplessUserDetailCallback mAfterLaunchCallback = null;
    private ActivityResultLauncher<JSONObject> mWebLaunch;

    OtplessImpl() {
    }

    void initWebLauncher(final FragmentActivity activity) {
        mWebLaunch = activity.registerForActivityResult(
                new OtplessWebResultContract(), this::onOtplessResult
        );
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (mAfterLaunchCallback != null) {
            mAfterLaunchCallback.onOtplessUserDetail(userDetail);
            mAfterLaunchCallback = null;
        }
    }

    void launchOtplessWeb(final OtplessUserDetailCallback callback, final JSONObject params) {
        mAfterLaunchCallback = callback;
        mWebLaunch.launch(params);
    }
}

