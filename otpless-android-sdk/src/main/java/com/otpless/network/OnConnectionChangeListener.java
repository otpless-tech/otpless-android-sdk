package com.otpless.network;

@FunctionalInterface
public interface OnConnectionChangeListener {
    void onConnectionChange(final NetworkStatusData statusData);
}
