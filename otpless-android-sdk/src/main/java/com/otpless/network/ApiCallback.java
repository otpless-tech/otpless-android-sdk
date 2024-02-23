package com.otpless.network;

public interface ApiCallback<T> {
    void onSuccess(final T data);

    void onError(final Throwable exception);
}
