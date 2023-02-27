package com.otpless.web;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

public class NativeWebManager implements OtplessWebListener {

    @NonNull
    private final FragmentActivity mActivity;
    @NonNull
    private final OtplessWebView mWebView;

    public NativeWebManager(@NonNull final FragmentActivity fragmentActivity, @NonNull final OtplessWebView webView) {
        mActivity = fragmentActivity;
        mWebView = webView;
    }

    @Override
    public void showLoader() {
        mWebView.callWebJs("showLoader");
    }

    @Override
    public void hideLoader() {
        mWebView.callWebJs("hideLoader");
    }
}
