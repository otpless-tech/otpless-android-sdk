package com.otpless.biometric.models;

import androidx.annotation.NonNull;

public class PromptData {

    public final String title;
    public final String subTitle;
    public final String negativeText;

    public PromptData(@NonNull final String title,
                      @NonNull final String subTitle,
                      @NonNull final String negativeText) {
        this.title = title;
        this.subTitle = subTitle;
        this.negativeText = negativeText;
    }

    public PromptData(String title, String subTitle) {
        this(title, subTitle, "");
    }

    public PromptData(String title) {
        this(title, "", "");
    }
}
