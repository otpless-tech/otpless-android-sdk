package com.otpless.views;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.otpless.main.OtplessWebResultContract;
import com.otpless.utils.Utility;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

class OtplessLegacyImpl extends OtplessImpl {

    private static final int REQUEST_CODE = 16702650;

    private OtplessUserDetailCallback mAfterLaunchCallback = null;
    private JSONObject mExtraParams = null;

    void start(final Activity activity, final OtplessUserDetailCallback callback, final JSONObject params) {
        mAfterLaunchCallback = callback;
        wActivity = new WeakReference<>(activity);
        mExtraParams = params;
        Utility.addContextInfo(activity);
        final Intent intent = OtplessWebResultContract.makeOtplessWebIntent(activity, params);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onFabButtonClicked() {
        final Activity activity = wActivity.get();
        final Intent intent = OtplessWebResultContract.makeOtplessWebIntent(activity, mExtraParams);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    void parseData(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode != REQUEST_CODE) return;
        if (mAfterLaunchCallback != null) {
            mAfterLaunchCallback.onOtplessUserDetail(
                    OtplessWebResultContract.parseResultData(resultCode, data)
            );
            // adding button
            final Button button = wFabButton.get();
            if (button != null) {
                if (!mShowOtplessFab) {
                    // remove the fab button
                    final ViewGroup parent = wDecorView.get();
                    if (parent == null) return;
                    parent.removeView(button);
                    return;
                }
                // make button visible after first callback
                button.setVisibility(View.VISIBLE);
                button.setText(mFabText);
                return;
            }
            if (wActivity.get() == null || !mShowOtplessFab) return;
            addButtonOnDecor(wActivity.get());
        }
    }

    @Override
    protected void onSignInCompleted() {
        mAfterLaunchCallback = null;
        mExtraParams = null;
        super.onSignInCompleted();
    }
}
