package com.otpless.views;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessWebResultContract;

import org.json.JSONObject;

class OtplessImpl {

    private OtplessUserDetailCallback mAfterLaunchCallback = null;
    private ActivityResultLauncher<JSONObject> mWebLaunch;
    private JSONObject mExtraParams;
    private View mFabButton;

    OtplessImpl() {
    }

    void initWebLauncher(final FragmentActivity activity) {
        mWebLaunch = activity.registerForActivityResult(
                new OtplessWebResultContract(), this::onOtplessResult
        );
        addButtonOnDecor(activity);
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (mAfterLaunchCallback != null) {
            mAfterLaunchCallback.onOtplessUserDetail(userDetail);
        }
        if (mFabButton != null) {
            // make button visible after first callback
            mFabButton.setVisibility(View.VISIBLE);
        }
    }

    void startOtpless(final OtplessUserDetailCallback callback, final JSONObject params) {
        mAfterLaunchCallback = callback;
        mExtraParams = params;
        mWebLaunch.launch(params);
    }

    private void addButtonOnDecor(final FragmentActivity activity) {
        final View decorView = activity.getWindow().getDecorView();
        if (decorView == null) return;
        final ViewGroup parentView = findSuitableParent(decorView);
        if (parentView == null) return;
        final ImageView button = (ImageView) activity.getLayoutInflater().inflate(R.layout.otpless_fab_button, parentView, false);
        button.setOnClickListener(v-> mWebLaunch.launch(mExtraParams));
        parentView.addView(button);
        mFabButton = button;
        // make the button in visible
        button.setVisibility(View.INVISIBLE);
    }

    private ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                return (ViewGroup) view;
            }
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                }
                fallback = (ViewGroup) view;
            }
            if (view != null) {
                ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);
        return fallback;
    }
}

