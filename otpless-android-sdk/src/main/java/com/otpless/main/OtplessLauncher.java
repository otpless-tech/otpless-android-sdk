package com.otpless.main;

import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.otpless.network.ApiManager;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessUserDetailCallback;


public class OtplessLauncher {

    private final ActivityResultLauncher<Uri> mLauncher;
    private final String mOtplessLink;

    public OtplessLauncher(@NonNull final Context context, @NonNull final ActivityResultCaller caller,
                           @NonNull final String otplessLink, @NonNull final OtplessUserDetailCallback callback) {
        this.mOtplessLink = Utility.getUrlWithDeviceParams(context, otplessLink);
        this.mLauncher = caller.registerForActivityResult(new OtplessResultContract(), callback::onOtplessUserDetail);
        // parsing host name
        try {
            final Uri uri = Uri.parse(this.mOtplessLink);
            // base url created
            ApiManager.getInstance().baseUrl = uri.getScheme() + "://" + uri.getHost() + "/";
        } catch (Exception ignore) {
        }
    }

    public void launch() {
        if (mOtplessLink != null && mOtplessLink.length() > 0) {
            try {
                final Uri uri = Uri.parse(mOtplessLink);
                mLauncher.launch(uri);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
