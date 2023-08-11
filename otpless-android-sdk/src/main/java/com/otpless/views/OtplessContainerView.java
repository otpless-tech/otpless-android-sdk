package com.otpless.views;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.otpless.R;
import com.otpless.main.OtplessViewContract;
import com.otpless.main.WebActivityContract;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;
import com.otpless.web.OtplessWebViewWrapper;

import org.json.JSONObject;

public class OtplessContainerView extends FrameLayout implements WebActivityContract {

    private FrameLayout parentVg;
    private ProgressBar progressBar;
    private OtplessWebViewWrapper webViewWrapper;
    private OtplessWebView webView;

    private NativeWebManager webManager;

    private FragmentActivity activity;
    private JSONObject extra;

    private OtplessViewContract viewContract;

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
        final int topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.otpless_view_status_bar_margin);
        final int bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.otpless_view_bottom_margin);
        params.setMargins(0, topMargin, 0, bottomMargin);
        setLayoutParams(params);
        // inflate the layout and add here
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_otpless_web, this, false);
        addView(view);
        // assigning all the view
        parentVg = view.findViewById(R.id.parent_vg);
        progressBar = view.findViewById(R.id.progress_bar);
        webViewWrapper = view.findViewById(R.id.otpless_web_wrapper);

        webView = webViewWrapper.getWebView();
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.otpless_slide_up_anim);
        parentVg.startAnimation(animation);
        if (webView == null) {
            // todo
        }
    }

    public void setCredentials(@NonNull final FragmentActivity activity, @NonNull final String loadingUrl, final JSONObject extra) {
        this.activity = activity;
        this.extra = extra;
        // if onFinishInflate is called before setActivity
        if (this.webView != null) {
            if (webManager == null) {
                webManager = new NativeWebManager(this.activity, this.webView, this);
                this.webView.attachNativeWebManager(webManager);
            }
            this.webView.loadWebUrl(loadingUrl);
        }
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
        return this.extra;
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
}
