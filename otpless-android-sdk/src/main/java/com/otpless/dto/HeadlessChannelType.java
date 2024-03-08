package com.otpless.dto;

import androidx.annotation.NonNull;

public enum HeadlessChannelType {
    WHATSAPP("WHATSAPP"),
    GMAIL("GMAIL"),
    APPLE("APPLE"),
    TWITTER("TWITTER"),
    DISCORD("DISCORD"),
    SLACK("SLACK"),
    FACEBOOK("FACEBOOK"),
    LINKEDIN("LINKEDIN"),
    MICROSOFT("MICROSOFT"),
    LINE("LINE") ,
    LINEAR("LINEAR") ,
    NOTION("NOTION") ,
    TWITCH("TWITCH") ,
    GITHUB("GITHUB") ,
    BITBUCKET("BITBUCKET") ,
    ATLASSIAN("ATLASSIAN") ,
    GITLAB("GITLAB");

    @NonNull
    private final String channelTypeName;

    private HeadlessChannelType(@NonNull String channelTypeName) {
        this.channelTypeName = channelTypeName;
    }

    @NonNull
    public String getChannelTypeName() {
        return channelTypeName;
    }
}
