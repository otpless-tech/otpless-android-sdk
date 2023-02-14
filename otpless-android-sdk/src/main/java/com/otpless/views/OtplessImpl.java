package com.otpless.views;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessResultContract;
import com.otpless.utils.Utility;

import java.util.HashMap;

class OtplessImpl {

    private final HashMap<Activity, ActivityResultLauncher<Uri>> mLauncherMap = new HashMap<>();
    private OtplessUserDetailCallback mAfterLaunchCallback = null;

    OtplessImpl() {
    }

    void add(final FragmentActivity activity) {
        final ActivityResultLauncher<Uri> launcher = activity.registerForActivityResult(
                new OtplessResultContract(), this::onOtplessResult
        );
        mLauncherMap.put(activity, launcher);
        activity.getLifecycle().addObserver(new OtplessObserver(activity));
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (mAfterLaunchCallback != null) {
            mAfterLaunchCallback.onOtplessUserDetail(userDetail);
            mAfterLaunchCallback = null;
        }
    }

    void launch(final Context context, final String link, final OtplessUserDetailCallback callback) {
        final ActivityResultLauncher<Uri> launcher = mLauncherMap.get(context);
        if (launcher != null) {
            final String appended = Utility.getUrlWithDeviceParams(context, link);
            final Uri uri = Uri.parse(appended);
            mAfterLaunchCallback = callback;
            launcher.launch(uri);
        }
    }

    class OtplessObserver implements LifecycleObserver {
        final Activity activity;

        OtplessObserver(final Activity activity) {
            this.activity = activity;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroyed() {
            mLauncherMap.remove(activity);
        }
    }
}

