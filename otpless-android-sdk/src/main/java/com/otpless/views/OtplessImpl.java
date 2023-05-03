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
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessWebResultContract;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

class OtplessImpl {

    private OtplessUserDetailCallback mAfterLaunchCallback = null;
    private ActivityResultLauncher<JSONObject> mWebLaunch;
    private JSONObject mExtraParams;
    private View mFabButton;
    private static final int ButtonWidth = 120;
    private static final int ButtonHeight = 40;

    private FabButtonAlignment mAlignment = FabButtonAlignment.BottomRight;
    private int mBottomMargin = 24;
    private int mSideMargin = 16;

    @NonNull
    private WeakReference<FragmentActivity> wActivity = new WeakReference<>(null);

    OtplessImpl() {
    }

    void initWebLauncher(final FragmentActivity activity) {
        mWebLaunch = activity.registerForActivityResult(
                new OtplessWebResultContract(), this::onOtplessResult
        );
        wActivity = new WeakReference<>(activity);
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (mAfterLaunchCallback != null) {
            mAfterLaunchCallback.onOtplessUserDetail(userDetail);
        }
        if (mFabButton != null) {
            // make button visible after first callback
            mFabButton.setVisibility(View.VISIBLE);
        } else {
            if (wActivity.get() == null) return;
            addButtonOnDecor(wActivity.get());
        }
    }

    void startOtpless(final OtplessUserDetailCallback callback, final JSONObject params) {
        mAfterLaunchCallback = callback;
        mExtraParams = params;
        if (mFabButton != null) {
            // make button invisible after first callback
            mFabButton.setVisibility(View.INVISIBLE);
        }
        mWebLaunch.launch(params);
    }

    private void addButtonOnDecor(final FragmentActivity activity) {
        if (mFabButton != null) return;
        final View decorView = activity.getWindow().getDecorView();
        if (decorView == null) return;
        final ViewGroup parentView = findSuitableParent(decorView);
        if (parentView == null) return;
        final Button button = (Button) activity.getLayoutInflater().inflate(R.layout.otpless_fab_button, parentView, false);
        button.setOnClickListener(v -> mWebLaunch.launch(mExtraParams));

        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        // region add the margin
        final Resources resources = activity.getResources();
        final DisplayMetrics matrix = resources.getDisplayMetrics();
        final int screenWidth = matrix.widthPixels;
        final int screenHeight = matrix.heightPixels;
        final int buttonWidth = dpToPixel(ButtonWidth);
        final int buttonHeight = dpToPixel(ButtonHeight);
        switch (mAlignment) {
            case Center: {
                // in center case draw of button will be
                int x = (screenWidth - buttonWidth) / 2;
                int y = (screenHeight - buttonHeight) / 2;
                params.setMargins(x, y, 0, 0);
            }
            break;
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
        mFabButton = button;
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

    public void setFabConfig(FabButtonAlignment alignment, int sideMargin, int bottomMargin) {
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

    private int dpToPixel(int dp) {
        final Activity activity = wActivity.get();
        if (activity == null) return 0;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, activity.getResources().getDisplayMetrics());
    }
}

