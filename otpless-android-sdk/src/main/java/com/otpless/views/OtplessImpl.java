package com.otpless.views;


import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessWebResultContract;
import com.otpless.utils.Utility;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

class OtplessImpl {

    private OtplessUserDetailCallback mAfterLaunchCallback = null;
    private ActivityResultLauncher<JSONObject> mWebLaunch;
    private JSONObject mExtraParams;
    private WeakReference<Button> wFabButton = new WeakReference<>(null);
    private WeakReference<ViewGroup> wDecorView = new WeakReference<>(null);
    private boolean mShowOtplessFab = true;
    private static final int ButtonWidth = 120;
    private static final int ButtonHeight = 40;

    private FabButtonAlignment mAlignment = FabButtonAlignment.BottomRight;
    private int mBottomMargin = 24;
    private int mSideMargin = 16;
    private String mFabText = "Sign in";

    @NonNull
    private WeakReference<FragmentActivity> wActivity = new WeakReference<>(null);

    OtplessImpl() {
    }

    void initWebLauncher(final FragmentActivity activity) {
        mWebLaunch = activity.registerForActivityResult(
                new OtplessWebResultContract(), this::onOtplessResult
        );
        wActivity = new WeakReference<>(activity);
        Utility.addContextInfo(activity);
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

    private void addButtonOnDecor(final FragmentActivity activity) {
        if (wFabButton.get() != null) return;
        final ViewGroup parentView = (ViewGroup) activity.findViewById(android.R.id.content);
        if (parentView == null) return;
        final Button button = (Button) activity.getLayoutInflater().inflate(R.layout.otpless_fab_button, parentView, false);
        button.setOnClickListener(v -> {
            final View fBtn = wFabButton.get();
            if (fBtn != null) {
                // make button invisible after first callback
                fBtn.setVisibility(View.INVISIBLE);
            }
            mWebLaunch.launch(mExtraParams);
        });
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

    private ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                return (ViewGroup) view;
            }
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                }
                fallback = (ViewGroup) view;
            }
            if (view != null) {
                ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);
        return fallback;
    }

    private int dpToPixel(int dp) {
        final Activity activity = wActivity.get();
        if (activity == null) return 0;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, activity.getResources().getDisplayMetrics());
    }

    void setFabText(@NonNull final String text) {
        this.mFabText = text;
    }
}

