package com.otpless.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.otpless.R;
import com.otpless.utils.Utility;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;
import com.otpless.web.OtplessWebViewWrapper;

public class OtplessWebActivity extends AppCompatActivity implements WebActivityContract {

    private OtplessWebView mWebView;
    private NativeWebManager mNativeManager;
    private CardView mParentCardView;

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
        mNativeManager = new NativeWebManager(this, mWebView, this);
        mWebView.attachNativeWebManager(mNativeManager);
        // parse uri from data
        final Uri uri = getIntent().getData();
        // get loading intent
        final String packageName = this.getApplicationContext().getPackageName();
        final String loginUrl = packageName + ".otpless://otpless";
        if (uri != null) {
            // adding loading url and package name, add login uri at last
            final Uri.Builder urlToLoad = uri.buildUpon();
            urlToLoad.appendQueryParameter("package", packageName);
            urlToLoad.appendQueryParameter("login_uri", loginUrl);
            mWebView.loadWebUrl(urlToLoad.build().toString());
        } else {
            final Uri.Builder builder = Uri.parse("https://web-uat.otpless.com").buildUpon();
            // add login uri at last
            builder.appendQueryParameter("package", packageName);
            builder.appendQueryParameter("login_uri", loginUrl);
            mWebView.loadWebUrl(builder.build().toString());
        }
        // add slide up animation
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.otpless_slide_up_anim);
        mParentCardView = findViewById(R.id.parent_cv);
        mParentCardView.startAnimation(animation);
    }

    private void checkVerifyOtpless(@NonNull Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            returnWithError("Uri is null");
            return;
        }
        reloadUrl(uri);
    }

    private void reloadUrl(@NonNull final Uri uri) {
        if (mWebView == null) {
            finish();
            return;
        }
        final String loadedUrl = mWebView.getLoadedUrl();
        final Uri newUrl = Utility.combineQueries(
                Uri.parse(loadedUrl), uri
        );
        mWebView.loadWebUrl(newUrl.toString());
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

    @Override
    public CardView getParentView() {
        return mParentCardView;
    }
}