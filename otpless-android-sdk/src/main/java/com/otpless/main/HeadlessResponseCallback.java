package com.otpless.main;

import androidx.annotation.NonNull;

import com.otpless.dto.HeadlessResponse;

@FunctionalInterface
public interface HeadlessResponseCallback {
    void onHeadlessResponse(@NonNull final HeadlessResponse headlessResponse);
}
