package com.otpless.dto;

public enum HeadlessChannel {
    PHONE("PHONE"),
    EMAIL("EMAIL"),
    OAUTH("OAUTH");



    private final String channelName;
    HeadlessChannel(String requestName) {
        this.channelName = requestName;
    }

    public String getChannelName() {
        return channelName;
    }
}
