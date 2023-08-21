package com.otpless.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.R;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessViewContract;
import com.otpless.main.WebActivityContract;
import com.otpless.web.LoadingStatus;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;
import com.otpless.web.OtplessWebViewWrapper;

import org.json.JSONException;
import org.json.JSONObject;

public class OtplessContainerView extends FrameLayout implements WebActivityContract {

    private FrameLayout parentVg;
    private ProgressBar progressBar;
    private OtplessWebView webView;

    private NativeWebManager webManager;

    private OtplessViewContract viewContract;
    private TextView networkTv;

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
        progressBar = view.findViewById(R.id.otpless_progress_bar);
        OtplessWebViewWrapper webViewWrapper = view.findViewById(R.id.otpless_web_wrapper);
        networkTv = view.findViewById(R.id.otpless_no_internet_tv);
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
        if (OtplessManager.getInstance().isToShowPageLoader()) {
            webView.pageLoadStatusCallback = (loadingStatus -> {
                if (loadingStatus == LoadingStatus.InProgress) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
        webManager = new NativeWebManager((Activity) getContext(), this.webView, this);
        this.webView.attachNativeWebManager(webManager);
    }

    public NativeWebManager getWebManager() {
        return webManager;
    }

    public OtplessWebView getWebView() {
        return webView;
    }

    @Override
    public ViewGroup getParentView() {
        return this.parentVg;
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

    public void showNoNetwork(final String error) {
        if (networkTv != null) {
            networkTv.setVisibility(View.VISIBLE);
            networkTv.setText(error);
        }
    }

    public void hideNoNetwork() {
        if (networkTv != null) {
            networkTv.setVisibility(View.GONE);
        }
    }
}
