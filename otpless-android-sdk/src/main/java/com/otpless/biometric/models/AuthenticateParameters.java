package com.otpless.biometric.models;

import androidx.activity.ComponentActivity;
import androidx.fragment.app.FragmentActivity;

import com.otpless.biometric.Constants;

public class AuthenticateParameters {

    public final FragmentActivity context;
    public final int sessionDurationMinutes;
    public final PromptData promptData;

    public AuthenticateParameters(FragmentActivity context, int sessionDurationMinutes, PromptData promptData) {
        this.context = context;
        this.sessionDurationMinutes = sessionDurationMinutes;
        this.promptData = promptData;
    }

    public AuthenticateParameters(FragmentActivity context, int sessionDurationMinutes) {
        this(context, sessionDurationMinutes, null);
    }

    public AuthenticateParameters(FragmentActivity context) {
        this(context, Constants.SESSION_TWO_MINS, null);
    }
}
