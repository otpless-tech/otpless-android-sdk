package com.otpless.web;

import android.content.Context;
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

    private boolean hasUrlLoadFailed = false;
    WebLoaderCallback webLoaderCallback = null;
    private String mLoadingUrl = null;

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
        // enabling javascript and dom
        getSettings().setJavaScriptEnabled(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setLoadsImagesAutomatically(true);
        setWebViewClient(new OtplessWebClient());
    }

    // for oreo and samsung and oppo devices autofill is suppressed
    protected boolean shouldDisableAutofill() {
        final String brand = Build.MANUFACTURER.toLowerCase();
        return (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) &&
                ("samsung".equals(brand) || "oppo".equals(brand));
    }

    private void injectJavaScript() {
        final String jsStr = "javascript: " +
                "window.androidObj.nativeSupport = function(message) { " +
                JAVASCRIPT_OBJ + ".nativeSupport(message) }";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(jsStr, null);
        } else {
            loadUrl(jsStr);
        }
    }

    public void loadWebUrl(String url) {
        if (url == null) return;
        mLoadingUrl = url;
        loadUrl(url);
    }

    public void callWebJs(final String methodName, final Object... params) {
        final StringBuilder builder = new StringBuilder();
        for (Object obj : params) {
            if (obj instanceof String) {
                final String quotedString = "\\'" + obj + "\\'";
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
            evaluateJavascript(script, null);
        } else {
            loadUrl(script);
        }
    }

    public void attachNativeWebManager(final OtplessWebListener manager) {
        final WebJsInterface webJsInterface = new WebJsInterface(manager);
        addJavascriptInterface(webJsInterface, JAVASCRIPT_OBJ);
    }

    public void detachNativeWebManager() {
        removeJavascriptInterface(JAVASCRIPT_OBJ);
    }

    private class OtplessWebClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (hasUrlLoadFailed) {
                if (webLoaderCallback != null) {
                    webLoaderCallback.showLoader();
                }
            } else {
                // inject java script object here
                if (webLoaderCallback != null) {
                    webLoaderCallback.hideLoader();
                }
                injectJavaScript();
            }
        }

        @Override
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (request.getUrl() != null && request.getUrl().toString().equals(mLoadingUrl)) {
                hasUrlLoadFailed = true;
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (failingUrl != null && failingUrl.equals(mLoadingUrl)) {
                hasUrlLoadFailed = true;
            }
        }
    }
}
