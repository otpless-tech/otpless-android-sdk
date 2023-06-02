package com.otpless.web;

public enum LoadingStatus {
    InProgress, // same enum will be used for reloading callback also
    Started,
    Success,
    Failed,
}
