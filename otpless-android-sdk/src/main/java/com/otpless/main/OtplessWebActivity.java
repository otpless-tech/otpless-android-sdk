package com.otpless.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.otpless.R;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.network.NetworkStatusData;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessManager;
import com.otpless.web.LoadingStatus;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;
import com.otpless.web.OtplessWebViewWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class OtplessWebActivity extends OtplessSdkBaseActivity implements WebActivityContract {

    private OtplessWebView mWebView;
    private ProgressBar mProgress;
    private NativeWebManager mNativeManager;
    private ViewGroup mParentViewGroup;
    private JSONObject mExtraJSONParams;
    private Uri mPendingReceivedUri = null;
    private boolean isCodeLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpless_web);
        OtplessWebViewWrapper webViewWrapper = findViewById(R.id.otpless_web_wrapper);
        mWebView = webViewWrapper.getWebView();
        if (mWebView == null) {
            finish();
            return;
        }
        initView();
        //region send load event
        Utility.pushEvent("sdk_screen_loaded");
        //endregion
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }
        checkVerifyOtpless(intent);
    }

    private void initView() {
        mNativeManager = new NativeWebManager(this, mWebView, this);
        mWebView.attachNativeWebManager(mNativeManager);
        mProgress = findViewById(R.id.progress_bar);
        if (OtplessManager.getInstance().isToShowPageLoader()) {
            mProgress.setVisibility(View.VISIBLE);
            mWebView.pageLoadStatusCallback = loadingStatus -> {
                if (loadingStatus == LoadingStatus.InProgress) {
                    mProgress.setVisibility(View.VISIBLE);
                } else {
                    mProgress.setVisibility(View.GONE);
                }
            };
        }
        ApiManager.getInstance().apiConfig(new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                // check for fab button text
                final String fabText = data.optString("button_text");
                OtplessManager.getInstance().setFabText(fabText);
                // check for url
                final String url = data.optString("auth");
                if (isCodeLoaded) return;
                if (!url.isEmpty()) {
                    firstLoad(url);
                    return;
                }
                // fallback loading
                firstLoad("https://otpless.com/mobile/index.html");

            }

            @Override
            public void onError(Exception exception) {
                if (isCodeLoaded) return;
                firstLoad("https://otpless.com/mobile/index.html");
            }
        });
        // add slide up animation
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.otpless_slide_up_anim);
        mParentViewGroup = findViewById(R.id.parent_vg);
        mParentViewGroup.startAnimation(animation);
    }

    private void firstLoad(final String url) {
        final String packageName = this.getApplicationContext().getPackageName();
        String loginUrl = packageName + ".otpless://otpless";
        final Uri.Builder urlToLoad = Uri.parse(url).buildUpon();

        // check for additional json params while loading
        final String extraParamStr = getIntent().getStringExtra("extra_json_params");
        if (extraParamStr != null) {
            try {
                final JSONObject extra = new JSONObject(extraParamStr);
                String methodName = extra.optString("method").toLowerCase();
                mExtraJSONParams = extra.getJSONObject("params");
                if (methodName.equals("get")) {
                    // add the params in url
                    final JSONObject params = extra.getJSONObject("params");
                    for (Iterator<String> it = params.keys(); it.hasNext(); ) {
                        String key = it.next();
                        final String value = params.optString(key);
                        if (value.isEmpty()) continue;
                        if ("login_uri".equals(key)) {
                            loginUrl = value + ".otpless://otpless";
                            continue;
                        }
                        urlToLoad.appendQueryParameter(key, value);
                    }
                }
            } catch (JSONException ignore) {
            }
        }
        // adding loading url and package name, add login uri at last
        urlToLoad.appendQueryParameter("package", packageName);
        urlToLoad.appendQueryParameter("hasWhatsapp", String.valueOf(Utility.isWhatsAppInstalled(this)));
        urlToLoad.appendQueryParameter("hasOtplessApp", String.valueOf(Utility.isOtplessAppInstalled(this)));
        urlToLoad.appendQueryParameter("login_uri", loginUrl);
        //
        final String finalUrl = urlToLoad.build().toString();
        if (mPendingReceivedUri != null) {
            reloadToVerifyCode(mPendingReceivedUri, finalUrl);
            mPendingReceivedUri = null;
            return;
        }
        mWebView.loadWebUrl(urlToLoad.build().toString());
    }

    private void checkVerifyOtpless(@NonNull Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            sendIntentInEvent(false);
            Intent result = new Intent();
            result.putExtra("error_message", "Uri is null");
            setResult(Activity.RESULT_CANCELED, result);
            finish();
            return;
        }
        if (mWebView == null) {
            finish();
            return;
        }
        final String loadedUrl = mWebView.getLoadedUrl();
        // if loadedUrl is null that means apps onCreate is called and activity is recreated because of memory reason and base url loading is in progress
        if (loadedUrl == null) {
            mPendingReceivedUri = uri;
            return;
        }
        reloadToVerifyCode(uri, loadedUrl);
    }

    private void reloadToVerifyCode(@NonNull final Uri uri, @NonNull final String loadedUrl) {
        final boolean hasCode;
        final String code = uri.getQueryParameter("code");
        hasCode = code != null && code.length() != 0;
        final Uri newUrl = Utility.combineQueries(
                Uri.parse(loadedUrl), uri
        );
        mWebView.loadWebUrl(newUrl.toString());
        isCodeLoaded = true;
        sendIntentInEvent(hasCode);
    }

    @Override
    public void onBackPressed() {
        if (mNativeManager == null) return;
        if (mNativeManager.getBackSubscription()) {
            mWebView.callWebJs("onHardBackPressed");
        } else {
            Utility.pushEvent("user_abort");
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.detachNativeWebManager();
        }
        Utility.pushEvent("sdk_screen_dismissed");
        super.onDestroy();
    }

    @Override
    public ViewGroup getParentView() {
        return mParentViewGroup;
    }

    @Override
    public JSONObject getExtraParams() {
        return mExtraJSONParams;
    }

    private void sendIntentInEvent(final boolean isSuccess) {
        final JSONObject params = new JSONObject();
        final String type;
        if (isSuccess) {
            type = "success";
        } else {
            type = "error";
        }
        try {
            params.put("type", type);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Utility.pushEvent("intent_redirect_in", params);
    }

    @Override
    public void onConnectionChange(NetworkStatusData statusData) {
        super.onConnectionChange(statusData);
        if (!statusData.isEnabled()) {
            runOnUiThread(() -> {
                OtplessManager.getInstance().sendOtplessEvent(
                        new OtplessEventData(101, null)
                );
            });
        }
    }
}