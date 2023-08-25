package com.otpless.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.network.NetworkStatusData;
import com.otpless.network.ONetworkStatus;
import com.otpless.network.OnConnectionChangeListener;
import com.otpless.network.OtplessNetworkManager;
import com.otpless.utils.Utility;
import com.otpless.views.FabButtonAlignment;
import com.otpless.views.OtplessContainerView;
import com.otpless.views.OtplessUserDetailCallback;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

final class OtplessViewImpl implements OtplessView, OtplessViewContract, OnConnectionChangeListener, NativeWebListener {

    private static final String VIEW_TAG_NAME = "OtplessView";

    private final Activity activity;
    private JSONObject extras;

    private WeakReference<OtplessContainerView> wContainer = new WeakReference<>(null);
    private OtplessUserDetailCallback detailCallback;
    private OtplessEventCallback eventCallback;
    private FabButtonAlignment mAlignment = FabButtonAlignment.BottomRight;
    private int mBottomMargin = 24;
    private int mSideMargin = 16;
    private String mFabText = "Sign in";
    private boolean mShowOtplessFab = true;
    private WeakReference<Button> wFabButton = new WeakReference<>(null);
    private static final int ButtonWidth = 120;
    private static final int ButtonHeight = 40;

    private boolean isLoginPageEnabled = false;

    private final Queue<ViewGroup> helpQueue = new PriorityQueue<>();

    OtplessViewImpl(final Activity activity) {
        this.activity = activity;
    }

    @Override
    public void startOtpless(JSONObject params) {
        this.extras = params;
        this.isLoginPageEnabled = false;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void startOtpless(JSONObject params, OtplessUserDetailCallback callback) {
        this.detailCallback = callback;
        this.extras = params;
        this.isLoginPageEnabled = false;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    private void startOtpless() {
        this.isLoginPageEnabled = false;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    private void loadWebView(final String baseUrl, Uri uri) {
        if (baseUrl == null) {
            ApiManager.getInstance().apiConfig(new ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject data) {
                    // check for fab button text
                    final String fabText = data.optString("button_text");
                    if (!fabText.isEmpty()) {
                        mFabText = fabText;
                    }
                    // check for url
                    final String url = data.optString("auth");
                    final OtplessContainerView containerView = wContainer.get();
                    if (containerView == null || containerView.getWebView() == null) return;
                    String firstLoadingUrl;
                    if (!url.isEmpty()) {
                        firstLoadingUrl = getFirstLoadingUrl(url, extras);
                    } else {
                        firstLoadingUrl = getFirstLoadingUrl("https://otpless.com/mobile/index.html", extras);
                    }
                    if (uri == null) {
                        containerView.getWebView().loadWebUrl(firstLoadingUrl);
                    } else {
                        reloadToVerifyCode(containerView.getWebView(), uri, firstLoadingUrl);
                    }
                }

                @Override
                public void onError(Exception exception) {
                    final OtplessContainerView containerView = wContainer.get();
                    if (containerView == null || containerView.getWebView() == null) return;
                    final String loadingUrl = getFirstLoadingUrl("https://otpless.com/mobile/index.html", extras);
                    containerView.getWebView().loadWebUrl(loadingUrl);
                    if (containerView.getWebManager() != null) {
                        containerView.getWebManager().setNativeWebListener(OtplessViewImpl.this);
                    }
                }
            });
        } else if (uri == null) {
            final OtplessContainerView containerView = wContainer.get();
            if (containerView == null || containerView.getWebView() == null) return;
            containerView.getWebView().loadWebUrl(baseUrl);
        } else {
            final OtplessContainerView containerView = wContainer.get();
            if (containerView == null || containerView.getWebView() == null) return;
            reloadToVerifyCode(containerView.getWebView(), uri, baseUrl);
        }
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
        if (isLoginPageEnabled) {
            urlToLoad.appendQueryParameter("lp", String.valueOf(true));
        }
        urlToLoad.appendQueryParameter("login_uri", loginUrl);
        return urlToLoad.build().toString();
    }

    @Override
    public void setCallback(final OtplessUserDetailCallback callback, final JSONObject extra) {
        this.setCallback(callback, extra, false);
    }

    @Override
    public void setCallback(OtplessUserDetailCallback callback, JSONObject extra, boolean isLoginPage) {
        this.detailCallback = callback;
        this.extras = extra;
        this.isLoginPageEnabled = isLoginPage;
    }

    @Override
    public void closeView() {
        removeView();
    }

    @Override
    public void onVerificationResult(int resultCode, JSONObject jsonObject) {
        // if login page is not enable and show button is enabled
        // show sign in button
        if (!isLoginPageEnabled && mShowOtplessFab) {
            // check if button is already added
            final Button btn = wFabButton.get();
            if (btn == null) {
                addFabOnDecor();
            } else {
                btn.setVisibility(View.VISIBLE);
            }
        }
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
            // if adding fab on decor has been added
        }
        removeView();
    }

    @Override
    public JSONObject getExtraParams() {
        return this.extras;
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
        } else {
            // remove the view
            onVerificationResult(Activity.RESULT_CANCELED, null);
        }
        return true;
    }

    @Override
    public boolean verifyIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) return false;
        if (!"otpless".equals(uri.getHost())) return false;
        // check if web view is already loaded or not if webview is loaded then reload the url
        final OtplessContainerView otplessContainerView = wContainer.get();
        if (otplessContainerView != null && otplessContainerView.getWebView() != null) {
            final OtplessWebView webView = wContainer.get().getWebView();
            final String loadedUrl = webView.getLoadedUrl();
            loadWebView(loadedUrl, uri);
        } else {
            // add view if not added
            Log.d(VIEW_TAG_NAME, "adding the view in low memory case.");
            addViewIfNotAdded();
            loadWebView(null, uri);
        }
        return true;
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
        final ViewGroup parent = findParentView();
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
        // adding listener to the data components from this class
        if (containerView.getWebManager() != null) {
            containerView.getWebManager().setNativeWebListener(OtplessViewImpl.this);
        }
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
        final ViewGroup parent = findParentView();
        if (parent == null) return;
        // search and remove the view
        View container = parent.findViewWithTag(VIEW_TAG_NAME);
        if (container != null) {
            parent.removeView(container);
            OtplessNetworkManager.getInstance().removeListener(activity, this);
        }
    }

    private ViewGroup findParentView() {
        final Window window = activity.getWindow();
        if (window == null) return null;
        final View decorView = window.getDecorView();
        if (!(decorView instanceof ViewGroup)) return null;
        helpQueue.clear();
        helpQueue.add((ViewGroup) decorView);
        return findFrameLayout();
    }

    // using bfs to find the view from decor
    private ViewGroup findFrameLayout() {
        final ViewGroup group = helpQueue.poll();
        if (group == null) return null;
        final int childCount = group.getChildCount();
        int index = 0;
        while (index < childCount) {
            final View v = group.getChildAt(index);
            if (v.getId() == android.R.id.content) {
                return (ViewGroup) (v);
            }
            if (v instanceof FrameLayout) {
                return (FrameLayout)(v);
            } else if (v instanceof ViewGroup) {
                helpQueue.add((ViewGroup) v);
            }
            index++;
        }
        return findFrameLayout();
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

    @Override
    public void setFabConfig(final FabButtonAlignment alignment, final int sideMargin, final int bottomMargin) {
        mAlignment = alignment;
        switch (alignment) {
            case BottomLeft:
            case BottomRight: {
                if (sideMargin > 0) {
                    mSideMargin = sideMargin;
                }
                if (bottomMargin > 0) {
                    mBottomMargin = bottomMargin;
                }
            }
            break;
            case BottomCenter:
                if (bottomMargin > 0) {
                    mBottomMargin = bottomMargin;
                }
        }
    }

    @Override
    public void showOtplessFab(boolean isToShow) {
        this.mShowOtplessFab = isToShow;
    }

    private void addFabOnDecor() {
        if (wFabButton.get() != null) return;
        final ViewGroup parentView = activity.findViewById(android.R.id.content);
        if (parentView == null) return;
        final Button button = (Button) activity.getLayoutInflater().inflate(R.layout.otpless_fab_button, parentView, false);
        button.setOnClickListener(v -> onFabButtonClicked());
        button.setText(mFabText);
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        // region add the margin
        final int screenWidth = parentView.getWidth();
        final int screenHeight = parentView.getHeight();
        final int buttonWidth = dpToPixel(ButtonWidth);
        final int buttonHeight = dpToPixel(ButtonHeight);
        switch (mAlignment) {
            case Center: {
                // in center case draw of button will be
                int x = (screenWidth - buttonWidth) / 2;
                int y = ((screenHeight - buttonHeight) / 2);
                params.setMargins(x, y, 0, 0);
            }
            break;
            // margin calculation excludes the height of status bar while setting and we are calculating
            // the margin with reference to full screen that's way status bar height is added
            case BottomRight: {
                int marginEnd = dpToPixel(mSideMargin);
                int marginBottom = dpToPixel(mBottomMargin);
                int x = screenWidth - (buttonWidth + marginEnd);
                int y = screenHeight - (buttonHeight + marginBottom);
                params.setMargins(x, y, 0, 0);
            }
            break;
            case BottomLeft: {
                int marginStart = dpToPixel(mSideMargin);
                int marginBottom = dpToPixel(mBottomMargin);
                int y = screenHeight - (buttonHeight + marginBottom);
                params.setMargins(marginStart, y, 0, 0);
            }
            break;
            case BottomCenter: {
                int x = (screenWidth - buttonWidth) / 2;
                int marginBottom = dpToPixel(mBottomMargin);
                int y = screenHeight - (buttonHeight + marginBottom);
                params.setMargins(x, y, 0, 0);
            }
            break;
        }
        // endregion
        parentView.addView(button);
        wFabButton = new WeakReference<>(button);
    }

    private int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, activity.getResources().getDisplayMetrics());
    }

    private void onFabButtonClicked() {
        final View fBtn = wFabButton.get();
        if (fBtn != null) {
            // make button invisible after first callback
            fBtn.setVisibility(View.INVISIBLE);
        }
        startOtpless();
    }

    private void removeFabFromDecor() {
        final Button fab = wFabButton.get();
        if (fab == null) return;
        final ViewGroup parentView = activity.findViewById(android.R.id.content);
        if (parentView == null) return;
        parentView.removeView(fab);
        wFabButton = new WeakReference<>(null);
    }

    @Override
    public void onSignInCompleted() {
        removeFabFromDecor();
    }

    @Override
    public void setFabText(final String text) {
        if (text == null || text.length() == 0) return;
        this.mFabText = text;
    }

    @Override
    public void showOtplessLoginPage(JSONObject extra, OtplessUserDetailCallback callback) {
        this.setCallback(callback, extra, true);
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void showOtplessLoginPage(OtplessUserDetailCallback callback) {
        this.setCallback(callback, null, true);
        addViewIfNotAdded();
        loadWebView(null, null);
    }
}
