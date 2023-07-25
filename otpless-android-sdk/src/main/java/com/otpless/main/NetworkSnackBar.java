package com.otpless.main;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class NetworkSnackBar {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @NonNull
    private final FrameLayout parentView;

    public static NetworkSnackBar createView(@NonNull final View parentView) {
        View contentView = parentView.findViewById(android.R.id.content);
        if (contentView instanceof FrameLayout) {
            return new NetworkSnackBar((FrameLayout) contentView);
        }
        do {
            final ViewParent parent = contentView.getParent();
            // check if parent is frame layout and android content view
            if (parent instanceof FrameLayout && ((FrameLayout) parent).getId() == android.R.id.content) {
                return new NetworkSnackBar((FrameLayout) parent);
            }
            // check if it is view or null
            if (parent instanceof View) {
                contentView = (View) parent;
            } else {
                contentView = null;
            }
        } while (contentView != null);
        return null;
    }

    private NetworkSnackBar(@NonNull FrameLayout parentView) {
        this.parentView = parentView;
    }

    public void showText(final String text, final String color, final long time) {
        NetworkStatusView networkStatusView = parentView.findViewWithTag("otpless_network_status_vw");
        if (networkStatusView == null) {
            networkStatusView = new NetworkStatusView(parentView.getContext());
            networkStatusView.setTag("otpless_network_status_vw");
            networkStatusView.setVisibility(View.VISIBLE);
            parentView.addView(networkStatusView);
        }
        Integer colorInt = null;
        if (color != null) {
            try {
                colorInt = Color.parseColor(color);
            } catch (Exception ignore) {
            }
        }
        networkStatusView.setText(text, colorInt);
        mHandler.removeCallbacksAndMessages(null);
        // set the callback handler
        if (time != 0) {
            mHandler.postDelayed(this::remove, time);
        }
    }

    public void remove() {
        final View statusView = parentView.findViewWithTag("otpless_network_status_vw");
        if (statusView != null) {
            parentView.removeView(statusView);
        }
    }
}
