package com.otpless.main;

import android.content.IntentSender;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.Nullable;

public interface NativeWebListener {
    void onOtplessEvent(final OtplessEventData event);

    @Nullable
    ActivityResultLauncher<IntentSenderRequest> getPhoneNumberHintLauncher();
}
