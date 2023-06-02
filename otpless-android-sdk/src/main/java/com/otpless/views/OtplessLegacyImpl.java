package com.otpless.views;

import android.app.Activity;
import android.content.Intent;

import com.otpless.main.OtplessWebResultContract;
import com.otpless.utils.Utility;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

class OtplessLegacyImpl extends OtplessImpl {

    private static final int REQUEST_CODE = 16702650;

    private OtplessUserDetailCallback mAfterLaunchCallback = null;

    void start(final Activity activity, final OtplessUserDetailCallback callback, final JSONObject params) {
        showOtplessFab(false);
        mAfterLaunchCallback = callback;
        wActivity = new WeakReference<>(activity);
        Utility.addContextInfo(activity);
        final Intent intent = OtplessWebResultContract.makeOtplessWebIntent(activity, params);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    void parseData(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode != REQUEST_CODE) return;
        if (mAfterLaunchCallback != null) {
            mAfterLaunchCallback.onOtplessUserDetail(
                    OtplessWebResultContract.parseResultData(resultCode, data)
            );
            mAfterLaunchCallback = null;
        }
    }
}
