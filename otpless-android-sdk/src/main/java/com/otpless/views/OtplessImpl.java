package com.otpless.views;


import android.app.Activity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessEventCallback;
import com.otpless.main.OtplessEventData;
import com.otpless.main.OtplessWebResultContract;
import com.otpless.utils.Utility;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

class OtplessImpl implements LifecycleObserver {

    private OtplessUserDetailCallback mAfterLaunchCallback = null;
    private OtplessEventCallback mEventCallback = null;
    protected ActivityResultLauncher<JSONObject> mWebLaunch;
    private JSONObject mExtraParams;
    protected WeakReference<Button> wFabButton = new WeakReference<>(null);
    protected WeakReference<ViewGroup> wDecorView = new WeakReference<>(null);
    protected boolean mShowOtplessFab = true;
    private static final int ButtonWidth = 120;
    private static final int ButtonHeight = 40;

    private FabButtonAlignment mAlignment = FabButtonAlignment.BottomRight;
    private int mBottomMargin = 24;
    private int mSideMargin = 16;
    protected String mFabText = "Sign in";

    @NonNull
    protected WeakReference<Activity> wActivity = new WeakReference<>(null);

    OtplessImpl() {
    }

    void initWebLauncher(final ComponentActivity activity) {
        mWebLaunch = activity.registerForActivityResult(
                new OtplessWebResultContract(), this::onOtplessResult
        );
        wActivity = new WeakReference<>(activity);
        Utility.addContextInfo(activity);
        activity.getLifecycle().addObserver(this);
    }

    private void onOtplessResult(@NonNull OtplessResponse userDetail) {
        if (mAfterLaunchCallback != null) {
            mAfterLaunchCallback.onOtplessUserDetail(userDetail);
        }
        final Button button = wFabButton.get();
        if (button != null) {
            if (!mShowOtplessFab) {
                // remove the fab button
                final ViewGroup parent = wDecorView.get();
                if (parent == null) return;
                parent.removeView(button);
                return;
            }
            // make button visible after first callback
            button.setVisibility(View.VISIBLE);
            button.setText(mFabText);
            return;
        }
        if (wActivity.get() == null || !mShowOtplessFab) return;
        addButtonOnDecor(wActivity.get());
    }

    void startOtpless(final OtplessUserDetailCallback callback, final JSONObject params) {
        mAfterLaunchCallback = callback;
        mExtraParams = params;
        final View button = wFabButton.get();
        if (button != null) {
            // make button invisible after first callback
            button.setVisibility(View.INVISIBLE);
        }
        mWebLaunch.launch(params);
    }

    @SuppressWarnings("unused")
    void setFabConfig(final FabButtonAlignment alignment, final int sideMargin, final int bottomMargin) {
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

    @SuppressWarnings("unused")
    void showOtplessFab(boolean isToShow) {
        this.mShowOtplessFab = isToShow;
    }

    protected void onFabButtonClicked() {
        final View fBtn = wFabButton.get();
        if (fBtn != null) {
            // make button invisible after first callback
            fBtn.setVisibility(View.INVISIBLE);
        }
        mWebLaunch.launch(mExtraParams);
    }

    protected void addButtonOnDecor(final Activity activity) {
        if (wFabButton.get() != null) return;
        final ViewGroup parentView = (ViewGroup) activity.findViewById(android.R.id.content);
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
        wDecorView = new WeakReference<>(parentView);
    }

    private int dpToPixel(int dp) {
        final Activity activity = wActivity.get();
        if (activity == null) return 0;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, activity.getResources().getDisplayMetrics());
    }

    void setFabText(@NonNull final String text) {
        this.mFabText = text;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @SuppressWarnings("unused")
    public void clearReferences() {
        mWebLaunch = null;
        mAfterLaunchCallback = null;
        mExtraParams = null;
        mEventCallback = null;
    }

    void setEventCallback(final OtplessEventCallback callback) {
        mEventCallback = callback;
    }

    void sendOtplessEvent(final OtplessEventData event) {
        if (mEventCallback == null) return;
        // event code in case of no internet connection
        if (event.getEventCode() == 101) {
            mEventCallback.onInternetError();
        } else {
            mEventCallback.onOtplessEvent(event);
        }
    }

    protected void onSignInCompleted() {
        // removing fab button
        final Activity activity = wActivity.get();
        if (activity == null) return;
        final Button fab = wFabButton.get();
        if (fab == null) return;
        final ViewGroup decorView = wDecorView.get();
        if (decorView == null) return;
        final ViewGroup parentView = (ViewGroup) activity.findViewById(android.R.id.content);
        if (parentView == null) return;
        parentView.removeView(fab);
        wFabButton = new WeakReference<>(null);
    }
}

