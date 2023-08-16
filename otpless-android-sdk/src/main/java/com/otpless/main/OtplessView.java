package com.otpless.main;

import android.content.Intent;

import com.otpless.views.FabButtonAlignment;
import com.otpless.views.OtplessUserDetailCallback;

import org.json.JSONObject;

public interface OtplessView {
    /// methods to start otpless
    void startOtpless(final JSONObject params);

    /// method to start otpless with with json parameters
    void startOtpless(final JSONObject params, final OtplessUserDetailCallback callback);

    /// explicitly setting the callback
    void setCallback(final OtplessUserDetailCallback callback, final JSONObject extra);

    void setCallback(final OtplessUserDetailCallback callback, final JSONObject extra, final boolean isLoginPage);

    /// explicitly closing the view
    void closeView();

    /// send the onBackPressed call from activity to here it returns false if it do not handles call
    /// and it returns true if it handles call
    boolean onBackPressed();

    /// intent to verify mainly from onNewIntent and also can be from onCreate
    boolean verifyIntent(Intent intent);

    /// to receive the events in apps
    void setEventCallback(final OtplessEventCallback callback);

    /// to configure the visibility of otpless fab button
    void showOtplessFab(boolean isToShow);

    /// to set the position of otpless fab button
    void setFabConfig(final FabButtonAlignment alignment, final int sideMargin, final int bottomMargin);

    /// removes fab button if added on login screens window
    void onSignInCompleted();

    /// to change the text of sign in fab button
    void setFabText(final String text);

    void showOtplessLoginPage(final JSONObject extra, OtplessUserDetailCallback callback);

    void showOtplessLoginPage(OtplessUserDetailCallback callback);
}
