package com.otpless.main;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.otpless.dto.OtplessRequest;
import com.otpless.views.FabButtonAlignment;
import com.otpless.views.OtplessUserDetailCallback;

import org.json.JSONObject;

public interface OtplessView {
    /// methods to start otpless
    @Deprecated
    void startOtpless(final JSONObject params);

    void startOtpless(final OtplessUserDetailCallback callback);

    /// method to start otpless with with json parameters
    @Deprecated
    void startOtpless(final JSONObject params, final OtplessUserDetailCallback callback);

    void startOtpless(@NonNull final OtplessRequest request, final OtplessUserDetailCallback callback);

    /// methods to start otpless
    void startOtpless();

    /// explicitly setting the callback
    @Deprecated
    void setCallback(final OtplessUserDetailCallback callback, final JSONObject extra);

    void setCallback(final OtplessUserDetailCallback callback, final JSONObject extra, final boolean isLoginPage);

    void setCallback(final OtplessUserDetailCallback callback, final boolean isLoginPage);

    void setCallback(@NonNull final OtplessRequest request, final OtplessUserDetailCallback callback, final boolean isLoginPage);

    /// explicitly closing the view
    void closeView();

    /// send the onBackPressed call from activity to here it returns false if it do not handles call
    /// and it returns true if it handles call
    boolean onBackPressed();

    /// intent to verify mainly from onNewIntent and also can be from onCreate
    boolean verifyIntent(Intent intent);

    /// to receive the events in apps
    void setEventCallback(final OtplessEventCallback callback);

    void setBackBackButtonSubscription(final boolean backSubscription);

    /// to configure the visibility of otpless fab button
    void showOtplessFab(boolean isToShow);

    /// to set the position of otpless fab button
    void setFabConfig(final FabButtonAlignment alignment, final int sideMargin, final int bottomMargin);

    /// removes fab button if added on login screens window
    void onSignInCompleted();

    /// to change the text of sign in fab button
    void setFabText(final String text);

    /// to show otpless login page with extra and callback
    void showOtplessLoginPage(final JSONObject extra, OtplessUserDetailCallback callback);

    void showOtplessLoginPage(@NonNull final OtplessRequest request, OtplessUserDetailCallback callback);

    /// to show otpless login page with callback
    void showOtplessLoginPage(OtplessUserDetailCallback callback);

    // to show otpless login page if callback is already set
    void showOtplessLoginPage();

    default void setLoaderVisibility(final boolean isVisible) {}

    default void setRetryVisibility(final boolean isVisible) {}
}
