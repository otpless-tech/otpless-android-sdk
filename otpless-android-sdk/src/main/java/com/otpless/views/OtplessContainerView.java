package com.otpless.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.R;
import com.otpless.dto.HeadlessResponse;
import com.otpless.main.OtplessEventCode;
import com.otpless.main.OtplessEventData;
import com.otpless.main.OtplessViewContract;
import com.otpless.main.WebActivityContract;
import com.otpless.utils.Utility;
import com.otpless.web.LoadingStatus;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;
import com.otpless.web.OtplessWebViewWrapper;

import org.json.JSONException;
import org.json.JSONObject;

public class OtplessContainerView extends FrameLayout implements WebActivityContract {

    private FrameLayout parentVg;
    private OtplessWebView webView;
    private NativeWebManager webManager;

    private OtplessViewContract viewContract;

    public boolean isToShowLoader = true;
    public boolean isToShowRetry = true;
    public boolean isHeadless = false;
    @Nullable
    private JSONObject mColorConfig;

    //region otpless loader properties
    private TextView mInfoTv, mCloseTv;
    private ProgressBar mOtplessProgress;
    private Button mRetryButton;
    private FrameLayout mLoaderContainerFl;
    //endregion

    public OtplessContainerView(@NonNull Context context) {
        super(context);
        initView(null);
    }

    public OtplessContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public OtplessContainerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(@Nullable AttributeSet attrs) {
        // set the layout parameters
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);
        // inflate the layout and add here
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.otpless_content_view, this, false);
        addView(view);
        // assigning all the view
        parentVg = view.findViewById(R.id.otpless_parent_vg);
        OtplessWebViewWrapper webViewWrapper = view.findViewById(R.id.otpless_web_wrapper);
        webView = webViewWrapper.getWebView();
        if (webView == null) {
            final JSONObject errorJson = new JSONObject();
            try {
                errorJson.put("error", "Error in loading web. Please try again later.");
            } catch (JSONException ignore) {
            }
            onVerificationResult(
                    Activity.RESULT_CANCELED, errorJson
            );
            return;
        }
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.otpless_slide_up_anim);
        parentVg.startAnimation(animation);
        //region setting page loader related configuration
        mLoaderContainerFl = view.findViewById(R.id.otpless_loader_container_fl);
        mCloseTv = view.findViewById(R.id.otpless_close_tv);
        mInfoTv = view.findViewById(R.id.otpless_info_tv);
        mOtplessProgress = view.findViewById(R.id.otpless_progress_bar);
        mRetryButton = view.findViewById(R.id.otpless_retry_btn);

        mCloseTv.setOnClickListener(v -> onOtplessLoaderEvent(OtplessLoaderEvent.CLOSE));
        mRetryButton.setOnClickListener(v -> onOtplessLoaderEvent(OtplessLoaderEvent.RETRY));
        //endregion
        webView.pageLoadStatusCallback = (loadingStatus -> {
            switch (loadingStatus.getLoadingStatus()) {
                case InProgress:
                case Started:
                    if (!isToShowLoader) return;
                    showLoader();
                    break;
                case Failed:
                    if (loadingStatus.getLoadingStatus() == LoadingStatus.Failed) {
                        //region send event because of error
                        final int errorCode = loadingStatus.getErrorCode();
                        if (errorCode == WebViewClient.ERROR_CONNECT || errorCode == WebViewClient.ERROR_TIMEOUT ||
                                errorCode == WebViewClient.ERROR_HOST_LOOKUP || errorCode == WebViewClient.ERROR_BAD_URL ||
                                errorCode == WebViewClient.ERROR_UNKNOWN) {
                            if (webManager != null && webManager.getNativeWebListener() != null) {
                                final JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("errorCode", loadingStatus.getErrorCode());
                                    jsonObject.put("description", loadingStatus.getDescription());
                                } catch (JSONException ignore) {
                                }
                                final OtplessEventData eventData = new OtplessEventData(
                                        OtplessEventCode.NO_INTERNET, jsonObject
                                );
                                webManager.getNativeWebListener().onOtplessEvent(
                                        eventData
                                );
                            }
                        }
                        //endregion
                    }
                    if (!isToShowRetry) {
                        hideLoader();
                        break;
                    }
                    String errorMessage = loadingStatus.getMessage();
                    if (errorMessage == null) {
                        // shield case
                        errorMessage = "Connection error : Failed to connect";
                    }
                    showRetry(errorMessage);
                    break;
                case Success:
                    if (!isToShowLoader) return;
                    hideLoader();
            }
        });
        webManager = new NativeWebManager((Activity) getContext(), this.webView, this);
        this.webView.attachNativeWebManager(webManager);
    }

    private void onOtplessLoaderEvent(final OtplessLoaderEvent event) {
        switch (event) {
            case CLOSE:
                final JSONObject errorJson = new JSONObject();
                try {
                    errorJson.put("error", "User cancelled.");
                } catch (JSONException ignore) {
                }
                Utility.pushEvent("user_abort_connection_error");
                onVerificationResult(
                        Activity.RESULT_CANCELED, errorJson
                );
                break;
            case RETRY:
                this.webView.loadWebUrl(this.webView.getLoadedUrl());
        }
    }

    private void showLoader() {
        // only progress bar will be visible rest view content will be hidden
        this.mLoaderContainerFl.setVisibility(View.VISIBLE);
        this.mOtplessProgress.setVisibility(View.VISIBLE);
        this.mInfoTv.setVisibility(View.GONE);
        this.mRetryButton.setVisibility(View.GONE);
        this.mCloseTv.setVisibility(View.GONE);
    }

    private void hideLoader() {
        this.mLoaderContainerFl.setVisibility(View.GONE);
    }

    private void showRetry(final String message) {
        // every item will be visible apart from progress bar
        this.mLoaderContainerFl.setVisibility(View.VISIBLE);
        this.mInfoTv.setVisibility(View.VISIBLE);
        this.mInfoTv.setText(message);
        this.mRetryButton.setVisibility(View.VISIBLE);
        this.mOtplessProgress.setVisibility(View.GONE);
        this.mCloseTv.setVisibility(View.VISIBLE);
    }

    public NativeWebManager getWebManager() {
        return webManager;
    }

    public OtplessWebView getWebView() {
        return webView;
    }

    @Override
    public ViewGroup getParentView() {
        return this;
    }

    @Override
    public JSONObject getExtraParams() {
        if (this.viewContract != null) {
            return this.viewContract.getExtraParams();
        }
        return null;
    }

    @Override
    public void closeView() {
        if (this.viewContract != null) {
            this.viewContract.closeView();
        }
    }

    @Override
    public void onVerificationResult(int resultCode, JSONObject jsonObject) {
        if (this.viewContract != null) {
            this.viewContract.onVerificationResult(resultCode, jsonObject);
        }
    }

    public void setViewContract(OtplessViewContract viewContract) {
        this.viewContract = viewContract;
    }

    public void setUiConfiguration(final JSONObject extras) {
        if (extras == null) return;
        try {
            mColorConfig = extras.getJSONObject("params");
            ColorUtils.parseColor(this.mColorConfig.optString("primaryColor"), (primaryColor) -> {
                //region ==== creating color state list
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_enabled}, // enabled
                        new int[]{-android.R.attr.state_enabled}, // disabled
                        new int[]{android.R.attr.state_pressed}  // pressed
                };
                int[] colors = new int[]{
                        primaryColor,
                        primaryColor,
                        primaryColor
                };
                final ColorStateList colorStateList = new ColorStateList(states, colors);
                //endregion
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.mRetryButton.setBackgroundTintList(colorStateList);
                } else {
                    this.mRetryButton.setBackgroundColor(primaryColor);
                }
            });
            // parse close button text color
            ColorUtils.parseColor(this.mColorConfig.optString("closeButtonColor"), (closeButtonColor) -> {
                this.mCloseTv.setTextColor(closeButtonColor);
            });
            // parse loader color and set to progress bar
            ColorUtils.parseColor(this.mColorConfig.optString("loaderColor"), (loaderColor) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mOtplessProgress.setIndeterminateTintList(ColorStateList.valueOf(loaderColor));
                }
            });
            // parse text color and set it info text and retry button text
            ColorUtils.parseColor(this.mColorConfig.optString("textColor"), (textColor) -> {
                this.mInfoTv.setTextColor(textColor);
                this.mRetryButton.setTextColor(textColor);
            });
            // checking parsing for alpha for loader background
            final String alphaString = this.mColorConfig.optString("loaderAlpha");
            if (!alphaString.isEmpty()) {
                try {
                    final float alpha = Float.parseFloat(alphaString);
                    mLoaderContainerFl.setAlpha(alpha);
                } catch (Exception ignore){}
            }
        } catch (JSONException ignore) {
        }
    }

    public void enableHeadlessConfig() {
        this.isHeadless = true;
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.height = 0;
        this.setLayoutParams(params);
    }

    public void disableHeadlessConfig() {
        this.isHeadless = false;
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        this.setLayoutParams(params);
    }

    @Override
    public void onHeadlessResult(HeadlessResponse response, boolean closeView) {
        if (this.viewContract != null) this.viewContract.onHeadlessResult(response, closeView);
    }
}
