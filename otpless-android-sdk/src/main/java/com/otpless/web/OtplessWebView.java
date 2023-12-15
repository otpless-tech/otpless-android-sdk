package com.otpless.web;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.otpless.BuildConfig;

public class OtplessWebView extends WebView {

    public static final String JAVASCRIPT_OBJ = "javascript_obj";

    private LoadingStatus mLoadingState = LoadingStatus.InProgress;
    private String mLoadingUrl = null;

    @Nullable
    public PageLoadStatusCallback pageLoadStatusCallback;

    public OtplessWebView(@NonNull Context context) {
        super(context);
        initWebView();
    }

    public OtplessWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    public OtplessWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebView();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public OtplessWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initWebView();
    }

    private void initWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shouldDisableAutofill()) {
            setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            setWebContentsDebuggingEnabled(true);
        }
        // setting background transparent
        this.setBackgroundColor(Color.TRANSPARENT);
        // enabling javascript and dom
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setUserAgentString(String.format("%s otplesssdk", getSettings().getUserAgentString()));
        setWebViewClient(new OtplessWebClient());
    }

    // for oreo and samsung and oppo devices autofill is suppressed
    protected boolean shouldDisableAutofill() {
        final String brand = Build.MANUFACTURER.toLowerCase();
        return (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) &&
                ("samsung".equals(brand) || "oppo".equals(brand));
    }

    private void injectJavaScript() {
        // inserting androidObj
        final String androidObjScript = "javascript: window.androidObj = function AndroidClass() { };";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(androidObjScript, null);
        } else {
            loadUrl(androidObjScript);
        }
        // inserting webNativeAssist function
        final String jsStr = "javascript: " +
                "window.androidObj.webNativeAssist = function(message) { " +
                JAVASCRIPT_OBJ + ".webNativeAssist(message) }";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(jsStr, null);
        } else {
            loadUrl(jsStr);
        }
    }

    public void loadWebUrl(String url) {
        if (url == null) return;
        mLoadingUrl = url;
        changeLoadingStatus(LoadingStatus.InProgress);
        clearCache(true);
        loadUrl(url);
    }

    public String getLoadedUrl() {
        return mLoadingUrl;
    }

    public void callWebJs(final String methodName, final Object... params) {
        final StringBuilder builder = new StringBuilder();
        for (Object obj : params) {
            if (obj instanceof String) {
                final String quotedString = "'" + obj + "'";
                builder.append(quotedString);
            } else {
                builder.append(obj);
            }
            builder.append(",");
        }
        if (builder.length() > 0) {
            // remove the last index as it is comma
            builder.deleteCharAt(builder.length() - 1);
        }
        final String paramStr = builder.toString();
        final String script = "javascript: " + methodName + "(" + paramStr + ")";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            post(() -> {
                evaluateJavascript(script, null);
            });
        } else {
            post(() -> {
                loadUrl(script);
            });
        }
    }

    public void attachNativeWebManager(final OtplessWebListener manager) {
        final WebJsInterface webJsInterface = new WebJsInterface(manager);
        addJavascriptInterface(webJsInterface, JAVASCRIPT_OBJ);
    }

    private class OtplessWebClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if ("about:blank".equals(url)) return;
            changeLoadingStatus(LoadingStatus.Started);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if ("about:blank".equals(url)) return;
            if (mLoadingState != LoadingStatus.Failed) {
                changeLoadingStatus(LoadingStatus.Success);
                injectJavaScript();
            } else { // failed case
                loadUrl("about:blank");
            }
        }

        @Override
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (request.getUrl() != null && request.getUrl().toString().equals(mLoadingUrl)) {
                mLoadingState = LoadingStatus.Failed;
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (failingUrl != null && failingUrl.equals(mLoadingUrl)) {
                String errorMessage;
                switch (errorCode) {
                    case WebViewClient.ERROR_HOST_LOOKUP:
                        errorMessage = "Connection error: " + "No Internet";
                        break;
                    case WebViewClient.ERROR_TIMEOUT:
                        errorMessage = "Connection error: " + "Taking too long to connect";
                        break;
                    default:
                        errorMessage = "Connection error";
                }
                changeLoadingStatus(LoadingStatus.Failed, errorMessage, errorCode, description);
            }
        }
    }

    private void changeLoadingStatus(LoadingStatus loadingStatus, String message, int errorCode, String description) {
        this.mLoadingState = loadingStatus;
        if (this.pageLoadStatusCallback != null) {
            this.pageLoadStatusCallback.onPageStatusChange(
                    new LoadingStatusData(loadingStatus, message, errorCode, description)
            );
        }
    }

    private void changeLoadingStatus(LoadingStatus loadingStatus) {
        this.changeLoadingStatus(loadingStatus, null, 0, null);
    }
}

