package com.otpless.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.HashSet;

public class OtplessNetworkManager {

    private static OtplessNetworkManager instance;

    @NonNull
    public static OtplessNetworkManager getInstance() {
        if (instance != null) return instance;
        synchronized (OtplessNetworkManager.class) {
            if (instance != null) return instance;
            instance = new OtplessNetworkManager();
        }
        return instance;
    }

    private OtplessNetworkManager() {
    }

    private static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private final HashSet<OnConnectionChangeListener> listeners = new HashSet<>();
    private ConnectivityManager.NetworkCallback mNetworkCallback = null;
    private BroadcastReceiver mNetworkReceiver = null;

    // if change is to be done with triggering callback,
    // use [changeNetworkStatus] method to change [mStatusData] value
    @NonNull
    private NetworkStatusData mStatusData = new NetworkStatusData();

    private boolean isStartFlag = true;
    private Handler mDelayHandler = null;

    public void addListeners(final Context context, final OnConnectionChangeListener listener) {
        // add the network callback
        this.listeners.add(listener);
        // add the network registration if it is not init
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (mNetworkCallback != null) return;
            final NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build();
            final ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
            mNetworkCallback = makeNetworkCallback();
            manager.registerNetworkCallback(request, mNetworkCallback);
        } else {
            if (mNetworkReceiver != null) return;
            mNetworkReceiver = makeNetworkReceiver();
            final IntentFilter filter = new IntentFilter(CONNECTIVITY_CHANGE);
            context.registerReceiver(mNetworkReceiver, filter);
        }
    }

    public void removeListener(final Context context, final OnConnectionChangeListener listener) {
        // remove the network callbacks
        this.listeners.remove(listener);
        // release the network callbacks if no listener is available
        if (this.listeners.isEmpty()) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (mNetworkCallback == null) return;
                final ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
                manager.unregisterNetworkCallback(mNetworkCallback);
                mStatusData = new NetworkStatusData();
                mNetworkCallback = null;
                // remove any pending delay callback
                if (mDelayHandler != null) {
                    mDelayHandler.removeCallbacksAndMessages(null);
                    mDelayHandler = null;
                }
            } else {
                if (mNetworkReceiver == null) return;
                context.unregisterReceiver(mNetworkReceiver);
                mStatusData = new NetworkStatusData();
                mNetworkReceiver = null;
            }
            isStartFlag = true;
        }
    }

    private void changeNetworkStatus(@NonNull final NetworkStatusData newData) {
        final NetworkStatusData oldValue = mStatusData;
        mStatusData = newData;
        // this block should not run second time after first callback from fresh registration
        if (isStartFlag) {
            isStartFlag = false;
            // if first time registration callback and status is enabled then no need for callback
            if (mStatusData.getStatus() == ONetworkStatus.ENABLED) {
                return;
            }
        }
        // change if oldValue and newValue both are enabled type for case of changing network from phone to wifi
        // do not send the callback
        if (oldValue.isEnabled() && newData.isEnabled()) return;
        for (final OnConnectionChangeListener listener : this.listeners) {
            listener.onConnectionChange(newData);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private ConnectivityManager.NetworkCallback makeNetworkCallback() {
        final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                final String netId = network.toString();
                // if status is enabled and new netId is same is previously assigned net id
                if (mStatusData.isEnabled() && netId.equals(mStatusData.getNetId())) return;
                // check for unmetered connection
                final boolean isUnmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
                if (isUnmetered) { // wifi connection
                    changeNetworkStatus(
                            NetworkStatusData.enabled(ONetworkType.WIFI, netId)
                    );
                } else { // cellular connection
                    changeNetworkStatus(
                            NetworkStatusData.enabled(ONetworkType.CELLULAR, netId)
                    );
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                changeNetworkStatus(
                        NetworkStatusData.disabled()
                );
            }
        };
        mDelayHandler = new Handler(Looper.getMainLooper());
        mDelayHandler.postDelayed(() -> {
            if (mStatusData.getStatus() == ONetworkStatus.NONE) {
                changeNetworkStatus(
                        NetworkStatusData.disabled()
                );
            }
            // assign null for early garbage collection
            mDelayHandler = null;
        }, 3_000);
        return callback;
    }

    private BroadcastReceiver makeNetworkReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                if (networkInfo == null) {
                    changeNetworkStatus(
                            NetworkStatusData.disabled()
                    );
                    return;
                }
                switch (networkInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        changeNetworkStatus(
                                NetworkStatusData.enabled(ONetworkType.WIFI, null)
                        );
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        changeNetworkStatus(
                                NetworkStatusData.enabled(ONetworkType.CELLULAR, null)
                        );
                        break;
                    default:
                        changeNetworkStatus(
                                NetworkStatusData.disabled()
                        );
                }

            }
        };
    }

    public NetworkStatusData getNetworkStatus() {
        return this.mStatusData;
    }
}
