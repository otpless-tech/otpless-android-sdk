package com.otpless.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.otpless.R;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.utils.Utility;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;
import com.otpless.web.OtplessWebViewWrapper;

import org.json.JSONObject;

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) return;
        checkVerifyOtpless(intent);
    }

    private void initView() {
        mNativeManager = new NativeWebManager(this, mWebView);
        mWebView.attachNativeWebManager(mNativeManager);
        // parse uri from data
        final Uri uri = getIntent().getData();
        if (uri != null) {
            mWebView.loadWebUrl(uri.toString());
        } else {
            // default loading of url
            mWebView.loadWebUrl("https://otpless.com/android/index.html");
        }
        // add slide up animation
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.otpless_slide_up_anim);
        final CardView cardView = findViewById(R.id.parent_cv);
        cardView.startAnimation(animation);
    }

    private void checkVerifyOtpless(@NonNull Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            returnWithError("Uri is null");
            return;
        }

        String waId = uri.getQueryParameter("waId");
        if (waId == null || waId.length() == 0) {
            returnWithError("Waid is null");
            return;
        }
        if (mWebView.isUrlLoaded()) {
            // check the validity of waId with otpless
            mWebView.callWebJs("onWaidReceived", waId);
        } else {
            mWebView.enqueueWaid(waId);
        }
    }

    @Override
    public void onBackPressed() {
        if (mNativeManager == null) return;
        if (mNativeManager.getBackSubscription()) {
            mWebView.callWebJs("onHardBackPressed");
        } else {
            super.onBackPressed();
        }
    }

    private void returnWithError(String message) {
        Intent intent = new Intent();
        intent.putExtra("error_message", message);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.detachNativeWebManager();
        }
        super.onDestroy();
    }
}