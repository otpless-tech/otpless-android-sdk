package com.otpless.network;

import androidx.annotation.Nullable;

public class NetworkStatusData {

    private final ONetworkType type;
    private final ONetworkStatus status;
    private final String netId;

    public NetworkStatusData(ONetworkStatus status, ONetworkType type, String netId) {
        this.type = type;
        this.status = status;
        this.netId = netId;
    }

    public NetworkStatusData() {
        this(ONetworkStatus.NONE, ONetworkType.NONE, null);
    }

    static NetworkStatusData disabled() {
        return new NetworkStatusData(ONetworkStatus.DISABLED, ONetworkType.NONE, null);
    }

    static NetworkStatusData enabled(final ONetworkType type, final String netId) {
        return new NetworkStatusData(ONetworkStatus.ENABLED, type, netId);
    }

    public ONetworkStatus getStatus() {
        return this.status;
    }

    public boolean isEnabled() {
        return this.status == ONetworkStatus.ENABLED;
    }

    @Nullable
    public String getNetId() {
        return this.netId;
    }

    public boolean isWifi() {
        return this.type == ONetworkType.WIFI;
    }

    public boolean isCellular() {
        return this.type == ONetworkType.CELLULAR;
    }
}
