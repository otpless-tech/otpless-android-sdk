package com.otpless.utils;

public class SchemeHostMetaInfo {

    private final String scheme;
    private final String host;

    public SchemeHostMetaInfo(final String scheme, final String host) {
        this.scheme = scheme;
        this.host = host;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }
}
