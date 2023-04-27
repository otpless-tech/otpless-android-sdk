package com.otpless.views;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessWebResultContract;

class OtplessImpl {

    private OtplessUserDetailCallback mAfterLaunchCallback = null;
    private ActivityResultLauncher<Void> mWebLaunch;

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

    void launchOtplessWeb(final OtplessUserDetailCallback callback) {
        mAfterLaunchCallback = callback;
        mWebLaunch.launch(null);
    }
}

