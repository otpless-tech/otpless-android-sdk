package com.otpless.main;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.otpless.R;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;
import com.otpless.web.OtplessWebViewWrapper;

public class OtplessWebActivity extends AppCompatActivity {

    private OtplessWebView mWebView;
    private NativeWebManager mNativeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpless_web);
        OtplessWebViewWrapper webViewWrapper = findViewById(R.id.otpless_web_wrapper);
        mWebView = webViewWrapper.getWebView();
        if (mWebView == null) return;
        initView();
    }

    private void initView() {
        mNativeManager = new NativeWebManager(this, mWebView);
        mWebView.attachNativeWebManager(mNativeManager);
        // parse uri from data
        final Uri uri = getIntent().getData();
        if (uri != null) {
            mWebView.loadWebUrl(uri.toString());
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.detachNativeWebManager();
        }
        super.onDestroy();
    }
}