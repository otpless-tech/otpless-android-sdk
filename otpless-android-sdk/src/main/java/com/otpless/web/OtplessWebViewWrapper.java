package com.otpless.web;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OtplessWebViewWrapper extends FrameLayout {

    private OtplessWebView mWebView = null;

    public OtplessWebViewWrapper(@NonNull Context context) {
        super(context);
        initView(context, null, 0);
    }

    public OtplessWebViewWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public OtplessWebViewWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    @Nullable
    public OtplessWebView getWebView() {
        return mWebView;
    }

    private void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        try {
            if (defStyleAttr > 0) {
                mWebView = new OtplessWebView(context, attrs, defStyleAttr);
            } else {
                mWebView = new OtplessWebView(context, attrs);
            }
            this.setBackgroundColor(Color.TRANSPARENT);
            addView(mWebView);
        } catch (Exception exception) {
            final TextView tv;
            if (defStyleAttr > 0) {
                tv = new TextView(context, attrs, defStyleAttr);
            } else {
                tv = new TextView(context, attrs);
            }
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(24, 24, 24, 24);
            tv.setTextColor(Color.BLACK);
            final String errorMessage = "Error in loading web. Please try again later.";
            tv.setText(errorMessage);
            addView(tv);
        }
    }
}
