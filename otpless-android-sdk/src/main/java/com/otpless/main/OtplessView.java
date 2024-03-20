package com.otpless.main;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.otpless.dto.OtplessRequest;
import com.otpless.views.FabButtonAlignment;
import com.otpless.views.OtplessUserDetailCallback;

import org.json.JSONObject;

public interface OtplessView {
    /// explicitly setting the callback
    void setCallback(@NonNull final OtplessRequest request, final OtplessUserDetailCallback callback);

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
    @Deprecated
    void showOtplessFab(boolean isToShow);

    /// to set the position of otpless fab button
    @Deprecated
    void setFabConfig(final FabButtonAlignment alignment, final int sideMargin, final int bottomMargin);

    /// removes fab button if added on login screens window
    @Deprecated
    void onSignInCompleted();

    /// to change the text of sign in fab button
    @Deprecated
    void setFabText(final String text);

    /// to show otpless login page
    void showOtplessLoginPage(@NonNull final OtplessRequest request, OtplessUserDetailCallback callback);

    default void setLoaderVisibility(final boolean isVisible) {}

    default void setRetryVisibility(final boolean isVisible) {}

    void hideContainerView();

    default void onActivityResult(final int requestCode, final int resultCode, final Intent data) {}
}
