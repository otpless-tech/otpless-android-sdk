package com.otpless.biometric.models;

import androidx.fragment.app.FragmentActivity;

import com.otpless.biometric.Constants;

public class RegisterParameters {

    public final FragmentActivity context;
    public final int sessionDurationMinutes;
    public final boolean allowFallbackToCleartext;
    public final PromptData promptData;
    public final boolean allowDeviceCredentials;

    public RegisterParameters(FragmentActivity context, int sessionDurationMinutes, boolean allowFallbackToCleartext, PromptData promptData, boolean allowDeviceCredentials) {
        this.context = context;
        this.sessionDurationMinutes = sessionDurationMinutes;
        this.allowFallbackToCleartext = allowFallbackToCleartext;
        this.promptData = promptData;
        this.allowDeviceCredentials = allowDeviceCredentials;
    }

    public RegisterParameters(FragmentActivity context, int sessionDurationMinutes, boolean allowFallbackToCleartext, PromptData promptData) {
        this(context, sessionDurationMinutes, allowFallbackToCleartext, promptData, false);
    }

    public RegisterParameters(FragmentActivity context, int sessionDurationMinutes, boolean allowFallbackToCleartext) {
        this(context, sessionDurationMinutes, allowFallbackToCleartext, null, false);
    }

    public RegisterParameters(FragmentActivity context, int sessionDurationMinutes) {
        this(context, sessionDurationMinutes, false, null, false);
    }

    public RegisterParameters(FragmentActivity context) {
        this(context, Constants.SESSION_TWO_MINS, false, null, false);
    }
}
