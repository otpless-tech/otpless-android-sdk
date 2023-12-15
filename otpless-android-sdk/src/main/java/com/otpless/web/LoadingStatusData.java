package com.otpless.web;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoadingStatusData {

    private final LoadingStatus loadingStatus;
    @Nullable
    private final String message;

    private final int errorCode;

    @Nullable
    private final String description;

    public LoadingStatusData(final LoadingStatus status, @Nullable final String message, int errorCode, @Nullable String description) {
        this.loadingStatus = status;
        this.message = message;
        this.errorCode = errorCode;
        this.description = description;
    }

    public LoadingStatusData(final LoadingStatus status) {
        this(status, null, 0, null);
    }

    public LoadingStatus getLoadingStatus() {
        return loadingStatus;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Nullable
    public String getDescription() {
        return description;
    }
}
