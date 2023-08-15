package com.otpless.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.network.NetworkStatusData;
import com.otpless.network.ONetworkStatus;
import com.otpless.network.OnConnectionChangeListener;
import com.otpless.network.OtplessNetworkManager;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessContainerView;
import com.otpless.views.OtplessManager;
import com.otpless.views.OtplessUserDetailCallback;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Iterator;

final class OtplessViewImpl implements OtplessView, OtplessViewContract, OnConnectionChangeListener, CommDownFlow {

    private static final String VIEW_TAG_NAME = "otpless_webview_container";

    private final FragmentActivity activity;

    private WeakReference<OtplessContainerView> wContainer = new WeakReference<>(null);
    private OtplessUserDetailCallback detailCallback;
    private OtplessEventCallback eventCallback;

    OtplessViewImpl(final FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void startOtpless(JSONObject params) {
        addViewIfNotAdded();
        loadWebView(params);
    }

    @Override
    public void startOtpless(JSONObject params, OtplessUserDetailCallback callback) {
        this.detailCallback = callback;
        addViewIfNotAdded();
        loadWebView(params);
    }

    private void loadWebView(final JSONObject params) {
        ApiManager.getInstance().apiConfig(new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                // check for fab button text
                final String fabText = data.optString("button_text");
                OtplessManager.getInstance().setFabText(fabText);
                // check for url
                final String url = data.optString("auth");
                if (wContainer.get() == null && wContainer.get().getWebView() == null) return;
                final OtplessContainerView containerView = wContainer.get();
                final OtplessWebView webView = wContainer.get().getWebView();
                String firstLoadingUrl = null;
                if (!url.isEmpty()) {
                    firstLoadingUrl = getFirstLoadingUrl(url, params);
                } else {
                    firstLoadingUrl = getFirstLoadingUrl("https://otpless.com/mobile/index.html", params);
                }
                containerView.setCredentials(activity, firstLoadingUrl, params);
                if (containerView.getWebManager() != null) {
                    containerView.getWebManager().setCommDownFlow(OtplessViewImpl.this);
                }
            }

            @Override
            public void onError(Exception exception) {
                if (wContainer.get() == null && wContainer.get().getWebView() == null) return;
                final OtplessWebView webView = wContainer.get().getWebView();
                final String loadingUrl = getFirstLoadingUrl("https://otpless.com/mobile/index.html", params);
                webView.loadWebUrl(loadingUrl);
            }
        });
    }

    private String getFirstLoadingUrl(final String url, final JSONObject extraParams) {
        final String packageName = this.activity.getPackageName();
        String loginUrl = packageName + ".otpless://otpless";
        final Uri.Builder urlToLoad = Uri.parse(url).buildUpon();
        // check for additional json params while loading
        if (extraParams != null) {
            try {
                String methodName = extraParams.optString("method").toLowerCase();
                if (methodName.equals("get")) {
                    // add the params in url
                    final JSONObject params = extraParams.getJSONObject("params");
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
        urlToLoad.appendQueryParameter("hasWhatsapp", String.valueOf(Utility.isWhatsAppInstalled(activity)));
        urlToLoad.appendQueryParameter("hasOtplessApp", String.valueOf(Utility.isOtplessAppInstalled(activity)));
        urlToLoad.appendQueryParameter("login_uri", loginUrl);
        return urlToLoad.build().toString();
    }

    @Override
    public void setCallback(OtplessUserDetailCallback callback) {
        this.detailCallback = callback;
    }

    @Override
    public void closeView() {
        removeView();
    }

    @Override
    public void onVerificationResult(int resultCode, JSONObject jsonObject) {
        if (this.detailCallback != null) {
            final OtplessResponse response = new OtplessResponse();
            if (resultCode == Activity.RESULT_CANCELED) {
                response.setErrorMessage("user cancelled");
                this.detailCallback.onOtplessUserDetail(response);
            } else {
                // check for error on jsonObject
                final String possibleError = jsonObject.optString("error");
                if (possibleError.isEmpty()) {
                    response.setData(jsonObject);
                } else {
                    response.setErrorMessage(possibleError);
                }
                this.detailCallback.onOtplessUserDetail(response);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (wContainer.get() == null) return false;
        final NativeWebManager manager = wContainer.get().getWebManager();
        if (manager == null) return false;
        final OtplessWebView webView = wContainer.get().getWebView();
        if (webView == null) return false;
        if (manager.getBackSubscription()) {
            // back-press has been consumed
            webView.callWebJs("onHardBackPressed");
            return true;
        }
        return false;
    }

    @Override
    public void verifyIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) return;
        // getting loaded url
        if (wContainer.get() != null && wContainer.get().getWebView() != null) {
            final OtplessWebView webView = wContainer.get().getWebView();
            reloadToVerifyCode(webView, uri, webView.getLoadedUrl());
        }
    }

    private void addViewIfNotAdded() {
        // safety checks
        final Window window = activity.getWindow();
        if (window == null) {
            Utility.pushEvent("window_null");
            return;
        }
        final View decorView = window.getDecorView();
        if (decorView == null) {
            Utility.pushEvent("decorview_null");
            return;
        }
        final ViewGroup parent = findParentView(decorView);
        if (parent == null) {
            Utility.pushEvent("parent_null");
            return;
        }
        // check if view inflated is already present or not
        View _container = parent.findViewWithTag(VIEW_TAG_NAME);
        if (_container != null) return;
        // add the view
        final OtplessContainerView containerView = new OtplessContainerView(activity);
        containerView.setTag(VIEW_TAG_NAME);
        containerView.setId(View.generateViewId());
        containerView.setViewContract(this);
        parent.addView(containerView);
        wContainer = new WeakReference<>(containerView);
        // check for listener and add view
        if (OtplessNetworkManager.getInstance().getNetworkStatus().getStatus() == ONetworkStatus.DISABLED) {
            containerView.showNoNetwork("You are not connected to internet.");
        }
        OtplessNetworkManager.getInstance().addListeners(activity, this);
    }

    private void removeView() {
        // safety checks
        final Window window = activity.getWindow();
        if (window == null) return;
        final View decorView = window.getDecorView();
        if (decorView == null) return;
        final ViewGroup parent = findParentView(decorView);
        if (parent == null) return;
        // search and remove the view
        View container = parent.findViewWithTag(VIEW_TAG_NAME);
        if (container != null) {
            parent.removeView(container);
            OtplessNetworkManager.getInstance().removeListener(activity, this);
        }
    }

    private ViewGroup findParentView(final View view) {
        if (view == null) return null;
        if (!(view instanceof ViewGroup)) return null;
        ViewGroup resultView = (ViewGroup) view;
        ViewGroup fallback = null;
        do {
            if (resultView.getId() == android.R.id.content) {
                return resultView;
            }
            if (resultView instanceof FrameLayout) {
                return resultView;
            }
            final ViewParent parent = resultView.getParent();
            if (parent instanceof ViewGroup) {
                resultView = (ViewGroup) parent;
                fallback = resultView;
            } else {
                resultView = null;
            }
        } while (resultView != null);
        return fallback;
    }

    private void reloadToVerifyCode(final OtplessWebView webView, @NonNull final Uri uri, @NonNull final String loadedUrl) {
        final boolean hasCode;
        final String code = uri.getQueryParameter("code");
        hasCode = code != null && code.length() != 0;
        final Uri newUrl = Utility.combineQueries(
                Uri.parse(loadedUrl), uri
        );
        webView.loadWebUrl(newUrl.toString());
        sendIntentInEvent(hasCode);
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
        final OtplessContainerView containerView = wContainer.get();
        if (containerView == null) return;
        activity.runOnUiThread(() -> {
            if (statusData.getStatus() == ONetworkStatus.DISABLED) {
                containerView.showNoNetwork("You are not connected to internet.");
            } else if (statusData.getStatus() == ONetworkStatus.ENABLED) {
                containerView.hideNoNetwork();
            }
            // send the event call
            if (!statusData.isEnabled() && this.eventCallback != null) {
                this.eventCallback.onInternetError();
            }
        });
    }

    @Override
    public void setEventCallback(final OtplessEventCallback callback) {
        this.eventCallback = callback;
    }

    @Override
    public void onOtplessEvent(OtplessEventData event) {
        if (this.eventCallback == null) return;
        this.eventCallback.onOtplessEvent(event);
    }
}
