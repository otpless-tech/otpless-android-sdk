package com.otpless.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.otpless.R;
import com.otpless.dto.OtplessResponse;
import com.otpless.network.ApiCallback;
import com.otpless.network.ApiManager;
import com.otpless.views.OtplessManager;
import com.otpless.views.OtplessUserDetailCallback;

import org.json.JSONObject;

class UtilityImpl {

    static void verifyOtplessIntent(final Activity activity, final Intent intent, @NonNull final OtplessUserDetailCallback callback) {
        if (intent == null || intent.getData() == null || !"otpless".equals(intent.getData().getHost())) {
            removeLoader(activity);
            return;
        }
        String waId = intent.getData().getQueryParameter("waId");
        if (waId == null || waId.length() == 0) {
            callback.onOtplessUserDetail(
                    createError("Waid is null")
            );
            removeLoader(activity);
            return;
        }
        // check the validity of waId with otpless
        Log.d("Otpless-api", "waid verification started");
        ApiManager.getInstance().verifyWaId(
                waId, new ApiCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        final OtplessResponse response = new OtplessResponse();
                        response.setStatus("success");
                        response.setWaId(waId);
                        String userNumber = Utility.parseUserNumber(data);
                        response.setUserNumber(userNumber);
                        Log.d("Otpless-api", "waid verification success");
                        if (activity.isFinishing()) return;
                        callback.onOtplessUserDetail(
                                response
                        );
                        Log.d("Otpless-api", "waid verification callback sent");
                        removeLoader(activity);
                    }

                    @Override
                    public void onError(Exception exception) {
                        exception.printStackTrace();
                        Log.d("Otpless-api", "waid verification error");
                        if (activity.isFinishing()) return;
                        callback.onOtplessUserDetail(
                                createError(exception.getMessage())
                        );
                        Log.d("Otpless-api", "waid verification callback sent");
                        removeLoader(activity);
                    }
                }
        );
    }

    static OtplessResponse createError(final String error) {
        final OtplessResponse response = new OtplessResponse();
        response.setStatus("failed");
        response.setMessage(error);
        return response;
    }

    static void addLoader(final Activity activity) {
        final Window window = activity.getWindow();
        if (window == null) return;
        final View decorView = window.getDecorView();
        if (!(decorView instanceof ViewGroup)) return;
        final ViewGroup contentView = (ViewGroup) decorView;
        FrameLayout loader = contentView.findViewWithTag("otpless_loader_view");
        if (loader != null) return;
        loader = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.otpless_loader, contentView, false);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        loader.setLayoutParams(params);
        loader.setTag("otpless_loader_view");
        loader.findViewById(R.id.cancel_tv).setOnClickListener((v) -> {
            removeLoader(activity);
            if (OtplessManager.initCallback != null) {
                OtplessManager.initCallback.onOtplessUserDetail(
                        createError("user cancelled")
                );
            }
        });
        contentView.addView(loader);
    }

    static void removeLoader(final Activity activity) {
        activity.runOnUiThread(() -> {
            rmLoader(activity);
        });
    }

    static boolean onBackPress(final Activity activity) {
        return rmLoader(activity);
    }

    private static boolean rmLoader(final Activity activity) {
        final Window window = activity.getWindow();
        if (window == null) return false;
        final View decorView = window.getDecorView();
        if (!(decorView instanceof ViewGroup)) return false;
        final ViewGroup contentView = (ViewGroup) decorView;
        FrameLayout loader = contentView.findViewWithTag("otpless_loader_view");
        if (loader != null) {
            contentView.removeView(loader);
            return true;
        }
        return false;
    }
}
